package client.serviceCenter.impl;

import client.cache.ServiceCache;
import client.serviceCenter.ServiceCenter;
import client.serviceCenter.ZKWatcher.WatchZK;
import client.serviceCenter.balance.impl.ConsistencyHashBalance;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class ZKServiceCenter implements ServiceCenter {

    private CuratorFramework curator;

    private ServiceCache serviceCache;

    private static final String ROOT_PATH = "/MyRPC";

    private static final String RETRY = "CAN_RETRY";

    public ZKServiceCenter() throws InterruptedException {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

        this.curator = CuratorFrameworkFactory.builder()
                .retryPolicy(retryPolicy)
                .connectString("localhost:2181")
                .namespace(ROOT_PATH)
                .sessionTimeoutMs(40000)
                .build();
        this.curator.start();
        System.out.println("Zookeeper 初始化");

        serviceCache = new ServiceCache();
        WatchZK watchZK = new WatchZK(curator,serviceCache);
        watchZK.watchToUpdate(ROOT_PATH);


    }

    @Override
    public  InetSocketAddress serviceDiscovery(String serviceName){
        try{
            List<String> addressList = serviceCache.getServiceFromCache(serviceName);
            if(addressList.isEmpty()){
                addressList = curator.getChildren().forPath("/"+serviceName);
            }
            String address =new ConsistencyHashBalance().balance(addressList);
            return parseAddress(address);
        }catch (Exception e){
            log.error("Zookeeper 未找到对应服务",e);
        }
        return null;
    }

    @Override
    public boolean checkRetry(String serviceName) {
        boolean canRetry = false;
        try {
            List<String> serviceList = curator.getChildren().forPath("/"+RETRY);
            for (String service: serviceList) {
                if (service.equals(serviceName)) {
                    log.info("查询到服务:"+service);
                    canRetry = true;
                }
            }
        }catch (Exception e){
            log.error("找不到服务",e);
        }
        return canRetry;
    }


    private InetSocketAddress parseAddress(String address){
        String[] split = address.split(":");
        return new InetSocketAddress(split[0],Integer.parseInt(split[1]));
    }
}

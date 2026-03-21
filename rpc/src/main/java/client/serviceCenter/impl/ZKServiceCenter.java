package client.serviceCenter.impl;

import client.cache.ServiceCache;
import client.serviceCenter.ServiceCenter;
import client.serviceCenter.ZKWatcher.WatchZK;
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

    private static final String ROOT_PATH = "/MyRPC";

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

        ServiceCache serviceCache = new ServiceCache();
        WatchZK watchZK = new WatchZK(curator,serviceCache);
        watchZK.watchToUpdate(ROOT_PATH);


    }

    @Override
    public  InetSocketAddress serviceDiscovery(String serviceName){
        try{
            List<String> severs = curator.getChildren().forPath("/"+serviceName);
            String address = severs.get(0);
            return parseAddress(address);
        }catch (Exception e){
            log.error("Zookeeper 未找到对应服务",e);
        }
        return null;
    }



    private InetSocketAddress parseAddress(String address){
        String[] split = address.split(":");
        return new InetSocketAddress(split[0],Integer.parseInt(split[1]));
    }
}

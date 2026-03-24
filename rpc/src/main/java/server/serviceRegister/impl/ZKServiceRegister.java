package server.serviceRegister.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import server.serviceRegister.ServiceRegister;

import java.net.InetSocketAddress;

@Slf4j
public class ZKServiceRegister implements ServiceRegister {


    public CuratorFramework curator;

    private static final String ROOT_PATH = "MyRPC";

    private static final String RETRY = "CanRetry";

    public ZKServiceRegister() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.curator = CuratorFrameworkFactory.builder()
                .connectString("localhost:2181")
                .retryPolicy(retryPolicy)
                .connectionTimeoutMs(3000)
                .sessionTimeoutMs(40000)
                .namespace(ROOT_PATH)
                .build();
        this.curator.start();
    }

    @Override
    public void register(String serviceName, InetSocketAddress addr,boolean canRetry) {
        try{
            if (curator.checkExists().forPath(ROOT_PATH + "/" + serviceName) == null) {
                curator.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(ROOT_PATH + "/" + serviceName);
            }
            String path = "/"+serviceName+"/"+getServiceAddress(addr);
            curator.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);

            if (canRetry) {
                String retryPath = "/"+RETRY+"/"+serviceName+"/"+getServiceAddress(addr);
                curator.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(retryPath);
            }
        }catch (Exception e){
            log.error("zookeeper register error",e);
        }

    }

    private String getServiceAddress(InetSocketAddress addr){
        return addr.getAddress().getHostAddress()+":"+addr.getPort();
    }

    private InetSocketAddress parseAddress(String address){
        String[] split = address.split(":");
        return new InetSocketAddress(split[0],Integer.parseInt(split[1]));
    }

}

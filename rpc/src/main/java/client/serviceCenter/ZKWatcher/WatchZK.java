package client.serviceCenter.ZKWatcher;

import client.cache.ServiceCache;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;

@Slf4j
public class WatchZK {

    private CuratorFramework curator;

    ServiceCache serviceCache;

    public WatchZK(CuratorFramework curator, ServiceCache serviceCache){
        this.curator = curator;
        this.serviceCache = serviceCache;
    }

    public void watchToUpdate(String path) throws InterruptedException{
        CuratorCache curatorCache = CuratorCache.build(curator,"/");
        curatorCache.listenable().addListener(new CuratorCacheListener() {
            @Override
            public void event(Type type, ChildData oldData, ChildData data) {
                switch (type.name()){
                    case "NODE_CREATED":
                        String[] pathList = pasrePath(data);
                        if(pathList.length<=2){
                            break;
                        }else {
                            String serviceName = pathList[1];
                            String address = pathList[2];
                            serviceCache.addServiceToCache(serviceName,address);
                        }
                        break;
                    case "NODE_CHANGED":
                        if (oldData.getData()!=null){
                            log.info("修改前数据"+oldData.getData().toString());
                        }else {
                            log.info("节点初次赋值！");
                        }
                        String[] oldPathList = pasrePath(oldData);
                        String[] newPathList = pasrePath(data);
                        serviceCache.replaceServiceAddress(oldPathList[1],oldPathList[2],newPathList[2]);
                        log.info("修改后数据："+data.getData().toString());
                        break;
                    case "NODE_DELETED":
                        String[] pathList_d = pasrePath(data);
                        if(pathList_d.length<=2){
                            break;
                        }else  {
                            String serviceName = pathList_d[1];
                            String address = pathList_d[2];
                            serviceCache.delete(serviceName,address);
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        curatorCache.start();
    }


    //解析节点对应地址
    public String[] pasrePath(ChildData childData){
        //获取更新的节点的路径
        String path=new String(childData.getPath());
        //按照格式 ，读取
        return path.split("/");
    }
}

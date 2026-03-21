package client.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ServiceCache {

    private Map<String, List<String>> cache = new HashMap<>();

    public void addServiceToCache(String serviceName,String address){
        if (cache.containsKey(serviceName)){
            cache.get(serviceName).add(address);
        }else {
            cache.put(serviceName, Arrays.asList(address));
        }
    }

    public void replaceServiceAddress(String serviceName,String oldAddress,String newAddress){
        if (cache.containsKey(serviceName)){
            cache.get(serviceName).remove(oldAddress);
            cache.get(serviceName).add(newAddress);
        }else  {
            log.error(oldAddress+" not exists in "+serviceName);
        }
    }

    public List<String> getServiceFromCache(String serviceName){
        if (!cache.containsKey(serviceName)){
            return null;
        }
        List<String> a =  cache.get(serviceName);
        return a;
    }

    public void delete(String serviceName,String address){
        List<String> addressList = cache.get(serviceName);
        addressList.remove(address);
    }

}

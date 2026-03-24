package client.serviceCenter.balance.impl;

import client.serviceCenter.balance.LoadBalance;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class ConsistencyHashBalance implements LoadBalance {

    private static final int VIRTUAL_NUM = 5;

    private SortedMap<Integer,String> shards = new TreeMap<Integer,String>();

    private List<String> realNodes = new LinkedList<String>();

    private String[] severs =  null;

    private void init(List<String> serviceList){
        for (String server : serviceList) {
            realNodes.add(server);
            log.info("节点["+server+"]被添加");
            for(int i = 0; i < VIRTUAL_NUM; i++){
                String virtualNode = server + "&&VN" +i;
                int hash = getHash(virtualNode);
                shards.put(hash,virtualNode);
            }
        }
    }

    public String getServer(String node,List<String> serviceList){
        init(serviceList);
        int hash = getHash(node);
        Integer key = null;
        SortedMap<Integer,String> sortedMap = shards.tailMap(hash);
        if (sortedMap.isEmpty()) {
            key = shards.lastKey();
        }else  {
            key = sortedMap.firstKey();
        }
        String virtualNode = shards.get(key);
        return virtualNode.substring(0,virtualNode.indexOf("&&"));
    }


    @Override
    public String balance(List<String> addressList) {

        String random= UUID.randomUUID().toString();
        return getServer(random,addressList);
    }

    @Override
    public void addNode(String node) {
        if (realNodes.contains(node)) {
            realNodes.add(node);
            for (int i = 1; i < VIRTUAL_NUM; i++) {
                String virtualNode = node + "&&VN" + i;
                int hash = getHash(virtualNode);
                shards.put(hash,virtualNode);
            }
        }
    }

    @Override
    public void removeNode(String node) {
        if (realNodes.contains(node)) {
            realNodes.remove(node);
            for (int i = 1; i < VIRTUAL_NUM; i++) {
                String virtualNode = node + "&&VN" + i;
                int hash = getHash(virtualNode);
                shards.remove(hash);
            }
        }

    }


    /**
     * FNV1_32_HASH算法
     */
    private static int getHash(String str) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < str.length(); i++)
            hash = (hash ^ str.charAt(i)) * p;
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        // 如果算出来的值为负数则取其绝对值
        if (hash < 0)
            hash = Math.abs(hash);
        return hash;
    }
}

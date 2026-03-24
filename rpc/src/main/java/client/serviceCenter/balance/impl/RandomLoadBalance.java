package client.serviceCenter.balance.impl;

import client.serviceCenter.balance.LoadBalance;

import java.util.List;
import java.util.Random;

public class RandomLoadBalance implements LoadBalance {

    @Override
    public String balance(List<String> addressList){
        int size = addressList.size();
        Random random = new Random();
        int index = random.nextInt(size);
        return addressList.get(index);
    }

    @Override
    public void addNode(String node) {

    }

    @Override
    public void removeNode(String node) {

    }

}

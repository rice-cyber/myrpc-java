package client.serviceCenter.balance.impl;

import client.serviceCenter.balance.LoadBalance;

import java.util.List;

public class RoundLoadBalance implements LoadBalance {

    public int choose = -1;

    @Override
    public String balance(List<String> addressList) {
        choose++;
        choose= choose%addressList.size();
        return addressList.get(choose);
    }

    @Override
    public void addNode(String node) {

    }

    @Override
    public void removeNode(String node) {

    }
}

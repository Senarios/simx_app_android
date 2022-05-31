package com.hdev.common.datamodels;

public class NetworkModel {
    private boolean isNetwork;

    public NetworkModel(boolean isNetwork) {
        this.isNetwork = isNetwork;
    }

    public boolean isNetwork() {
        return isNetwork;
    }

    public void setNetwork(boolean network) {
        isNetwork = network;
    }
}

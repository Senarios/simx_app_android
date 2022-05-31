package com.hdev.common.datamodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResponseBroadcast {
    @SerializedName(value = "resource",alternate = "data")
    @Expose
    private List<Broadcasts> resource = null;


    public List<Broadcasts> getResource() {
        return resource;
    }

    public void setResource(List<Broadcasts> resource) {
        this.resource = resource;
    }
}

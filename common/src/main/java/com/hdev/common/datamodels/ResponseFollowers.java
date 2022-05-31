package com.hdev.common.datamodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResponseFollowers {
    @SerializedName("resource")
    @Expose
    private List<Followers> resource = null;

    public List<Followers> getResource() {
        return resource;
    }

    public void setResource(List<Followers> resource) {
        this.resource = resource;
    }
}

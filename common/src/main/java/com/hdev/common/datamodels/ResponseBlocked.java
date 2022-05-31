package com.hdev.common.datamodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResponseBlocked extends Object {
    @SerializedName("resource")
    @Expose
    private List<Blocked> resource = null;

    public List<Blocked> getResource() {
        return resource;
    }

    public void setResource(List<Blocked> resource) {
        this.resource = resource;
    }
}

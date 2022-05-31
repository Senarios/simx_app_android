package com.hdev.common.datamodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResponseUsers {
    @SerializedName("resource")
    @Expose
    private List<Users> resource = null;

    public List<Users> getResource() {
        return resource;
    }

    public void setResource(List<Users> resource) {
        this.resource = resource;
    }
}

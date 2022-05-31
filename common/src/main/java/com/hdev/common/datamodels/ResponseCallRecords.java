package com.hdev.common.datamodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResponseCallRecords {
    @SerializedName("resource")
    @Expose
    private List<CallRecords> resource = null;

    public List<CallRecords> getResource() {
        return resource;
    }

    public void setResource(List<CallRecords> resource) {
        this.resource = resource;
    }
}

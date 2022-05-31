package com.hdev.common.datamodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ResponseJobCandidate implements Serializable {

    @SerializedName("resource")
    @Expose
    private ArrayList<JobCandidates> resource = new ArrayList<>();


    public ArrayList<JobCandidates> getResource() {
        return resource;
    }

    public void setResource(ArrayList<JobCandidates> resource) {
        this.resource = resource;
    }
}

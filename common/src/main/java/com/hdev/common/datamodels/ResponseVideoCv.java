package com.hdev.common.datamodels;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ResponseVideoCv implements Serializable {
    @SerializedName("resource")
    private List<VideoCv> videoCvs=new ArrayList<>();

    public List<VideoCv> getVideoCvs() {
        return videoCvs;
    }

    public void setVideoCvs(List<VideoCv> videoCvs) {
        this.videoCvs = videoCvs;
    }
}



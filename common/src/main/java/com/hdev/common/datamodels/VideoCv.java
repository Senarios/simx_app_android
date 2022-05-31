package com.hdev.common.datamodels;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class VideoCv implements Serializable {
    @SerializedName("title")
    private String title;

    @SerializedName("videocv")
    private String videocv;

    @SerializedName("username")
    private String username;

    @SerializedName("id")
    private int id;

    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVideocv() {
        return videocv;
    }

    public void setVideocv(String videocv) {
        this.videocv = videocv;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

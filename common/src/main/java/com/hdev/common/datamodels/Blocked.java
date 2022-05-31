package com.hdev.common.datamodels;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Blocked implements Serializable
{

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("userid")
    @Expose
    private String userid;
    @SerializedName("blockedid")
    @Expose
    private String blockedid;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("blockedname")
    @Expose
    private String blockedname;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getBlockedid() {
        return blockedid;
    }

    public void setBlockedid(String blockedid) {
        this.blockedid = blockedid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBlockedname() {
        return blockedname;
    }

    public void setBlockedname(String blockedname) {
        this.blockedname = blockedname;
    }

}
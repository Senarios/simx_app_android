package com.hdev.common.datamodels;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Followers implements Parcelable {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("userid")
    @Expose
    private String userid;
    @SerializedName("userEmail")
    @Expose
    private String userEmail;
    @SerializedName("followerid")
    @Expose
    private String followerid;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("followername")
    @Expose
    private String followername;
    @SerializedName("followerEmail")
    @Expose
    private String followerEmail;

    public Followers() {
    }

    protected Followers(Parcel in) {
        id = in.readString();
        userid = in.readString();
        followerid = in.readString();
        username = in.readString();
        userEmail = in.readString();
        followername = in.readString();
        followerEmail = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(userid);
        dest.writeString(followerid);
        dest.writeString(username);
        dest.writeString(userEmail);
        dest.writeString(followername);
        dest.writeString(followerEmail);
    }
    public static final Creator<Followers> CREATOR = new Creator<Followers>() {
        @Override
        public Followers createFromParcel(Parcel in) {
            return new Followers(in);
        }

        @Override
        public Followers[] newArray(int size) {
            return new Followers[size];
        }
    };

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

    public String getFollowerid() {
        return followerid;
    }

    public void setFollowerid(String followerid) {
        this.followerid = followerid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFollowername() {
        return followername;
    }

    public void setFollowername(String followername) {
        this.followername = followername;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getFollowerEmail() {
        return followerEmail;
    }

    public void setFollowerEmail(String followerEmail) {
        this.followerEmail = followerEmail;
    }
}

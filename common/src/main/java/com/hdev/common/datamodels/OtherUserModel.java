package com.hdev.common.datamodels;

import java.io.Serializable;
import java.util.ArrayList;

public class OtherUserModel implements Serializable {
    private ArrayList<Followers> following;
    private ArrayList<Followers> followers;
    private ArrayList<Blocked> blocked;
    private ArrayList<Broadcasts> broadcasts;
    private Users users;

    public OtherUserModel() {

    }

    public ArrayList<Followers> getFollowing() {
        return following;
    }

    public void setFollowing(ArrayList<Followers> following) {
        this.following = following;
    }



    public ArrayList<Followers> getFollowers() {
        return followers;
    }

    public void setFollowers(ArrayList<Followers> followers) {
        this.followers = followers;
    }

    public ArrayList<Broadcasts> getBroadcasts() {
        return broadcasts;
    }

    public void setBroadcasts(ArrayList<Broadcasts> broadcasts) {
        this.broadcasts = broadcasts;
    }

    public ArrayList<Blocked> getBlocked() {
        return blocked;
    }

    public void setBlocked(ArrayList<Blocked> blocked) {
        this.blocked = blocked;
    }

    public Users getUsers() {
        return users;
    }

    public void setUsers(Users users) {
        this.users = users;
    }
}

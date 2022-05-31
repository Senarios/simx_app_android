package com.hdev.common.datamodels;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.hdev.common.Constants;

import java.io.Serializable;

public class JobCandidates implements Serializable {

	private final String BASE_URL= Constants.DreamFactory.GET_IMAGE_URL;

	@SerializedName("users_by_username")
	private Users user;

	@SerializedName("broadcasts_by_broadcast")
	private Broadcasts broadcasts;

	@SerializedName("broadcast")
	private String broadcast;

	@SerializedName("username")
	private String username;

	@SerializedName("id")
	private int id;

	@SerializedName("videocvID")
	private int videocvID;

	@SerializedName("broadcast_id")
	private int broadcast_id;

	@SerializedName("isshortlisted")
	private boolean isshortlisted;

	@SerializedName("videocvs_by_videocvID")
	private VideoCv videoCv;

	public boolean isshortlisted() {
		return isshortlisted;
	}

	public void setIsshortlisted(boolean isshortlisted) {
		this.isshortlisted = isshortlisted;
	}

	public Users getUser() {
		return user;
	}

	public void setUser(Users user) {
		this.user = user;
	}

	public String getBroadcast() {
		return broadcast;
	}

	public JobCandidates setBroadcast(String broadcast) {
		this.broadcast = broadcast;
		return this;
	}

	public JobCandidates setUsername(String username){
		this.username = username;
		return this;
	}

	public String getBASE_URL() {
		return BASE_URL;
	}

	public String getUsername(){
		return username;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Broadcasts getBroadcasts() {
		return broadcasts;
	}

	public void setBroadcasts(Broadcasts broadcasts) {
		this.broadcasts = broadcasts;
	}

	public int getVideocvID() {
		return videocvID;
	}

	public void setVideocvID(int videocvID) {
		this.videocvID = videocvID;
	}



	public VideoCv getVideoCv() {
		return videoCv;
	}

	public void setVideoCv(VideoCv videoCv) {
		this.videoCv = videoCv;
	}


	public int getBroadcast_id() {
		return broadcast_id;
	}

	public void setBroadcast_id(int broadcast_id) {
		this.broadcast_id = broadcast_id;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		assert obj != null;
		return this.username.equals(((JobCandidates)obj).username);
	}
}
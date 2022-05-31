package com.hdev.common.datamodels;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.hdev.common.CommonUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.internal.Util;

@SuppressWarnings("ALL")
public class Broadcasts implements Serializable {
    public boolean isLoadMore = false;
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("broadcast")
    @Expose
    private String broadcast;
    @SerializedName("location")
    @Expose
    private String location;
    @SerializedName("latti")
    @Expose
    private String latti;
    @SerializedName("longi")
    @Expose
    private String longi;
    @SerializedName("imglink")
    @Expose
    private String imglink;
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("skill")
    @Expose
    private String skill;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("arn")
    @Expose
    private String arn;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("time")
    @Expose
    private String time;
    @SerializedName("viewers")
    @Expose
    private Integer viewers;

    @SerializedName("isOffline")
    @Expose
    private boolean isOffline;

    @SerializedName("isJob")
    @Expose
    private boolean isjob;

    @SerializedName("applyonvideo")
    @Expose
    private boolean applyonvideo;

    @SerializedName("applyonjobsite")
    @Expose
    private boolean applyonjobsite;

    @SerializedName("messageonly")
    @Expose
    private boolean messageonly;
    @SerializedName("callonly")
    @Expose
    private boolean callonly;
    @SerializedName("bothmsgcall")
    @Expose
    private boolean bothmsgcall;

    @SerializedName("jobcandidates_by_broadcast")
    @Expose
    private List<JobCandidates> jobCandidates=new ArrayList<>();

    @SerializedName("tags_by_broadcast")
    @Expose
    private List<Tags> tags=new ArrayList<>();

    @SerializedName("jobDescription")
    @Expose
    private String jobDes;


    @SerializedName("users_by_username")
    @Expose
    private Users users;

    @SerializedName("videourl")
    @Expose
    private String videourl;

    @SerializedName("isApproved")
    @Expose
    private boolean isApproved;
    @SerializedName("jobSiteLink")
    @Expose
    private String jobSiteLink;
    @SerializedName("jobPostStatus")
    @Expose
    private String jobPostStatus;

    public String getJobPostStatus() {
        return jobPostStatus;
    }

    public void setJobPostStatus(String jobPostStatus) {
        this.jobPostStatus = jobPostStatus;
    }

    public String getJobSiteLink() {
        return jobSiteLink;
    }

    public void setJobSiteLink(String jobSiteLink) {
        this.jobSiteLink = jobSiteLink;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    public boolean isMessageonly() {
        return messageonly;
    }

    public void setMessageonly(boolean messageonly) {
        this.messageonly = messageonly;
    }

    public boolean isCallonly() {
        return callonly;
    }

    public void setCallonly(boolean callonly) {
        this.callonly = callonly;
    }

    public boolean isBothmsgcall() {
        return bothmsgcall;
    }

    public void setBothmsgcall(boolean bothmsgcall) {
        this.bothmsgcall = bothmsgcall;
    }

    public boolean isApplyonvideo() {
        return applyonvideo;
    }

    public void setApplyonvideo(boolean applyonvideo) {
        this.applyonvideo = applyonvideo;
    }

    public boolean isApplyonjobsite() {
        return applyonjobsite;
    }

    public void setApplyonjobsite(boolean applyonjobsite) {
        this.applyonjobsite = applyonjobsite;
    }

    private StreamStatus streamStatus;

    private int position;

    private boolean isApplied;

    private String duration;

    public String getVideourl() {
        return videourl;
    }

    public void setVideourl(String videourl) {
        this.videourl = videourl;
    }

    public StreamStatus getStreamStatus() {
        return streamStatus;
    }

    public Broadcasts setStreamStatus(StreamStatus streamStatus) {
        this.streamStatus = streamStatus;
        return this;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setJobDes (String jobDes){this.jobDes=jobDes;}

    public String getJobDes (){return jobDes;}

    public boolean isApplied() {
        return isApplied;
    }

    public void setApplied(boolean applied) {
        isApplied = applied;
    }

    public boolean isIsjob() {
        return isjob;
    }

    public List<JobCandidates> getJobCandidates() {
        return jobCandidates;
    }

    public void setJobCandidates(List<JobCandidates> jobCandidates) {
        this.jobCandidates = jobCandidates;
    }

    public boolean isjob() {
        return isjob;
    }

    public void setIsjob(boolean isjob) {
        this.isjob = isjob;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public void setOffline(boolean offline) {
        isOffline = offline;
    }

    private Integer camera;

    public Integer getCamera() {
        return camera;
    }

    public void setCamera(Integer camera) {
        this.camera = camera;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBroadcast() {
        return broadcast;
    }

    public void setBroadcast(String broadcast) {
        this.broadcast = broadcast;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLatti() {
        return latti;
    }

    public void setLatti(String latti) {
        this.latti = latti;
    }

    public String getLongi() {
        return longi;
    }

    public void setLongi(String longi) {
        this.longi = longi;
    }

    public String getImglink() {
        return imglink;
    }

    public void setImglink(String imglink) {
        this.imglink = imglink;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public String getStatus() {
        if (status != "") {
            return status;
        }
        else
        {
         status="";
         return status;
        }
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Integer getViewers() {
        return viewers;
    }

    public void setViewers(Integer viewers) {
        this.viewers = viewers;
    }


    public int getPosition() {
        return position;
    }

    public Users getUsers() {
        return users;
    }

    public void setUsers(Users users) {
        this.users = users;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public static Broadcasts fromJson(String s){
        return new Gson().fromJson(s,Broadcasts.class);
    }

    public List<Tags> getTags() {
        return tags;
    }

    public void setTags(List<Tags> tags) {
        this.tags = tags;
    }

    public boolean isLoadMore() {
        return isLoadMore;
    }

    public Broadcasts setLoadMore(boolean loadMore) {
        isLoadMore = loadMore;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        CommonUtils.showLog(""+o.hashCode());
        CommonUtils.showLog(""+this.hashCode());
        Broadcasts obj=((Broadcasts)o);
        Boolean eq=obj!=null && this.id.equals(obj.id);
        CommonUtils.showLog(""+eq);
        return eq;

    }

    @Override
    public int hashCode() {
        return this.id*31;
     }
}

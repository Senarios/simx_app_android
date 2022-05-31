package com.hdev.common.datamodels;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Users implements Serializable {
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("picture")
    @Expose
    private String picture;
    @SerializedName("password")
    @Expose
    private String password;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("skills")
    @Expose
    private String skills;
    @SerializedName("linkedin")
    @Expose
    private String linkedin;
    @SerializedName("qbid")
    @Expose
    private String qbid;
    @SerializedName("paypal")
    @Expose
    private Boolean paypal;
    @SerializedName("broadcasts")
    @Expose
    private Integer broadcasts;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("arn")
    @Expose
    private String arn;
    @SerializedName("rate")
    @Expose
    private String rate;
    @SerializedName("link")
    @Expose
    private String link;
    @SerializedName("credit")
    @Expose
    private Float credit;
    @SerializedName("videocv")
    private String Videocv;
    @SerializedName("videocvs_by_username")
    private List<VideoCv> cvs=new ArrayList<>();
    @SerializedName("bank_name")
    @Expose
    private String bankName;
    @SerializedName("account_no")
    @Expose
    private String accountNo;
    @SerializedName("user_ratings")
    @Expose
    private String userRatings;
    @SerializedName("total_ratings")
    @Expose
    private String totalRatings;

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<VideoCv> getCvs() {
        return cvs;
    }

    public void setCvs(List<VideoCv> cvs) {
        this.cvs = cvs;
    }

    public String getVideocv() {
        return Videocv;
    }

    public String getUserRatings() {
        return userRatings;
    }

    public String getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(String totalRatings) {
        this.totalRatings = totalRatings;
    }

    public void setUserRatings(String userRatings) {
        this.userRatings = userRatings;
    }

    public void setVideocv(String videocv) {
        Videocv = videocv;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getLinkedin() {
        return linkedin;
    }

    public void setLinkedin(String linkedin) {
        this.linkedin = linkedin;
    }

    public String getQbid() {
        return qbid;
    }

    public void setQbid(String qbid) {
        this.qbid = qbid;
    }

    public Boolean getPaypal() {
        return paypal;
    }

    public void setPaypal(Boolean paypal) {
        this.paypal = paypal;
    }

    public Integer getBroadcasts() {
        return broadcasts;
    }

    public void setBroadcasts(Integer broadcasts) {
        this.broadcasts = broadcasts;
    }

    public String getArn() {
        return arn;
    }

    public void setArn(String arn) {
        this.arn = arn;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Float getCredit() {
        return credit;
    }

    public void setCredit(Float credit) {
        this.credit = credit;
    }


    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}

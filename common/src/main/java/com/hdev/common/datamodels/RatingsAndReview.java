package com.hdev.common.datamodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RatingsAndReview {
    @SerializedName("id")
    @Expose
    private Integer id ;
    @SerializedName("review")
    @Expose
    private String review;
    @SerializedName("userId")
    @Expose
    private String userId;
    @SerializedName("toUserId")
    @Expose
    private String toUserId;
    @SerializedName("rating")
    @Expose
    private String rating ;
    @SerializedName("users_by_userId")
    @Expose
    private Users user ;




    public RatingsAndReview() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

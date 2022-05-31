package com.hdev.common.datamodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Ratings {
    @SerializedName("username")
    @Expose
    private String username;
    @SerializedName("user_ratings")
    @Expose
    private String userRatings;
    @SerializedName("total_ratings")

    @Expose
    private String totalRatings;

    public Ratings() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserRatings() {
        return userRatings;
    }

    public void setUserRatings(String userRatings) {
        this.userRatings = userRatings;
    }

    public String getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(String totalRatings) {
        this.totalRatings = totalRatings;
    }
}

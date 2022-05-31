package com.hdev.common.datamodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RatingsReviewsResponse {
    @SerializedName("data")
    @Expose
    private Ratings ratings;

    public Ratings getRatingsResponse() {
        return ratings;
    }

    public void setRatingsResponse(Ratings ratings) {
        this.ratings = ratings;
    }
}

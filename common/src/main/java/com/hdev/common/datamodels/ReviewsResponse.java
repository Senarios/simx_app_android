package com.hdev.common.datamodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReviewsResponse {
    @SerializedName("resource")
    @Expose
    private List<RatingsAndReview> ratingsAndReview;

    public List<RatingsAndReview> getRatingsAndReview() {
        return ratingsAndReview;
    }

    public void setRatingsAndReview(List<RatingsAndReview> ratingsAndReview) {
        this.ratingsAndReview = ratingsAndReview;
    }
}

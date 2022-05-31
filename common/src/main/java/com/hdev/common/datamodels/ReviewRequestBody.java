package com.hdev.common.datamodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ReviewRequestBody {
    @SerializedName("resource")
    @Expose
    private RatingsAndReview ratingsAndReview;

    public RatingsAndReview getRatingsAndReview() {
        return ratingsAndReview;
    }

    public void setRatingsAndReview(RatingsAndReview ratingsAndReview) {
        this.ratingsAndReview = ratingsAndReview;
    }
}

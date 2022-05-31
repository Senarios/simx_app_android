package com.senarios.simxx.adaptors.viewHolders;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hdev.common.Constants;
import com.hdev.common.datamodels.RatingsAndReview;
import com.senarios.simxx.R;

public class ReviewsViewHolder extends RecyclerView.ViewHolder {

    public TextView review, name, rating;
    public ImageView image;
    RatingBar ratingBar;
    public View parent;

    public ReviewsViewHolder(@NonNull View itemView) {
        super(itemView);
        parent = itemView;
        review = itemView.findViewById(R.id.review);
        name = itemView.findViewById(R.id.name);
        image = itemView.findViewById(R.id.image);
        ratingBar = itemView.findViewById(R.id.ratingBar);
    }

    public void setData(Context context ,RatingsAndReview ratingsAndReview) {
        if (ratingsAndReview.getReview()==null||ratingsAndReview.getReview().equalsIgnoreCase("null")|| ratingsAndReview.getReview().equalsIgnoreCase("")){
           review.setVisibility(View.GONE);
        }else review.setText(ratingsAndReview.getReview());
        name.setText(ratingsAndReview.getUser().getName());
        ratingBar.setRating(Float.parseFloat(ratingsAndReview.getRating()));
        if (ratingsAndReview.getUser().getPicture() != null && !ratingsAndReview.getUser().getPicture().equalsIgnoreCase("")) {
            Glide.with(context)
                    .load(Constants.DreamFactory.GET_IMAGE_URL + ratingsAndReview.getUser().getUsername()  + ".png")
                    .centerCrop()
                    .placeholder(R.drawable.h2pay2)
                    .into(image);
        }


    }
}

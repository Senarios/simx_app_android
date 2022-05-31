package com.senarios.simxx.adaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hdev.common.datamodels.RatingsAndReview;
import com.senarios.simxx.R;
import com.senarios.simxx.adaptors.viewHolders.ReviewsViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsViewHolder> {

    private Context context;
    private List<RatingsAndReview> reviewList;

    public ReviewsAdapter(Context context, List<RatingsAndReview> lodgeList) {
        this.context = context;
        this.reviewList = lodgeList;
    }

    @NonNull
    @Override
    public ReviewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.reviews_list_item, parent, false);
        return new ReviewsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewsViewHolder holder, int position) {
        holder.setData(context,reviewList.get(position));
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public void filteredList(ArrayList<RatingsAndReview> filteredList) {
        this.reviewList = filteredList;
        notifyDataSetChanged();
    }
}
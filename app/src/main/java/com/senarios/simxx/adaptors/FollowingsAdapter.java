package com.senarios.simxx.adaptors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.hdev.common.Constants;
import com.senarios.simxx.R;
import com.senarios.simxx.callbacks.FollowCallBack;
import com.hdev.common.datamodels.Followers;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FollowingsAdapter extends RecyclerView.Adapter<FollowingsAdapter.ViewHolder> {
    private ArrayList<Followers> arrayList;
    private ArrayList<Followers> followingList;
    private Context context;
    private FollowCallBack callBack;

    public FollowingsAdapter(ArrayList<Followers> arraylist2,ArrayList<Followers> arrayList ,Context context,FollowCallBack callBack) {
        this.arrayList = arrayList;
        this.followingList=arraylist2;
        this.context = context;
        this.callBack=callBack;
    }

    @NonNull
    @Override
    public FollowingsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_view_followers_following,parent,false);
        return new ViewHolder(view);
    }




    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull FollowingsAdapter.ViewHolder holder, int position) {
        holder.name.setText(arrayList.get(position).getFollowername());

//        Glide.with(context).load(Constants.DreamFactory.GET_IMAGE_URL+arrayList.get(position).getFollowerid()+".png")
//                .into(holder.circle_profile_picture);
        if(arrayList.get(holder.getAdapterPosition()).getFollowerEmail() != null) {
            Glide.with(context)
                    .asBitmap()
                    .load(Constants.DreamFactory.GET_IMAGE_URL+arrayList.get(holder.getAdapterPosition()).getFollowerEmail()+".png")
                    .placeholder(R.drawable.h2pay2)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            holder.circle_profile_picture.setImageBitmap(resource);
                        }
                    });
        } else {
            holder.circle_profile_picture.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.h2pay2));
        }

        if(followingList != null)
        {
            for(int i=0;i<followingList.size();i++)
            {
                if (followingList.get(i).getFollowername().matches(arrayList.get(position).getFollowername()))
                {
                    holder.follow.setText("Following");
                    holder.follow.setBackgroundResource(R.drawable.go_live_button);
                }
            }
        }


        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBack.OnClick(arrayList.get(position).getFollowerid());
            }
        });


    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private CircleImageView circle_profile_picture;
        private ConstraintLayout container;
        private Button follow;


        private ViewHolder(View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.Name);
            container=itemView.findViewById(R.id.cardView);
            circle_profile_picture=itemView.findViewById(R.id.image);
            follow = itemView.findViewById(R.id.follow);





        }
    }
}

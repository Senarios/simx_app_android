package com.senarios.simxx.adaptors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.hdev.common.Constants;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.callbacks.BroadcastCallback;
import com.hdev.common.datamodels.Broadcasts;
import com.senarios.simxx.databinding.ItemOtherUserStreamsBinding;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class OtherUserBroadcastsAdapter extends RecyclerView.Adapter<OtherUserBroadcastsAdapter.ViewHolder> {
    private ArrayList<Broadcasts> arrayList;
    private Context context;
    private BroadcastCallback broadcastCallback;


    public OtherUserBroadcastsAdapter(ArrayList<Broadcasts> arrayList, Context context, BroadcastCallback broadcastCallback) {
        this.arrayList = arrayList;
        this.context = context;
        this.broadcastCallback=broadcastCallback;
    }

    @NonNull
    @Override
    public OtherUserBroadcastsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_other_user_streams,parent,false);
        ItemOtherUserStreamsBinding binding= DataBindingUtil.bind(view);
        return new ViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull OtherUserBroadcastsAdapter.ViewHolder holder, int position) {
        if (arrayList.get(position).getStatus()!=null && arrayList.get(position).getStatus().equalsIgnoreCase(Constants.GoCoder.ONLINE)){
            holder.binding.container.setBackgroundColor(context.getResources().getColor(R.color.LavenderBlush));
        }
        holder.binding.name.setText(arrayList.get(position).getTitle());
        holder.binding.doctorName.setText(arrayList.get(position).getName());
        holder.binding.jobDes.setHint("");
        holder.binding.jobDes.setText(arrayList.get(position).getJobDes());
        String thumbnail = Constants.DreamFactory.GET_IMAGE_URL+arrayList.get(position).getBroadcast()+".png";
        Utility.showLog("thumbnail "+thumbnail);


        if (arrayList.get(position).getVideourl()!=null&&arrayList.get(position).getVideourl().startsWith("https://youtu")) {
            String fullsize_path_img = "http://img.youtube.com/vi/"+Utility.getYouTubeId(arrayList.get(position).getVideourl())+"/0.jpg";
//            holder.binding.ytlinear.setVisibility(View.VISIBLE);
            Glide.with(context).load(fullsize_path_img)
                    .optionalCenterCrop()
                    .error(R.drawable.h2pay2)
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            holder.binding.image.setImageResource(R.drawable.h2pay2);
                            holder.binding.progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.binding.progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(holder.binding.image);

        } else {
            Glide.with(context).load(Constants.DreamFactory.GET_IMAGE_URL+arrayList.get(position).getImglink()+".png")
                    .optionalCenterCrop()
                    .error(R.drawable.h2pay2)
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            holder.binding.image.setImageResource(R.drawable.h2pay2);
                            holder.binding.progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            holder.binding.progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(holder.binding.image);
        }



        Glide.with(context).load(Constants.DreamFactory.GET_IMAGE_URL+arrayList.get(position).getUsername()+".png")
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.binding.circleProfilePicture);
        if (arrayList.get(position).getViewers()!=null) {
            holder.binding.viewersNumber.setText("" + arrayList.get(position).getViewers());
        }
        holder.binding.container.setOnClickListener(v -> {
            broadcastCallback.OnBroadcastClicked(arrayList.get(position));

        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
       private ItemOtherUserStreamsBinding binding;
        private ViewHolder(ItemOtherUserStreamsBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }
    }

}

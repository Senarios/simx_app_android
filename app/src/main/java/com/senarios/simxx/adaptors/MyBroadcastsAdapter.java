package com.senarios.simxx.adaptors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.senarios.simxx.activities.OfflineStreamActivity;
import com.senarios.simxx.activities.ViewStream;
import com.hdev.common.datamodels.Broadcasts;
import com.senarios.simxx.databinding.ItemMyBroadcastsBinding;

import java.util.ArrayList;


public class MyBroadcastsAdapter extends RecyclerView.Adapter<MyBroadcastsAdapter.ViewHolder> {
    private ArrayList<Broadcasts> arrayList;
    private Context context;
    private RecyclerViewCallback listener;


    public MyBroadcastsAdapter(ArrayList<Broadcasts> arrayList, Context context, RecyclerViewCallback listener) {
        this.arrayList = arrayList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyBroadcastsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         View view = LayoutInflater.from(context).inflate(R.layout.item_my_broadcasts,parent,false);
         ItemMyBroadcastsBinding binding= DataBindingUtil.bind(view);
        return new ViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MyBroadcastsAdapter.ViewHolder holder, int position) {
        holder.binding.container.setBackgroundColor(context.getResources().getColor(R.color.white));
        if (arrayList.get(position).getStatus()!=null && arrayList.get(position).getStatus().equalsIgnoreCase(Constants.GoCoder.ONLINE)){
            holder.binding.container.setBackgroundColor(context.getResources().getColor(R.color.LavenderBlush));
        }


        if (holder.binding.root.isOpened()){
            holder.binding.root.close(true);
        }

        holder.binding.name.setText(arrayList.get(position).getTitle());
        holder.binding.doctorName.setText(arrayList.get(position).getName());
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
            holder.binding.deleteButton.setOnClickListener(view -> listener.onItemDelete(position,arrayList.get(position)));
            holder.binding.shareButton.setOnClickListener(v -> {
                Broadcasts broadcasts=arrayList.get(position);
                String link=Constants.DreamFactory.WEB_SHARE_URL_SCOTTISH + arrayList.get(position).getBroadcast();
                if (broadcasts.isOffline()){
                 link=link+Constants.DreamFactory.TYPE_offline;
                }else {
                    link=link+Constants.DreamFactory.TYPE_RECORDED;
                }
                Utility.share(context,link,broadcasts.getJobDes());
            });
        holder.binding.container.setOnClickListener(v -> {
            Utility.showLog("clicked");
            Broadcasts broadcasts=arrayList.get(position);
            if (broadcasts.getStatus().equalsIgnoreCase(Constants.GoCoder.ONLINE)) {
                Intent intent=new Intent(context, ViewStream.class);
                intent.putExtra("b", broadcasts);
                context.startActivity(intent);
            }
            else {
                if (broadcasts.isOffline()) {
                    Utility.makeFilePublic(context, broadcasts,Constants.S3Constants.OFFLINE_VIDEO_FOLDER + "/" + broadcasts.getBroadcast() + OfflineStreamActivity.EXT);
                } else {
                    Utility.makeFilePublic(context,broadcasts, Constants.S3Constants.RECORDED_VIDEO_FOLDER + "/" + broadcasts.getBroadcast() + OfflineStreamActivity.EXT);
                }
            }
        });
    }



    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public ArrayList<Broadcasts> getData() {
        return arrayList;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ItemMyBroadcastsBinding binding;

        private ViewHolder(ItemMyBroadcastsBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
        }
    }

}

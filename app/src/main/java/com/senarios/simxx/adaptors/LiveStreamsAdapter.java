package com.senarios.simxx.adaptors;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.hdev.common.Constants;
import com.hdev.common.datamodels.Tags;
import com.hdev.common.datamodels.UserType;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerUtils;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.databinding.CardViewLiveStreamsBinding;
import com.hdev.common.datamodels.Broadcasts;
import com.hdev.common.datamodels.JobCandidates;
import com.senarios.simxx.fragments.homefragments.BroadcastsFragment;
import com.senarios.simxx.viewmodels.SharedVM;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LiveStreamsAdapter extends RecyclerView.Adapter<LiveStreamsAdapter.ViewHolder> {
    private ArrayList<Broadcasts> arrayList = new ArrayList<>();
    private Context context;
    private SharedVM sharedVM;
    private RecyclerViewCallback listener;
    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();
    private final boolean canApply;


    public LiveStreamsAdapter(Context context, RecyclerViewCallback listener, SharedVM sharedVM, Boolean canApply) {
        this.context = context;
        this.listener = listener;
        this.sharedVM = sharedVM;
        this.canApply = canApply;
    }


    @NonNull
    @Override
    public LiveStreamsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_view_live_streams, parent, false);
        CardViewLiveStreamsBinding binding = DataBindingUtil.bind(view);
        return new ViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull LiveStreamsAdapter.ViewHolder holder, int position) {
        Broadcasts broadcast = arrayList.get(position);
        viewBinderHelper.bind(holder.binding.root, broadcast.getBroadcast());
        viewBinderHelper.setOpenOnlyOne(true);
        holder.binding.root.setLockDrag(true);

        holder.binding.group.setVisibility(View.GONE);

        holder.binding.root.close(true);
        if (broadcast.getTags().size() > 0) {
            List<String> newTags = getAddedTags(broadcast.getTags());
            holder.binding.tagsView.setData(newTags);
        }
        broadcast.setPosition(position);
        if (broadcast.getStatus() != null && broadcast.getStatus().equalsIgnoreCase(Constants.GoCoder.ONLINE)) {
            holder.binding.container.setBackgroundColor(context.getResources().getColor(R.color.LavenderBlush));
        } else {
            holder.binding.container.setBackgroundColor(context.getResources().getColor(R.color.white));
        }
        if (broadcast.getUsers() != null && broadcast.getUsers().getSkills() != null) {
            holder.binding.userType.setText("" + broadcast.getUsers().getSkills().replace("_", " "));
        }
        holder.binding.name.setText(broadcast.getTitle());
//        holder.binding.rateOfCall.setText(broadcast.getUsers().getRate());
        if (broadcast.getUsers() != null) holder.binding.doctorName.setText("Contact me");
        holder.binding.jobDes.setText(broadcast.getJobDes());

        String thumbnail = Constants.DreamFactory.GET_IMAGE_URL + broadcast.getBroadcast() + ".png";
        Utility.showLog("thumbnail " + thumbnail);

        if (broadcast.getVideourl()!=null&&broadcast.getVideourl().startsWith("https://youtu")) {
            String fullsize_path_img = "http://img.youtube.com/vi/"+getYouTubeId(broadcast.getVideourl())+"/0.jpg";
//            holder.binding.ytlinear.setVisibility(View.VISIBLE);
            Glide.with(context).load(fullsize_path_img).optionalCenterCrop()
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .placeholder(R.drawable.h2pay2)
                    .error(R.drawable.h2pay2)
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
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
            Glide.with(context).load(thumbnail).optionalCenterCrop()
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .placeholder(R.drawable.h2pay2)
                    .error(R.drawable.h2pay2)
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
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


        Glide.with(context).load(Constants.DreamFactory.GET_IMAGE_URL + broadcast.getUsername() + ".png")
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.h2pay2)
                .into(holder.binding.circleProfilePicture);

        if (broadcast.getViewers() != null) {
//            holder.binding.viewerGroup.setVisibility(View.VISIBLE);
//            holder.binding.viewersNumber.setText("" + broadcast.getViewers());
            holder.binding.viewersNumbers.setVisibility(View.VISIBLE);
            holder.binding.viewersNumbers.setText("" + broadcast.getViewers());
        }

        holder.binding.container.setOnClickListener(view -> listener.onItemClick(position, broadcast));
        holder.binding.circleProfilePicture.setOnClickListener(view -> listener.onItemPictureClick(position, broadcast));
        holder.binding.doctorName.setOnClickListener(view -> listener.onItemPictureClick(position, broadcast));
        if (broadcast.getUsername().equals(sharedVM.getLoggedUser().getUsername())) {
            holder.binding.group.setVisibility(View.GONE);
            holder.binding.swipeToapply.setVisibility(View.GONE);
            holder.binding.root.setLockDrag(true);
            return;
        }

//        if (sharedVM.getLoggedUser().getSkills().equalsIgnoreCase(UserType.RemoteWorker.toString())) {
        if(canApply){
            if (sharedVM.getLoggedUser().getSkills()!=null) {
                if (!Utility.normalizeStringEqual(broadcast.getUsername(), sharedVM.getLoggedUser().getUsername())) {
                    if (broadcast.getJobCandidates() != null && broadcast.getJobCandidates().size() > 0) {
                        if (!broadcast.getJobCandidates().contains(new JobCandidates().setUsername(sharedVM.getLoggedUser().getUsername()))) {
                            holder.binding.group.setVisibility(View.GONE);
                            holder.binding.swipeToapply.setVisibility(View.VISIBLE);
                            holder.binding.root.setLockDrag(false);
                        }
                    } else {
                        holder.binding.group.setVisibility(View.GONE);
                        holder.binding.swipeToapply.setVisibility(View.VISIBLE);
                        holder.binding.root.setLockDrag(false);
                    }
                } else {
                    holder.binding.group.setVisibility(View.GONE);
                    holder.binding.swipeToapply.setVisibility(View.GONE);
                    holder.binding.root.setLockDrag(true);
                }
            } else {
                holder.binding.group.setVisibility(View.GONE);
                holder.binding.swipeToapply.setVisibility(View.GONE);
                holder.binding.root.setLockDrag(false);
            }
        } else {
            holder.binding.group.setVisibility(View.GONE);
            holder.binding.swipeToapply.setVisibility(View.GONE);
            holder.binding.root.setLockDrag(true);
        }

        if (broadcast.getJobSiteLink() != null && !broadcast.getJobSiteLink().isEmpty()) {
            holder.binding.JobButton.setText("Apply on\njob site");
        } else {
            holder.binding.JobButton.setText("Apply\nvideo");
        }
        holder.binding.JobButton.setOnClickListener(view -> {
            holder.binding.buttonJob.startAnimation();
            listener.onItemButtonClick(position, broadcast);


//            if (sharedVM.getLoggedUser().getVideocv()!=null ) {
//            Utility.getAlertDialoge(context, "User Action Required", "Do you really want to apply on this job?")
//                    .setPositiveButton("Yes", (dialog, which) -> {
//                        holder.binding.buttonJob.startAnimation();
//                        listener.onItemButtonClick(position, broadcast);
//                    })
//                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();
//            }
//            else{
//                Utility.getAlertDialoge(context,"User Action Required","Please upload a Video CV first by going to Edit Profile")
//                        .setPositiveButton("Upload CV", (dialog, which) -> listener.onItemUserAction())
//                        .setNegativeButton("Naah", (dialog, which) -> dialog.dismiss()).show();
//            }
        });


    }

    private List<String> getAddedTags(List<Tags> tags) {
        List<String> tagList = new ArrayList<>();
        for (Tags child : tags) {
            if (!tagList.contains(child.getTag().trim()))
                tagList.add(child.getTag().trim());
        }
        return tagList;
    }

    public void setData(List<Broadcasts> broadcasts) {
        this.arrayList.clear();
        this.arrayList.addAll(broadcasts);
        notifyDataSetChanged();
    }

    public List<Broadcasts> getData() {
        return arrayList;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public void updateSearchList(List<Broadcasts> files) {
        arrayList = new ArrayList<>();
        arrayList.addAll(files);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private CardViewLiveStreamsBinding binding;


        private ViewHolder(CardViewLiveStreamsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
    private String getYouTubeId(String youTubeUrl) {
        String pattern = "(?<=youtu.be/|watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(youTubeUrl);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return "error";
        }
    }
}

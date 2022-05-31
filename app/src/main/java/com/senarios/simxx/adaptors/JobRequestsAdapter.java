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
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.hdev.common.Constants;
import com.hdev.common.datamodels.JobCandidates;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.databinding.ItemJobAppliedBinding;
import com.senarios.simxx.viewmodels.SharedVM;

import java.util.ArrayList;
import java.util.List;

import br.com.simplepass.loadingbutton.presentation.State;


public class JobRequestsAdapter extends RecyclerView.Adapter<JobRequestsAdapter.ViewHolder> {
    private ArrayList<JobCandidates> arrayList;
    private Context context;
    private SharedVM sharedVM;
    private RecyclerViewCallback listener;


    public JobRequestsAdapter(ArrayList<JobCandidates> arrayList, Context context, RecyclerViewCallback listener) {
        this.arrayList = arrayList;
        this.context = context;
        this.listener= listener;
    }


    @NonNull
    @Override
    public JobRequestsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_job_applied,parent,false);
        ItemJobAppliedBinding binding= DataBindingUtil.bind(view);
        return new ViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull JobRequestsAdapter.ViewHolder holder, int position) {
        holder.binding.setModel(arrayList.get(position));
       if (holder.binding.buttonJob.getState().equals(State.PROGRESS)){
           holder.binding.buttonJob.revertAnimation();
           holder.binding.buttonJob.setBackground(context.getDrawable(R.drawable.block_user_button));
       }

        if (arrayList.get(position).getBroadcasts()!=null&&arrayList.get(position).getBroadcasts().getVideourl()!=null&&arrayList.get(position).getBroadcasts().getVideourl().startsWith("https://youtu")) {
            String fullsize_path_img = "https://img.youtube.com/vi/"+Utility.getYouTubeId(arrayList.get(position).getBroadcasts().getVideourl())+"/0.jpg";
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

        } else if (arrayList.get(position).getBroadcasts()!=null&&arrayList.get(position).getBroadcasts().getImglink()!=null){
            Glide.with(context).load(Constants.DreamFactory.GET_IMAGE_URL+arrayList.get(position).getBroadcasts().getImglink()+".png")
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

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public List<JobCandidates> getData(){
        return arrayList;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public void reset() {
        arrayList.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
      private  ItemJobAppliedBinding binding;


        private ViewHolder(ItemJobAppliedBinding binding) {
            super(binding.getRoot());
            binding.circleProfilePicture.setOnClickListener(this);
            binding.container.setOnClickListener(this);
            binding.buttonJob.setOnClickListener(this);
            binding.doctorName.setOnClickListener(this);
            this.binding=binding;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.button_job:
                    Utility.getAlertDialoge(context,"User Action Required","Do you really want to delete this job request?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                binding.buttonJob.startAnimation();
                                listener.onItemButtonClick(getAdapterPosition(),arrayList.get(getAdapterPosition()));
                            })
                            .setNegativeButton("No", (dialog, which) -> dialog.dismiss()).show();

                    break;
                case R.id.container:

                    listener.onItemClick(getAdapterPosition(),arrayList.get(getAdapterPosition()));
                    break;
                case R.id.doctor_name:
                    listener.onItemPictureClick(getAdapterPosition(),arrayList.get(getAdapterPosition()));
                    break;

            }

        }
    }
}

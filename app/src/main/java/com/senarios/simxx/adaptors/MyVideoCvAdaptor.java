package com.senarios.simxx.adaptors;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hdev.common.datamodels.JobCandidates;
import com.hdev.common.datamodels.VideoCv;
import com.senarios.simxx.R;
import com.senarios.simxx.activities.ShowPicCvActivity;
import com.senarios.simxx.databinding.ItemJobCandidatesBinding;
import com.senarios.simxx.databinding.ItemMyVideoCvBinding;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyVideoCvAdaptor extends RecyclerView.Adapter<MyVideoCvAdaptor.ViewHolder> {
    private Context context;
    private List<VideoCv> arraylist=new ArrayList<>();
    private RecyclerViewCallback listener;
    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();
    boolean forApply=false;
    FirebaseDatabase database;

    public MyVideoCvAdaptor(Context context, List<VideoCv> arraylist, RecyclerViewCallback listener) {
        this.context = context;
        this.arraylist = arraylist;
        this.listener = listener;
    }
    public MyVideoCvAdaptor(Context context, List<VideoCv> arraylist, RecyclerViewCallback listener,boolean forApply) {
        this.context = context;
        this.arraylist = arraylist;
        this.listener = listener;
        this.forApply=forApply;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_my_video_cv,parent,false);
        ItemMyVideoCvBinding binding= DataBindingUtil.bind(view);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (holder.binding.root.isOpened()){
            holder.binding.root.close(true);
        }
        holder.binding.setModel(arraylist.get(position));
        viewBinderHelper.bind(holder.binding.root, arraylist.get(position).getId()+"");
        viewBinderHelper.setOpenOnlyOne(true);
        if (arraylist.get(position).getTitle().startsWith("Pic")) {
            holder.binding.playVideo.setVisibility(View.GONE);
            database = FirebaseDatabase.getInstance();
            database.getReference().child("images/"+arraylist.get(position).getVideocv())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String imagee = snapshot.getValue(String.class);
                            Glide.with(context.getApplicationContext()).load(imagee).into(holder.binding.imageView);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


        }
        holder.binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                listener.onPicClick(arraylist.get(position).getVideocv());
                if (arraylist.get(position).getTitle().startsWith("Pic")) {
                    database = FirebaseDatabase.getInstance();
                    database.getReference().child("images/"+arraylist.get(position).getVideocv())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String imagee = snapshot.getValue(String.class);
                                    Intent intent = new Intent(context, ShowPicCvActivity.class);
                                    intent.putExtra("picPathh", imagee);
                                    context.startActivity(intent);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });



                }
            }
        });
//        holder.itemView.setOnClickListener(v->{
//            if (arraylist.get(position).getTitle().startsWith("Pic")) {
//                Toast.makeText(context, arraylist.get(position).getTitle()+"dd", Toast.LENGTH_SHORT).show();
//                listener.onItemPicCVCLick(position,arraylist.get(position).getPath());
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return arraylist.size();
    }

    public List<VideoCv> getData() {
        return arraylist;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ItemMyVideoCvBinding binding;

        public ViewHolder(@NonNull ItemMyVideoCvBinding binding) {
            super(binding.getRoot());
            this.binding=binding;
            binding.delete.setOnClickListener(this);
            binding.imageView.setOnClickListener(this);
            binding.container.setOnClickListener(this);
            binding.applyChoosenVideo.setOnClickListener(this);
            binding.playVideo.setOnClickListener(this);
            if (forApply){
                binding.applyChoosenVideo.setVisibility(View.VISIBLE);
            }else binding.applyChoosenVideo.setVisibility(View.GONE);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.apply_choosen_video:
                    listener.onItemClick(getAdapterPosition(),arraylist.get(getAdapterPosition()));
//                    binding.applyChoosenVideo.setVisibility(View.GONE);
                    break;
                case R.id.play_video:
                    listener.onItemPictureClick(getAdapterPosition(),arraylist.get(getAdapterPosition()));
                    break;
                case R.id.delete:
                    listener.onItemDelete(getAdapterPosition(),arraylist.get(getAdapterPosition()));
                    break;

            }

        }
    }
}

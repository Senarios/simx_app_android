package com.senarios.simxx.adaptors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hdev.common.Constants;
import com.senarios.simxx.R;
import com.hdev.common.datamodels.Blocked;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlockListAdapter extends RecyclerView.Adapter<BlockListAdapter.ViewHolder> {
    private ArrayList<Blocked> arrayList;
    private Context context;
    private RecyclerViewCallback listener;

    public BlockListAdapter(ArrayList<Blocked> arrayList , Context context, RecyclerViewCallback listener) {
        this.arrayList=arrayList;
        this.context = context;
        this.listener=listener;
    }

    @NonNull
    @Override
    public BlockListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_view_blocked,parent,false);
        return new ViewHolder(view);
    }



    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull BlockListAdapter.ViewHolder holder, int position) {
        holder.name.setText(arrayList.get(position).getBlockedname());
        Glide.with(context).load(Constants.DreamFactory.GET_IMAGE_URL+arrayList.get(position).getBlockedid()+".png")
                .into(holder.circle_profile_picture);



        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(position,arrayList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public void reset(){
        arrayList.clear();
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private CircleImageView circle_profile_picture;
        private ConstraintLayout container;



        private ViewHolder(View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.Name);
            circle_profile_picture=itemView.findViewById(R.id.image);
            container=itemView.findViewById(R.id.cardView);





        }
    }
}

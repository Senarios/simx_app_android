package com.senarios.simxx.adaptors;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hdev.common.Constants;
import com.senarios.simxx.R;
import com.hdev.common.datamodels.Comments;

import java.util.List;
import java.util.Random;

public class CommentsAdaptor extends RecyclerView.Adapter<CommentsAdaptor.holder> {
    private List<Comments>comments;
    private Context context;

    public CommentsAdaptor(List<Comments> comments, Context context) {
        this.comments = comments;
        this.context = context;
    }

    @NonNull
    @Override
    public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.comments_view,parent,false);
        return new holder(view);


    }

    @Override
    public void onBindViewHolder(@NonNull holder holder, @SuppressLint("RecyclerView") int position) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            holder.image.setForeground(new ColorDrawable(getRandomColor()));
        }
        holder.message.setText(comments.get(position).getText()+"");
        Glide.with(context).load(Constants.DreamFactory.GET_IMAGE_URL+comments.get(position).getEmail()+".png")
                .into(holder.image);
            holder.name.setText(comments.get(position).getName()+"");

        holder.timer=new CountDownTimer(10000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                try{
                    if (comments.size()>0){
                        comments.remove(position);
                        notifyItemRemoved(position);
                    }
                }
                catch (Exception e){
                    Log.v("!comment", e.getMessage());
                    notifyDataSetChanged();
                }

            }
        }    ;
        holder.timer.start();


    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    class holder extends RecyclerView.ViewHolder{
        private TextView name,message;
        private ImageView image;
        private CountDownTimer timer;
        private ConstraintLayout container;

    holder(@NonNull View itemView) {
        super(itemView);
        message=itemView.findViewById(R.id.doctor_name);
        name=itemView.findViewById(R.id.name);
        image=itemView.findViewById(R.id.image);
        container=itemView.findViewById(R.id.container);
    }
}

private Integer getRandomColor(){
    Random rnd = new Random();
    return adjustAlpha(Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)), 0.5f);
}
    private int adjustAlpha(@ColorInt int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }
}

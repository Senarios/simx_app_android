package com.senarios.simxx;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.view.View;
import android.view.autofill.AutofillId;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.hdev.common.Constants;
import com.hdev.common.customlayout.ImageViewWithLoading;
import com.hdev.common.datamodels.Broadcasts;
import com.hdev.common.datamodels.JobCandidates;
import com.hdev.common.datamodels.Users;

import org.w3c.dom.Text;

public class BindingAdaptors {

    @BindingAdapter("loadUserImage")
    public static void loadUserImage(View view,String photo){
        Utility.showLog(Constants.DreamFactory.GET_IMAGE_URL + photo  + ".png");
        Glide.with(view.getContext())
                .asBitmap()
                .load(Constants.DreamFactory.GET_IMAGE_URL + photo  + ".png")
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(R.drawable.h2pay2)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        ((ImageView) view).setImageBitmap(resource);
                    }
                });
//        Glide.with(view.getContext())
//                .load(Constants.DreamFactory.GET_IMAGE_URL + photo  + ".png")
//                .skipMemoryCache(true)
//                .placeholder(R.drawable.h2pay2)
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .into((ImageView) view);
    }

    @BindingAdapter("loadImageWithProgressBar")
    public static void loadImageWithProgressBar(View view,String photo){
        Utility.showLog(Constants.DreamFactory.GET_IMAGE_URL + photo  + ".png");
        if (view!=null && !photo.isEmpty()){
          RequestBuilder<Drawable> requestBuilder =  Glide.with(view.getContext())
                    .load(photo+".png")
                    .error(R.drawable.h2pay2)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    });
          if (view instanceof ImageView){
                requestBuilder.into((ImageView) view  );
          }
          else{
              view.setVisibility(View.GONE);
          }


        }
    }

    @BindingAdapter("buttonClick")
    public static void buttonClick(View view,String photo){

    }


    @BindingAdapter({"setUserProfiletype","type"})
    public static void setUserProfiletype(View view, Users user , String type){
        if (user.getSkills().equalsIgnoreCase(Constants.VIEWER)){
            view.setVisibility(View.GONE);
        }
        else{
            if (type.equalsIgnoreCase("1")){
                ((TextView)view).setText(user.getRate());
            }

        }

    }


    @BindingAdapter("setbackground")
    public static void setbackground(View view, Broadcasts broadcast){
        try {
            if (broadcast.getStatus()!=null && broadcast.getStatus().equalsIgnoreCase(Constants.GoCoder.ONLINE)){
            view.setBackgroundColor(view.getContext().getResources().getColor(R.color.LavenderBlush));
            }
        }
        catch (Exception e){
            Utility.showELog(e);
        }
    }

    @BindingAdapter("settext")
    public static void settext(View view,String text) {
        if (view != null && text != null) {
            ((TextView) view).setText(text.replace("_", " "));
        }
    }

}

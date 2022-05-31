package com.hdev.common.customlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.hdev.common.R;
import com.hdev.common.databinding.LayoutImageviewPrgoressbarBinding;

public class ImageViewWithLoading extends FrameLayout {
    private LayoutImageviewPrgoressbarBinding binding;

    public ImageViewWithLoading(@NonNull Context context) {
        super(context);
    }

    public ImageViewWithLoading(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs,-1);
    }

    public ImageViewWithLoading(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs,defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        binding=LayoutImageviewPrgoressbarBinding.inflate(LayoutInflater.from(context),this);
        TypedArray array=context.obtainStyledAttributes(attrs,R.styleable.ImageViewWithLoading);
        if (array.length()>0){
            String image=array.getString(R.styleable.ImageViewWithLoading_uri);
            int error=array.getInteger(R.styleable.ImageViewWithLoading_error,-1);
//            loadImage(context,image,error);

        }



        array.recycle();
    }

    public ProgressBar getProgressBar(){
      return  binding.progressBar;
    }

    public ImageView getImageView(){
        return  binding.image;
    }

    public void loadImage(String image,int error){
        if (image!=null && !image.isEmpty()){
            Glide.with(getContext())
                    .load(image)
                    .error(error)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .addListener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            binding.progressBar.setVisibility(GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            binding.progressBar.setVisibility(GONE);
                            return false;
                        }
                    })
                    .into(binding
                    .image);

        }
    }


}

package com.senarios.simxx.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.hdev.common.Constants;
import com.senarios.simxx.R;

public class ShowPicCvActivity extends AppCompatActivity {

    ImageView pic;
    Button ok;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_pic_cv);
        String picPath = getIntent().getStringExtra("picPathh");
        pic = findViewById(R.id.piccv);
        ok = findViewById(R.id.btnok);
        Glide.with(ShowPicCvActivity.this).load(picPath)
                .into(pic);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
//                startActivity(new Intent(ShowPicCvActivity.this,MyVideoCVActivity.class));
            }
        });
    }
}
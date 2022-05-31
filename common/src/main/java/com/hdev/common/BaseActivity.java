package com.hdev.common;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.hdev.common.datamodels.Broadcasts;

import java.security.Permission;
import java.util.stream.Stream;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getData(getIntent());
        setBinding();
        Initialize();

    }


    public void keepScreenOn(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeInitialize();
    }

    public void getData(Intent intent) {
    }

    protected abstract void setBinding();

    protected abstract void Initialize();

    protected abstract void resumeInitialize();




}

package com.streamaxia.example;


import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.databinding.DataBindingUtil;

import com.hdev.common.BaseActivity;
import com.streamaxia.player.StreamaxiaPlayer;
import com.streamaxia.player.listener.StreamaxiaPlayerState;

import com.streamaxia.example.databinding.ActivityStreamaxiaPlayerBinding;


public class StreamaxiaPlayerActivity extends BaseActivity implements StreamaxiaPlayerState , View.OnClickListener {
    public static final String URI = "streamlink";
    private ActivityStreamaxiaPlayerBinding binding;
    private Uri uri;

    private StreamaxiaPlayer mStreamaxiaPlayer = new StreamaxiaPlayer();

    private int STREAM_TYPE = 0;

    Runnable hide = new Runnable() {
        @Override
        public void run() {
            binding.play.setVisibility(View.GONE);
        }
    };

    @Override
    protected void setBinding() {
        binding= DataBindingUtil.setContentView(this,R.layout.activity_streamaxia_player);
    }

    @Override
    protected void Initialize() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getExtras();
        //setting the initial tags of the player
        binding.play.setTag("play");
        binding.mute.setTag("mute");
        binding.small.setTag("small");
        binding.progressBar.setVisibility(View.GONE);
        initRTMPExoPlayer();

        binding.play.setOnClickListener(this);
        binding.mute.setOnClickListener(this);
        binding.small.setOnClickListener(this);

    }

    @Override
    protected void resumeInitialize() {

    }

    private void getExtras() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            uri = Uri.parse(extras.getString(StreamaxiaPlayerActivity.URI));
        }
    }


    public void setPlayBtn() {
        binding.play.postDelayed(hide, 1000);
        if (binding.play.getTag().equals("play")) {
            mStreamaxiaPlayer.play(uri, STREAM_TYPE);
            binding.surfaceView.setBackgroundColor(Color.TRANSPARENT);
            binding.progressBar.setVisibility(View.GONE);
            binding.play.setTag("pause");
            binding.play.setImageResource(R.drawable.pause);
            setAspectRatioFrameLayoutOnClick();
        } else {

            mStreamaxiaPlayer.pause();
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.play.setTag("play");
            binding.play.setImageResource(R.drawable.play);
        }
    }

    public void setMute() {
        if (binding.mute.getTag().equals("mute")) {
            mStreamaxiaPlayer.setMute();
            binding.mute.setTag("muted");
            binding.mute.setImageResource(R.drawable.muted);
        } else {
            mStreamaxiaPlayer.setMute();
            binding.mute.setTag("mute");
            binding.mute.setImageResource(R.drawable.mute);
        }
    }


    public void setScreenSize() {
        if (binding.small.getTag().equals("small")) {
            mStreamaxiaPlayer.setVideoSize(300, 300);
            binding.small.setTag("big");
            binding.small.setImageResource(R.drawable.big);
        } else {
            mStreamaxiaPlayer.setVideoSize(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            binding.small.setTag("small");
            binding.small.setImageResource(R.drawable.small);
        }
    }

    private void setAspectRatioFrameLayoutOnClick() {
        binding.videoFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.play.setVisibility(View.VISIBLE);
                binding.play.postDelayed(hide, 1000);
            }
        });
    }

    private void initRTMPExoPlayer() {
        mStreamaxiaPlayer.initStreamaxiaPlayer(binding.surfaceView, binding.videoFrame,
                binding.playerStateView, this, this, uri);
    }

    @Override
    public void stateENDED() {
        binding.progressBar.setVisibility(View.GONE);
        binding.play.setImageResource(R.drawable.play);

    }

    @Override
    public void stateBUFFERING() {
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void stateIDLE() {
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void statePREPARING() {
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void stateREADY() {
        binding.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void stateUNKNOWN() {
        binding.progressBar.setVisibility(View.VISIBLE);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mStreamaxiaPlayer.stop();
    }

    @Override
    public void onClick(View v) {
        if (v==binding.small){
            setScreenSize();
        }
        else if(v==binding.mute){
            setMute();
        }
        else if(v==binding.play){
            setPlayBtn();
        }

    }
}
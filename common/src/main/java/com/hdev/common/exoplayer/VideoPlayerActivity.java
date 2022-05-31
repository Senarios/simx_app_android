package com.hdev.common.exoplayer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.snackbar.Snackbar;
import com.hdev.common.BaseActivity;
import com.hdev.common.CommonUtils;
import com.hdev.common.R;
import com.hdev.common.databinding.ActivityVideoPlayerBinding;

import java.io.IOException;

public class VideoPlayerActivity extends BaseActivity implements Player.EventListener {
    private ActivityVideoPlayerBinding binding;
    private SimpleExoPlayer player;
    private String URL=null;
    public static final String URL_DATA_STRING="link of video";


    public static Intent newInstance(Context context,String URL) throws Exception {
        if (URL.isEmpty()){
            throw new Exception("URL of video is empty!");
        }
        return new Intent(context,VideoPlayerActivity.class).putExtra(URL_DATA_STRING,URL);
    }


    @Override
    public void getData(Intent intent) {
        if (intent!=null){
            URL=intent.getStringExtra(URL_DATA_STRING);
        }

    }

    @Override
    protected void setBinding() {
        binding= DataBindingUtil.setContentView(this, R.layout.activity_video_player);
    }

    @Override
    protected void Initialize() {
        keepScreenOn();


    }

    @Override
    protected void resumeInitialize() {
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(this).build();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
        TrackSelector trackSelector = new DefaultTrackSelector(this,videoTrackSelectionFactory);
        player = new SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).setBandwidthMeter(bandwidthMeter).build();

        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "dev"));
// This is the MediaSource representing the media to be played.
        MediaSource videoSource =
                new ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.parse(URL));

        binding.videoPlayer.setPlayer(player);
        player.addListener(this);
        player.setPlayWhenReady(true);
        player.prepare(videoSource);

    }


    @Override
    public void onPlayerError(ExoPlaybackException error) {

        Log.v("OTHER",URL);
        Snackbar.make(binding.getRoot(),"Unable to play this stream",3000).show();
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case SimpleExoPlayer.STATE_READY:
                binding.progressBar.setVisibility(View.GONE);
                break;
            case SimpleExoPlayer.STATE_BUFFERING:
                binding.progressBar.setVisibility(View.VISIBLE);
                break;


            case Player.STATE_ENDED:
                break;
            case Player.STATE_IDLE:
                break;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player!=null){
            player.stop(true);
            player.release();

        }
    }
}

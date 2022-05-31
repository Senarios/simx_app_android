package com.senarios.simxx.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.senarios.simxx.R;
import com.senarios.simxx.databinding.ActivityRtspPlayerBinding;
import com.wowza.gocoder.sdk.support.wse.StreamPlayer;

import java.util.Arrays;

public class RtspPlayerActivity extends AppCompatActivity {
    private ActivityRtspPlayerBinding binding;
    public static final String VID="wowzavideo";
    private PlayerView playerView;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= DataBindingUtil.setContentView(this,R.layout.activity_rtsp_player);
        initplayer();




    }

    private void initplayer() {
        if (getIntent()!=null){
            String path=getIntent().getStringExtra(VID);
            if (path!=null){
                prepare(path);
            }

        }

    }

    private void prepare(String path){
        // Create Simple ExoPlayer
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(this).build();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
        TrackSelector trackSelector = new DefaultTrackSelector(this,videoTrackSelectionFactory);
        SimpleExoPlayer player = new SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).setBandwidthMeter(bandwidthMeter).build();

         playerView = binding.videoView;

        playerView.setPlayer(player);

        // Create RTMP Data Source
        RtmpDataSourceFactory rtmpDataSourceFactory = new RtmpDataSourceFactory();


        MediaSource videoSource = new ExtractorMediaSource
                .Factory(rtmpDataSourceFactory)
                .createMediaSource(Uri.parse(path));

        player.prepare(videoSource);
        player.setPlayWhenReady(true);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (playerView!=null && playerView.getPlayer()!=null){
            playerView.getPlayer().stop();
            playerView.getPlayer().release();


        }
    }
}

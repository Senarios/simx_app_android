package com.senarios.simxx.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

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
import com.google.gson.JsonObject;
import com.hdev.common.BaseActivity;
import com.hdev.common.Constants;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.adaptors.JobCandidatesAdaptor;
import com.senarios.simxx.adaptors.RecyclerViewCallback;
import com.senarios.simxx.databinding.ActivityJobVideoPlayerBinding;
import com.hdev.common.datamodels.Broadcasts;
import com.hdev.common.datamodels.JobCandidates;
import com.hdev.common.datamodels.ResponseJobCandidate;
import com.hdev.common.datamodels.Users;
import com.hdev.common.retrofit.ApiResponse;
import com.hdev.common.retrofit.NetworkCall;
import com.senarios.simxx.fragments.JobCandidatesFragment;
import com.senarios.simxx.fragments.ShortlistJCFragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Response;

public class JobVideoPlayerActivity extends BaseActivity implements Player.EventListener  {
    private ActivityJobVideoPlayerBinding binding;
    private SimpleExoPlayer player;
    private String URL=null;
    private Broadcasts broadcast;
    private JobCandidatesAdaptor adaptor;
    public static final String URL_DATA_STRING="link of video";
    public static final String BROADCAST="broadcast";


    public static Intent newInstance(Context context, Broadcasts broadcast, String URL) throws Exception {
        if (URL.isEmpty()){
            throw new Exception("URL of video is empty!");
        }
        return new Intent(context, JobVideoPlayerActivity.class).putExtra(URL_DATA_STRING,URL).putExtra(BROADCAST,broadcast);
    }


    @Override
    public void getData(Intent intent) {
        if (intent!=null){
            URL=intent.getStringExtra(URL_DATA_STRING);
            broadcast= (Broadcasts) intent.getSerializableExtra(BROADCAST);
        }
    }

    @Override
    protected void setBinding() {
        binding= DataBindingUtil.setContentView(this, R.layout.activity_job_video_player);
    }

    @Override
    protected void Initialize() {
        binding.swipe.setEnabled(false);
        binding.swipe.setRefreshing(false);

        SharedPreferences editor = getSharedPreferences("hunter", MODE_PRIVATE);
        String userType = editor.getString("jobhunter1", "");
        String userName = editor.getString("username1", "");

        if (broadcast!=null&&broadcast.getVideourl() != null && !broadcast.getVideourl().isEmpty()) {
            binding.youtubePlayerView.setVisibility(View.VISIBLE);
            YouTubePlayerView youTubePlayerView = findViewById(R.id.youtube_player_view);
            getLifecycle().addObserver(youTubePlayerView);

            youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                    youTubePlayer.loadVideo(getYouTubeId(broadcast.getVideourl()), 0);
                }
            });
        } else {
            iniPlayer();
        }

        if (!userType.isEmpty()&&userType!=null&&userType.equalsIgnoreCase("Job hunter")) {
            binding.view.viewPager.setVisibility(View.GONE);
        } else if (broadcast.getUsername().equalsIgnoreCase(userName)){
            binding.view.viewPager.setVisibility(View.VISIBLE);
            binding.view.viewPager.setAdapter(new FragmentPagerItemAdapter(getSupportFragmentManager(),
                    FragmentPagerItems.with(this)
                            .add("Applicants",new JobCandidatesFragment().getClass(),getBundle(false))
                            .add("Shortlist Applicants",new ShortlistJCFragment().getClass(),getBundle(true))
                            .create()
            ));
            binding.view.tabs.setViewPager(binding.view.viewPager);
        }



    }
    private String getYouTubeId(String youTubeUrl) {
        String pattern = "(?<=youtu.be/|watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(youTubeUrl);
        if (matcher.find()) {
            return matcher.group();
        } else {
            return "error";
        }
    }
    private void iniPlayer(){
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(this).build();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
        TrackSelector trackSelector = new DefaultTrackSelector(this,videoTrackSelectionFactory);
        player = new SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).setBandwidthMeter(bandwidthMeter).build();

        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "dev"));
        // This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(URL));

        binding.videoPlayer.setPlayer(player);
        player.addListener(this);
        player.setPlayWhenReady(true);
        player.prepare(videoSource);
    }

    @Override
    protected void resumeInitialize() {
        if (player!=null){
            player.setPlayWhenReady(true);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player!=null){
            player.setPlayWhenReady(false);
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {


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

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player!=null){
            player.stop(true);
            player.release();

        }
    }

    private Bundle getBundle(boolean isShortList){
        Bundle bundle=new Bundle();
        bundle.putBoolean(ShortlistJCFragment.SHORTILIST,isShortList);
        bundle.putSerializable(ShortlistJCFragment.DATA,broadcast);
        return bundle;
    }



}

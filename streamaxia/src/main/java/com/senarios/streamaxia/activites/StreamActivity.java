package com.senarios.streamaxia.activites;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.google.gson.JsonObject;
import com.hdev.common.BaseActivity;
import com.hdev.common.CommonUtils;
import com.hdev.common.Constants;
import com.hdev.common.datamodels.Broadcasts;
import com.hdev.common.datamodels.HttpMethod;
import com.hdev.common.retrofit.ApiResponse;
import com.hdev.common.retrofit.NetworkCall;
import com.senarios.streamaxia.R;
import com.senarios.streamaxia.databinding.ActivityStreamBinding;
import com.streamaxia.android.StreamaxiaPublisher;
import com.streamaxia.android.handlers.EncoderHandler;
import com.streamaxia.android.handlers.RecordHandler;
import com.streamaxia.android.handlers.RtmpHandler;
import com.streamaxia.android.utils.Size;
import com.hdev.common.datamodels.StreamStatus;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;

import retrofit2.Response;


public class StreamActivity extends BaseActivity implements View.OnClickListener,ApiResponse,Chronometer.OnChronometerTickListener,RtmpHandler.RtmpListener, RecordHandler.RecordListener,
        EncoderHandler.EncodeListener {
    // Set default values for the streamer
    private final static int bitrate = 500;
    private final static int width = 720;
    private final static int height = 1280;
    public static final String DATA="required data";
    private ActivityStreamBinding binding;
    private StreamaxiaPublisher mPublisher;
    private boolean isStarted=false;
    private Broadcasts data;
    private int padding ;


    @Override
    public void getData(Intent intent) {
        data=(Broadcasts) intent.getSerializableExtra(DATA);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void setBinding() {
        binding= DataBindingUtil.setContentView(this, R.layout.activity_stream);
    }

    @Override
    protected void Initialize() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        hideStatusBar();

        binding.chronometer.setOnChronometerTickListener(this);


        mPublisher = new StreamaxiaPublisher(binding.preview, this);

        mPublisher.setEncoderHandler(new EncoderHandler(this));
        mPublisher.setRtmpHandler(new RtmpHandler(this));
        mPublisher.setRecordEventHandler(new RecordHandler(this));

        binding.preview.startCamera();

        binding.startStop.setOnClickListener(this);
        binding.switchCamera.setOnClickListener(this);

        setStreamerDefaultValues();

    }

    private int calculatePadding() {
        int seconds = Integer.parseInt(binding.chronometer.getText().toString().split(":")[1]);
        seconds =seconds+ (Integer.parseInt(binding.chronometer.getText().toString().split(":")[0])*60);
        seconds = (int) Math.round(seconds*0.1);
        binding.switchCamera.setClickable(false);
        binding.startStop.setClickable(false);
        binding.layoutEnd.getRoot().setVisibility(View.VISIBLE);
        binding.startStop.setClickable(false);
        binding.chronometer.setOnChronometerTickListener(null);
        stopChronometer();
        binding.startStop.setText("START");
        return seconds;

    }

    @Override
    public void onClick(View v) {
        if (v==binding.startStop){
            startStopStream();
        }
        else if (v==binding.switchCamera) {
            if (mPublisher != null) {
                mPublisher.switchCamera();
            }
        }
    }
    @Override
    protected void resumeInitialize() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
//            stopStreaming();
//            stopChronometer();
            binding.preview.startCamera();
//            binding.startStop.setText("START");
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.preview.stopCamera();
//        mPublisher.stopPublish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPublisher.stopPublish();
        mPublisher.stopRecord();
    }

    public void startStopStream() {
        if (binding.startStop.getText().toString().toLowerCase().equals("start")) {
            binding.startStop.setClickable(false);
            mPublisher.startPublish("rtmp://simx.tv:1935/live/" + data.getBroadcast());
            takeSnapshot();
        } else {
            if (isTime()) {
                new CountDownTimer(calculatePadding(),1000){


                    @Override
                    public void onTick(long millisUntilFinished) {
                        binding.layoutEnd.countdown.setText(""+(millisUntilFinished/1000));

                    }

                    @Override
                    public void onFinish() {
                        mPublisher.stopPublish();
                    }
                }
                .start();
            }
            else{
                Toast.makeText(this, "You cannot stop stream before 15 seconds", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private boolean isTime() {
    return Integer.parseInt(binding.chronometer.getText().toString().split(":")[0]+binding.chronometer.getText().toString().split(":")[1])>15;
    }

    private void stopStreaming() {
        mPublisher.stopPublish();
    }

    private void takeSnapshot() {
        final Handler handler = new Handler();
        handler.postDelayed(() ->
                binding.preview.takeSnapshot(image -> {
        }), 5000);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mPublisher.setScreenOrientation(newConfig.orientation);
    }

    private void hideStatusBar() {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void setStreamerDefaultValues() {
        // Set one of the available resolutions
        List<Size> sizes = mPublisher.getSupportedPictureSizes(getResources().getConfiguration().orientation);
        Size resolution = sizes.get(0);
        mPublisher.setCameraFacing(data.getCamera());
        mPublisher.setVideoOutputResolution(resolution.width, resolution.height, getResources().getConfiguration().orientation);
    }

    private void setStatusMessage(final String msg) {
        runOnUiThread(() -> binding.stateText.setText("[" + msg + "]"));
    }


    /*
    * EncoderHandler implementation
    * You can use these callbacks to get events from the streamer
    * */

    @Override
    public void onNetworkWeak() {
        CommonUtils.showLog("Network weak");
    }

    @Override
    public void onNetworkResume() {
        CommonUtils.showLog("Network Fine");
    }

    @Override
    public void onEncodeIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }


    /*
    * RecordHandler implementation
    * */

    @Override
    public void onRecordPause() {

    }

    @Override
    public void onRecordResume() {

    }

    @Override
    public void onRecordStarted(String s) {

    }

    @Override
    public void onRecordFinished(String s) {

    }

    @Override
    public void onRecordIllegalArgumentException(IllegalArgumentException e) {
        handleException(e);
    }

    @Override
    public void onRecordIOException(IOException e) {
        handleException(e);
    }

    /*
    * RTMPListener implementation
    * */

    @Override
    public void onRtmpConnecting(String s) {
        CommonUtils.showLog(s);
        setStatusMessage(s);

    }

    @Override
    public void onRtmpConnected(String s) {
        CommonUtils.showLog(s);
        setStatusMessage(s);
        isStarted=true;
        binding.startStop.setClickable(true);
        binding.startStop.setText("STOP");
        binding.chronometer.setBase(SystemClock.elapsedRealtime());
        binding.chronometer.start();
        postStatus(data.setStreamStatus(StreamStatus.CONNECTED));
    }

    @Override
    public void onRtmpVideoStreaming() {
//        CommonUtils.showLog("Video Streaming");
    }

    @Override
    public void onRtmpAudioStreaming() {
//        CommonUtils.showLog("Audio Streaming");
    }

    @Override
    public void onRtmpStopped() {
        setStatusMessage("STOPPED");
        binding.startStop.setClickable(true);
        CommonUtils.showLog("STOPPED");
        postStatus(data.setStreamStatus(StreamStatus.ENDED));

    }

    @Override
    public void onRtmpDisconnected() {
        binding.startStop.setClickable(true);
        CommonUtils.showLog("Disconnected");
        setStatusMessage("Disconnected");
    }

    @Override
    public void onRtmpVideoFpsChanged(double v) {
        CommonUtils.showLog("onRtmpVideoFpsChanged " + v);
    }

    @Override
    public void onRtmpVideoBitrateChanged(double v) {
        CommonUtils.showLog("onRtmpVideoBitrateChanged " + v);
    }

    @Override
    public void onRtmpAudioBitrateChanged(double v) {
        CommonUtils.showLog("onRtmpAudioBitrateChanged " + v);
    }

    @Override
    public void onRtmpSocketException(SocketException e) {
        binding.startStop.setClickable(true);
        handleException(e);
        CommonUtils.showELog(e);
    }

    @Override
    public void onRtmpIOException(IOException e) {
        binding.startStop.setClickable(true);
        CommonUtils.showELog(e);
        handleException(e);
    }

    @Override
    public void onRtmpIllegalArgumentException(IllegalArgumentException e) {
        CommonUtils.showELog(e);
        binding.startStop.setClickable(true);
        handleException(e);
    }

    @Override
    public void onRtmpIllegalStateException(IllegalStateException e) {
        CommonUtils.showELog(e);
        binding.startStop.setClickable(true);
        handleException(e);
    }

    @Override
    public void onRtmpAuthenticationg(String s) {
        CommonUtils.showLog(s);
    }

    private void stopChronometer() {
        binding.chronometer.setBase(SystemClock.elapsedRealtime());
        binding.chronometer.stop();
    }

    private void handleException(Exception e) {
        try {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            mPublisher.stopPublish();
            finish();
        } catch (Exception e1) {
            // Ignore
            CommonUtils.showELog(e);
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onChronometerTick(Chronometer chronometer) {
        if (chronometer.getText().toString().equalsIgnoreCase("00:10")){
            postStatus(data.setStreamStatus(StreamStatus.SECONDS));
        }
        if (chronometer.getText().toString().equalsIgnoreCase(data.getDuration())){
            startStopStream();
        }
    }

    private void postStatus(Broadcasts data){
        switch (data.getStreamStatus()){
            case STARTED:
                break;
            case SECONDS:
                postThumbnail();
                break;
            case CONNECTED:
                postBroadcast(data,false);
                break;

            case ENDED:
                postBroadcast(data,true);
                break;
        }
    }

    private void postThumbnail() {
        NetworkCall.CallAPI(this,CommonUtils.getService(Constants.DEFAULT_URL_PORT)
                .postThumbnail(data.getBroadcast()),this,false,Object.class,Constants.Endpoints.POST_LIVE_THUMBNAIL);
    }

    private void postBroadcast(Broadcasts data,boolean isupdate){
        HashMap<String, Object> map = new HashMap<>();
        map.put("resource", data);
        if (isupdate){
            data.setStatus(Constants.GoCoder.OFFLINE);
            NetworkCall.CallAPI(this, CommonUtils.getService(Constants.DreamFactory.URL).updateBroadcast(map)
                    ,this,true,Object.class,Constants.Endpoints.BROADCASTS);
        }
        else{
            NetworkCall.CallAPI(this, CommonUtils.getService(Constants.DreamFactory.URL).postBroadcast(map)
                    ,this,false,Object.class,Constants.Endpoints.BROADCASTS);
        }
    }


    @Override
    public void OnSuccess(Response<JsonObject> response, Object body, String endpoint) {
        if (endpoint.equalsIgnoreCase(Constants.Endpoints.BROADCASTS)) {
            if (response.raw().request().method().equalsIgnoreCase(HttpMethod.POST.toString())) {
                int id = 0;
                if (response.body() != null) {
                    id = response.body().get("resource").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsInt();
                }
                data.setId(id);
                data.setTags(null);
//                postTags();

            } else if (response.raw().request().method().equalsIgnoreCase(HttpMethod.PATCH.toString())) {
                finish();
            }
        }
        else{

        }

    }

    @Override
    public void OnError(Response<JsonObject> response, String endpoint) {

    }

    @Override
    public void OnException(Throwable e, String endpoint) {

    }

    @Override
    public void OnNetWorkError(String endpoint, String message) {

    }

    @Override
    public void onBackPressed() {
        if (mPublisher!=null && isStarted){
            startStopStream();
        }
        else {
            finish();
        }
    }
}

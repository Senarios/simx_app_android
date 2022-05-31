package com.senarios.simxx.services;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hdev.common.CommonUtils;
import com.novoda.merlin.Connectable;
import com.novoda.merlin.Merlin;
import com.otaliastudios.transcoder.Transcoder;
import com.otaliastudios.transcoder.TranscoderListener;
import com.otaliastudios.transcoder.strategy.DefaultVideoStrategies;
import com.otaliastudios.transcoder.strategy.DefaultVideoStrategy;
import com.hdev.common.Constants;
import com.senarios.simxx.Info;
import com.senarios.simxx.Utility;
import com.hdev.common.datamodels.Broadcasts;
import com.hdev.common.datamodels.S3UploadRequest;
import com.hdev.common.datamodels.Users;
import com.hdev.common.retrofit.ApiResponse;
import com.hdev.common.retrofit.NetworkCall;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.Future;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

import static com.hdev.common.Constants.SharedPreference.S3_REQUEST;


@Info(author = "mr-hashim",callType = Info.CallType.SERVICE,description = "Uploading file for s3")
public class AmazonS3UploadService extends BaseService implements Connectable,TransferListener , ApiResponse {
    private String PATH =null;
    private String S3FileLink=null;
    private Broadcasts broadcasts;
    private AmazonS3Client s3client;
    private String videoDestination=null;
    private S3UploadRequest s3UploadRequest=new S3UploadRequest();
    private Merlin merlin;
    private static AmazonS3UploadService mInstance=null;
    private TransferObserver observer;
    private Future<Void> transcoder=null;



    public static boolean checkRunning(){
        return mInstance !=null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance=this;
//        EventBus.getDefault().register(this);
        s3client=Utility.getS3Client(this);
//        videoDestination=getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath()+"/"+System.currentTimeMillis()+".mp4";
        videoDestination=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getPath()+"/"+System.currentTimeMillis()+".mp4";
        merlin = new Merlin.Builder().withConnectableCallbacks().build(this);
        merlin.bind();
        merlin.registerConnectable(this);
        TransferNetworkLossHandler.getInstance(this);
        startForeground(NOTIFICATION_ID,notificationBuilder("SimX","App running in background",false,true,false));
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent!=null){
            if (intent.hasExtra(S3_REQUEST)){
                Utility.getPreference(this).edit().putBoolean(Constants.SharedPreference.ISUPLOADING,true).apply();
              S3UploadRequest((S3UploadRequest) intent.getSerializableExtra(S3_REQUEST));
            }
            else if (intent.hasExtra(Constants.SharedPreference.ISUPLOADING)){
                if (intent.getBooleanExtra(Constants.SharedPreference.ISUPLOADING,false)){
                    if (transcoder!=null){
                        transcoder.cancel(true);
                    }
                    if (observer!=null){
                        Utility.getTransferUtility(this,s3client).cancel(observer.getId());
                    }
                    onErrorStopEverything();
                }
            }
        }

        return START_STICKY;
    }



    @Override
    public void onConnect() {
        if (observer==null) {
            uploadRequest();
        }
        else{
            if (Utility.isNetworkAvailable(this)) {
                getNotificationManager().notify(NOTIFICATION_ID, notificationBuilder("Uploading", "", true, true,true));
                Utility.getTransferUtility(this,s3client).resume(observer.getId());
            }
            else{
                getNotificationManager().notify(NOTIFICATION_ID, notificationBuilder("Waiting for network", "", false, true,true));
            }
        }
    }
//    @Info(author = "Mr-Hashim",callType = Info.CallType.EVENT, description = "Eventbus Triggers this function")
//    @Subscribe(threadMode = ThreadMode.MAIN)
    public void S3UploadRequest(S3UploadRequest request){
        this.broadcasts=request.getBroadcast();
        this.s3UploadRequest=request;
        Utility.getSharedPreference(this).edit().putString(S3_REQUEST,new Gson().toJson(request)).apply();
        transcoder(request.getPath());
    }


    @Info(author = "mr-hashim",type = "Video Compressor", description = "this funcion performs tasks for video compressing")
    private void transcoder (String path){
        getNotificationManager().notify(NOTIFICATION_ID,notificationBuilder("Compressing","",true,true,false));
        transcoder=Transcoder.into(videoDestination)
                .addDataSource(this, Uri.parse(path))
                .setVideoTrackStrategy(DefaultVideoStrategies.for720x1280())
                .setListener(new TranscoderListener() {
                    @Override
                    public void onTranscodeProgress(double progress) {
                       int prog =  (int) Math.round(progress*100.0);
                        updateProgress(prog);
                        Utility.showLog("transcoding prog "+ prog);
                        Utility.showLog("transcoding "+ progress);
                    }

                    @Override
                    public void onTranscodeCompleted(int successCode) {
                        if (successCode==Transcoder.SUCCESS_TRANSCODED){
                            transcoder=null;
                            PATH=videoDestination;
                            Utility.showLog("transcoding finished "+ PATH);
                            uploadRequest();
                        }

                    }

                    @Override
                    public void onTranscodeCanceled() {
                        onErrorStopEverything();
                    }

                    @Override
                    public void onTranscodeFailed(@NonNull Throwable exception) {
                        transcoder=null;
                        Utility.showLog("transcoder failed"+exception.getLocalizedMessage());
                        onErrorStopEverything();
                    }
                })
                .transcode();
    }

    @Info(callType = Info.CallType.AWS_S3,description = "upload file")
    private void uploadRequest(){
        if (Utility.isNetworkAvailable(this)) {
            if (s3UploadRequest.getS3_PATH() !=null && PATH!=null) {
                Utility.showLog("uploading");
                getNotificationManager().notify(NOTIFICATION_ID, notificationBuilder("Uploading", "", true, true,true));
                 observer = Utility.getTransferUtility(this, s3client)
                         .upload(s3UploadRequest.getS3_PATH(), new File(PATH), CannedAccessControlList.PublicRead);
                observer.setTransferListener(this);
            }
        }
        else{
            getNotificationManager().notify(NOTIFICATION_ID, notificationBuilder("Waiting for network", "", false, true,true));
        }
    }


    @Info(callType = Info.CallType.AWS_S3,description = "upload file response trigger")
    @Override
    public void onStateChanged(int id, TransferState state) {
        if(TransferState.COMPLETED==state){
            updateProgress(50);
            handleAction();
            Utility.showLog("upload success");
        }
        else if (TransferState.FAILED==state){
            Utility.showLog("upload failed");
            onErrorStopEverything();
        }
        else if (TransferState.WAITING_FOR_NETWORK==state){
            getNotificationManager().notify(NOTIFICATION_ID, notificationBuilder("Waiting for network", "", false, true,true));
            Utility.showLog("waiting for network");
        }
        else if (TransferState.CANCELED==state){
            Utility.showLog("upload cancelled");
        }
        else if (TransferState.IN_PROGRESS==state){
            Utility.showLog("upload inprogress");
        }
        else if (TransferState.PAUSED==state){
            Utility.showLog("upload paused");
        }
        else if (TransferState.UNKNOWN==state){
            Utility.showLog("upload unknown");
        }
        else if (TransferState.PART_COMPLETED==state){
            Utility.showLog("upload part completed");
        }

    }

    private void handleAction() {
        makeFilePublic();


    }



    @Info(callType = Info.CallType.AWS_S3,description = "upload file response trigger")
    @SuppressLint("NewApi")
    @Override
    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
        double bc= bytesCurrent*1.0;
        double bt= bytesTotal*1.0;
        int progress=(int)  Math.round((bc/bt)*100);
        Utility.showLog("upload progress = "+progress);
        updateProgress(progress);
        Utility.showLog("TBytes = "+bytesTotal + " / RBytes = "+bytesCurrent);
    }

    @Info(callType = Info.CallType.AWS_S3,description = "upload file response trigger")
    @Override
    public void onError(int id, Exception ex) {
        Utility.showLog("S3 Error"+id);
    }


    @Info(callType = Info.CallType.AWS_S3,description = "make file public")
    private void makeFilePublic() {
        getNotificationManager().notify(NOTIFICATION_ID,notificationBuilder("Getting Public Url","",false,true,false));
        Single.fromCallable(() -> s3client.getUrl(Constants.S3Constants.S3_BUCKET,s3UploadRequest.getS3_PATH())
        ).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<URL>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(URL url) {
                        updateProgress(75);
                        Utility.showLog("url"+url.toString());
                        S3FileLink=url.toString();
                        postThumbnail(url.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        onErrorStopEverything();
                        Utility.showLog("errorObject");
                    }
                });

    }

    @Info(callType = Info.CallType.API_CALL,description = "post thumbnail for s3 video")
    private void postThumbnail(String url) {
        NetworkCall.CallAPI(this,Utility.getService(Constants.DEFAULT_URL_PORT).postThumbnail(s3UploadRequest.getKey(),url),this,false, Object.class,Constants.Endpoints.S3THUMBNAIL);
        Utility.showLog("ThumbNail");
        Log.e("none", "postThumbnail: " );

    }

    @Info(callType = Info.CallType.API_CALL,description = "post broadcast on simx server")
    private void postBroadcast() {
        HashMap<String,Object>map= new HashMap<>();
        map.put("resource",broadcasts);
        NetworkCall.CallAPI(this,Utility.getService(Constants.DreamFactory.URL).postBroadcast(map),this,false,Broadcasts.class,Constants.Endpoints.BROADCASTS);
    }

    private void updateUser(HashMap <String,Object> map) {
        NetworkCall.CallAPI(this,Utility.getService(Constants.DreamFactory.URL).updateUser(map),this,false,Object.class,Constants.Endpoints.POST_USER);
    }

    private void postTags() {
        HashMap<String,Object> map=new HashMap<>();
        map.put("resource",broadcasts.getTags());
        NetworkCall.CallAPI(this, CommonUtils.getService(Constants.DreamFactory.URL).postTags(map),this,
                false,Object.class,Constants.Endpoints.TAGS);
    }
    private void postVideoCV() {
        HashMap <String,Object> map=new HashMap<>();
        map.put("resource", s3UploadRequest.getVideoCv());
       NetworkCall.CallAPI(this,Utility.getService(Constants.DreamFactory.URL).postVideoCV(map),this,false,
               Object.class, Constants.Endpoints.VIDEOCV);
    }


    @Info(author = "Mr-Hashim",callType = Info.CallType.API_RESPONSE, description = "function trigger when remote api call is successfull")
    @Override
    public void OnSuccess(Response<JsonObject> response, Object body, String endpoint) {

        if (endpoint.equalsIgnoreCase(Constants.Endpoints.BROADCASTS)) {
            updateProgress(100);
            if (Utility.deleteFile(s3UploadRequest.getPath())){
                Utility.showLog("original file deleted"+s3UploadRequest.getPath());
            }
            if (Utility.deleteFile(PATH)){
                Utility.showLog("compressed file deleted"+PATH);
            }
            Utility.getSharedPreference(this).edit().putBoolean(Constants.SharedPreference.ISUPLOADING,false).apply();
            Utility.getSharedPreference(this).edit().remove(S3_REQUEST).apply();
            EventBus.getDefault().post(new HashMap<String, String>());
            getNotificationManager().notify(NOTIFICATION_ID, notificationBuilder("Upload Successfull!", "", false, false,false));
            EventBus.getDefault().unregister(this);
            stopForeground(true);
            stopSelf();
        }
        else if (endpoint.equalsIgnoreCase(Constants.Endpoints.TAGS)){

        }
        else if (endpoint.equals(Constants.Endpoints.S3THUMBNAIL)){
            switch (s3UploadRequest.getAction()){
                case BROADCAST:
                    postBroadcast();
                    break;

                case VIDEOCV:
                    postVideoCV();
                    break;


            }
        }

        else if (endpoint.equalsIgnoreCase(Constants.Endpoints.VIDEOCV)){
            EventBus.getDefault().post(new HashMap<String, String>());
            Utility.showLog("User CV Updated");
            stopService();
        }
    }



    @Info(author = "Mr-Hashim", callType = Info.CallType.API_RESPONSE ,description = "function trigger when remote api call yields error")
    @Override
    public void OnError(Response<JsonObject> response, String endpoint) {
        onErrorStopEverything();
        Utility.showLog("API Error "+endpoint+" ");
    }


    @Info(author = "Mr-Hashim",callType = Info.CallType.API_RESPONSE,description = "function trigger when remote api call yields exception")
    @Override
    public void OnException(Throwable e, String endpoint) {
        Utility.showLog("API Exception "+e.getMessage()+" "+e.getLocalizedMessage());
        onErrorStopEverything();


    }


    @Info(author = "Mr-Hashim",callType = Info.CallType.API_RESPONSE,description = "function trigger when remote api call yields network error")
    @Override
    public void OnNetWorkError(String endpoint, String message) {
        if (endpoint.equalsIgnoreCase(Constants.Endpoints.S3THUMBNAIL)){
            if (S3FileLink!=null) {
                postThumbnail(S3FileLink);
            }
        }
        else if (endpoint.equals(Constants.Endpoints.BROADCASTS)){
          postBroadcast();
        }
        else if (endpoint.equalsIgnoreCase(Constants.Endpoints.POST_USER)) {
            HashMap <String,Object> map=new HashMap<>();
            Users user= Utility.getloggedUser(this);
            user.setVideocv(Utility.getloggedUser(this).getUsername());
            map.put("resource", user);
            updateUser(map);
        }
    }


    @Info(author = "Mr-hashim",type = "Transcoder Builder", description = "unused")
    private DefaultVideoStrategy getStrategy(){
        return new DefaultVideoStrategy.Builder()
                .bitRate(2073600)
                .build();
    }

    @Info(author = "Mr-hashim",callType = Info.CallType.SERVICE, description = "this function stop the service if it is detected")
    private void onErrorStopEverything(){
        Utility.showLog("on error called");
        if (s3UploadRequest.getPath()!=null && Utility.deleteFile(s3UploadRequest.getPath())){
            Utility.showLog("original file deleted "+s3UploadRequest.getPath());
        }
        if (PATH!=null && Utility.deleteFile(PATH)){
            Utility.showLog("compressed file deleted "+PATH);
        }
        Utility.getSharedPreference(this).edit().putBoolean(Constants.SharedPreference.ISUPLOADING,false).apply();
        Utility.getSharedPreference(this).edit().remove(S3_REQUEST).apply();
        getNotificationManager().notify(NOTIFICATION_ID,notificationBuilder("Uploading Failed","",false,false,false));
        EventBus.getDefault().unregister(this);
        stopSelf();
    }

    private void stopService(){
        Utility.showLog("Stop called");
        if (s3UploadRequest.getPath()!=null && Utility.deleteFile(s3UploadRequest.getPath())){
            Utility.showLog("original file deleted"+s3UploadRequest.getPath());
        }
        if (PATH!=null && Utility.deleteFile(PATH)){
            Utility.showLog("compressed file deleted"+PATH);
        }
        Utility.getSharedPreference(this).edit().putBoolean(Constants.SharedPreference.ISUPLOADING,false).apply();
        Utility.getSharedPreference(this).edit().remove(S3_REQUEST).apply();
        EventBus.getDefault().unregister(this);
        stopSelf();
    }








    @Override
    public void onDestroy() {
        super.onDestroy();
        merlin.unbind();
        EventBus.getDefault().unregister(this);
        mInstance=null;
    }



}

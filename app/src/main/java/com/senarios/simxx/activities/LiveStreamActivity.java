package com.senarios.simxx.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.sns.AmazonSNSAsyncClient;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hdev.common.Constants;
import com.hdev.common.MessageEvent;
import com.hdev.common.datamodels.paypaldatamodels.BroadcastRequestBody;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.adaptors.CommentsAdaptor;
import com.hdev.common.datamodels.Broadcasts;
import com.hdev.common.datamodels.Comments;
import com.senarios.simxx.models.ApiService;
import com.senarios.simxx.models.SendMailRes;
import com.senarios.simxx.viewmodels.SharedVM;
import com.wowza.gocoder.sdk.api.WowzaGoCoder;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcast;
import com.wowza.gocoder.sdk.api.broadcast.WOWZBroadcastConfig;
import com.wowza.gocoder.sdk.api.configuration.WOWZMediaConfig;
import com.wowza.gocoder.sdk.api.devices.WOWZAudioDevice;
import com.wowza.gocoder.sdk.api.devices.WOWZCamera;
import com.wowza.gocoder.sdk.api.devices.WOWZCameraView;
import com.wowza.gocoder.sdk.api.errors.WOWZError;
import com.wowza.gocoder.sdk.api.errors.WOWZStreamingError;
import com.wowza.gocoder.sdk.api.status.WOWZState;
import com.wowza.gocoder.sdk.api.status.WOWZStatus;
import com.wowza.gocoder.sdk.api.status.WOWZStatusCallback;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.SingleObserver;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.recyclerview.animators.FadeInAnimator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.hdev.common.Constants.Messages.NETWORK_ERROR;
import static com.hdev.common.Constants.SharedPreference.B_LINKEDIN;
import static com.hdev.common.Constants.SharedPreference.B_TWITTER;
import static com.hdev.common.Constants.SharedPreference.SUB_AWS;
import static com.hdev.common.Constants.SharedPreference.iS_COMMENT;

public class LiveStreamActivity extends AppCompatActivity implements WOWZStatusCallback, Chronometer.OnChronometerTickListener {
    public static final String DURATION = "duration";
    private final int CODE = 1000;
    private String[] permissions = {"android.permission.RECORD_AUDIO", "android.permission.CAMERA"};
    private List<String> denied = new ArrayList<>();
    private final int SETTING_CODE = 101;
    private Chronometer chronometer;
    private TextView tv_start_end, viewers, warntext;
    private ImageView switch_camera;
    private ConstraintLayout topview;
    private TextView counter;
    private SharedVM sharedVM;
    private Broadcasts broadcast;
    private AWSCredentials credentials;
    private AmazonSNSClient snsClient;
    private RecyclerView recyclerView;
    private CommentsAdaptor commentsAdaptor;
    private List<Comments> comments = new ArrayList<>();
    private EditText comment;
    private ImageView send;
    private ImageView like;
    private ProgressDialog pd;
    // The top-level GoCoder API interface
    private WowzaGoCoder goCoder;
    private String subscription_arn = "";

    // The GoCoder SDK camera view
    private WOWZCameraView goCoderCameraView;
    // The GoCoder SDK audio device
    private WOWZAudioDevice goCoderAudioDevice;
    // The GoCoder SDK broadcaster
    private WOWZBroadcast goCoderBroadcaster;
    // The broadcast configuration settings
    private WOWZBroadcastConfig goCoderBroadcastConfig;
    private boolean LINKED_IN = false, TWITTER = false, IS_COMMENT = false;
    private int viewer_counter = 0;
    private ConstraintLayout bottom;
    private String mDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_stream);


        //
        switch_camera = findViewById(R.id.iv_switch);
        tv_start_end = findViewById(R.id.tv_end_stream);
        viewers = findViewById(R.id.viewers);
        topview = findViewById(R.id.topview);
        counter = findViewById(R.id.countdown);
        recyclerView = findViewById(R.id.recyclerview);
        send = findViewById(R.id.send);
        comment = findViewById(R.id.comment);
        like = findViewById(R.id.like);
        chronometer = findViewById(R.id.chronometer);
        bottom = findViewById(R.id.bottom);
        warntext = findViewById(R.id.warntext);


        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new FadeInAnimator());
        recyclerView.getItemAnimator().setRemoveDuration(1500);
        recyclerView.getItemAnimator().setAddDuration(1000);

        //
        sharedVM = new ViewModelProvider(this).get(SharedVM.class);

        pd = Utility.setDialogue(this);


        //
        if (getIntent().getExtras() != null) {
            broadcast = (Broadcasts) getIntent().getSerializableExtra("b");
            TWITTER = getIntent().getBooleanExtra(B_TWITTER, false);
            LINKED_IN = getIntent().getBooleanExtra(B_LINKEDIN, false);
            IS_COMMENT = getIntent().getBooleanExtra(iS_COMMENT, false);
            mDuration = getIntent().getStringExtra(DURATION);
            if (mDuration.equals("15:00"))
                warntext.setText(R.string.warninglivestreamforlong);
        }

        bottom.setVisibility(View.VISIBLE);
        initAWS();

        initgocoder();
        init();
        initclick();

        chronometer.setOnChronometerTickListener(this);

    }

    private void initAWS() {
//aws sns using rxjava, cant use this on main thread.
        Completable.fromAction(() -> {

            //init aws credentials
            credentials = new BasicAWSCredentials(getResources().getString(R.string.AWSAppAccessKey), getResources().getString(R.string.AWSAppAccessSecretKey));

            //init aws SNsclient
            snsClient = new AmazonSNSAsyncClient(credentials);
            snsClient.setRegion(Region.getRegion("us-west-2"));

            //init Create endpoint
            CreatePlatformEndpointRequest endpointRequest = new CreatePlatformEndpointRequest();
            endpointRequest.setToken(sharedVM.getSharedPreference().getString(Constants.SharedPreference.FCM, ""));
            endpointRequest.setPlatformApplicationArn(getResources().getString(R.string.APPLICATION_ARN));
            CreatePlatformEndpointResult endpointResult = snsClient.createPlatformEndpoint(endpointRequest);

            //init Create Topic
            CreateTopicRequest ctr = new CreateTopicRequest(broadcast.getBroadcast());
            CreateTopicResult result = snsClient.createTopic(ctr);

            //set topic arn to broadcast model object
            broadcast.setArn(result.getTopicArn());

            ////init sub to endpoint
            SubscribeRequest sub = new SubscribeRequest();
            sub.withTopicArn(broadcast.getArn());
            sub.setEndpoint(endpointResult.getEndpointArn());
            sub.setProtocol("Application");
            SubscribeResult req = snsClient.subscribe(sub);
            subscription_arn = req.getSubscriptionArn();


        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new CompletableObserver() {

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        sharedVM.getSharedPreference().edit().putBoolean(SUB_AWS, true).apply();
                        broadcast.setViewers(viewer_counter);
                        broadcast.setUsername(sharedVM.getLoggedUser().getUsername());
                        broadcast.setId(0);
                        postBroadcast(broadcast);


                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.v("error ARN", e.getMessage());
                        postBroadcast(broadcast);
                    }
                });
    }

    private void initgocoder() {
        // Initialize the GoCoder SDK
        goCoder = WowzaGoCoder.getInstance();


        if (goCoder == null) {
            // If initialization failed, retrieve the last error and display it
            WOWZError goCoderInitError = WowzaGoCoder.getLastError();
            Log.v("coder error", goCoderInitError.getErrorDescription());
            return;
        }

        goCoderCameraView = findViewById(R.id.camera_preview);

        goCoderCameraView.setCameraByDirection(broadcast.getCamera());

        goCoderAudioDevice = new WOWZAudioDevice();

        // Create a broadcaster instance
        goCoderBroadcaster = new WOWZBroadcast();

// Create a configuration instance for the broadcaster
        goCoderBroadcastConfig = new WOWZBroadcastConfig(WOWZMediaConfig.FRAME_SIZE_960x540);

        // Set the connection properties for the target Wowza Streaming Engine server or Wowza Streaming Cloud live stream
        goCoderBroadcastConfig.setHostAddress(Constants.GoCoder.PUBLISH_VIDEO);
        goCoderBroadcastConfig.setPortNumber(1935);
        goCoderBroadcastConfig.setVideoFramerate(30);
        goCoderBroadcastConfig.setVideoKeyFrameInterval(60);
        goCoderBroadcastConfig.setVideoBitRate(1000);
        goCoderBroadcastConfig.setApplicationName("live");
        goCoderBroadcastConfig.setStreamName(broadcast.getBroadcast());


// Designate the camera preview as the video source
        goCoderBroadcastConfig.setVideoBroadcaster(goCoderCameraView);

// Designate the audio device as the audio broadcaster
        goCoderBroadcastConfig.setAudioBroadcaster(goCoderAudioDevice);

    }


    private void addtoList(String text) {
        Comments comment = new Comments();
        comment.setUser(sharedVM.getLoggedUser().getUsername());
        comment.setName(sharedVM.getLoggedUser().getName());
        comment.setEmail(sharedVM.getLoggedUser().getEmail());
        comment.setText(text);
        comment.setUser(sharedVM.getLoggedUser().getUsername());
        if (commentsAdaptor == null) {
            comments.add(comment);
            commentsAdaptor = new CommentsAdaptor(comments, LiveStreamActivity.this);
            recyclerView.setAdapter(commentsAdaptor);
        } else {
            comments.add(comment);
            commentsAdaptor.notifyDataSetChanged();
            commentsAdaptor.notifyItemInserted(comments.size() - 1);
        }
    }

    @SuppressLint("NewApi")
    private void init() {

        //timer
        new CountDownTimer(4000, 1000) {


            @Override
            public void onTick(long l) {
                counter.setText("" + l / 1000);
            }

            @Override
            public void onFinish() {
                // Ensure the minimum set of configuration settings have been specified necessary to
                // initiate a broadcast streaming session
                try {
                    WOWZStreamingError configValidationError = goCoderBroadcastConfig.validateForBroadcast();
                    if (configValidationError != null) {
                        return;
                    } else {
                        // Start streaming
                        goCoderBroadcaster.startBroadcast(goCoderBroadcastConfig, LiveStreamActivity.this);
                    }
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();

                } catch (Exception e) {
                    Utility.showELog(e);


                }

                topview.setVisibility(View.GONE);

            }
        }.start();

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (commentsAdaptor == null) {
                    if (getComment(intent.getStringExtra("data")) != null) {
                        Comments comment = getComment(intent.getStringExtra("data"));
                        if (comment != null) {
                            if (comment.getUser() != null && !comment.getUser().equalsIgnoreCase(sharedVM.getLoggedUser().getUsername())) {

                                if (comment.getText().isEmpty()) {
                                   viewer_counter = viewer_counter+1;
                                    viewers.setText("Viewers " + viewer_counter);
                                }
                                else if (comment.getText().matches("Left"))
                                {
                                    viewer_counter = viewer_counter-1;
                                    viewers.setText("Viewers " + viewer_counter);
                                }
                                    else
                                {
                                    comments.add(comment);
                                }

                            }
                        }

                    }
                    commentsAdaptor = new CommentsAdaptor(comments, LiveStreamActivity.this);
                    recyclerView.setAdapter(commentsAdaptor);
                } else {
                    if (getComment(intent.getStringExtra("data")) != null) {
                        Comments comment = getComment(intent.getStringExtra("data"));
                        if (comment != null) {
                            if (comment.getUser() != null && !comment.getUser().equalsIgnoreCase(sharedVM.getLoggedUser().getUsername())) {
                                if (comment.getText().isEmpty()) {
                                    viewer_counter = viewer_counter+1;
                                    viewers.setText("Viewers " + viewer_counter);
                                }
                                else if (comment.getText().matches("Left"))
                                {
                                    viewer_counter = viewer_counter-1;
                                    viewers.setText("Viewers " + viewer_counter);
                                }
                                else {
                                    comments.add(comment);
                                }
                            }
                        }
                    }
                    commentsAdaptor.notifyDataSetChanged();
                    commentsAdaptor.notifyItemInserted(comments.size() - 1);
                }

            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("custom"));


        //permissions
        if (!hasPermissions(this, permissions)) {
            requestPermissions(permissions, CODE);
        }


    }

    private void initclick() {
        send.setOnClickListener(v -> {
            String text = comment.getText().toString().trim();
            comment.setText("");
            if (!text.isEmpty()) {
                sendmessage(text);
                addtoList(text);
            }

        });

        tv_start_end.setOnClickListener(view -> {
                    String s = chronometer.getText().toString().split(":")[0] + chronometer.getText().toString().split(":")[1];
                    int time = Integer.parseInt(s);
                    if (time > 15) {
                        sendMailAndEndLiveStream();
                    } else {
                        Toast.makeText(this, "You cannot End Stream Before 15 Seconds!", Toast.LENGTH_SHORT).show();
                    }
                }

        );
        switch_camera.setOnClickListener(view -> {
            if (goCoderCameraView != null) {
                goCoderCameraView.switchCamera();
            }
        });


    }

    private void sendMailAndEndLiveStream() {
        pd.show();
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://web.scottishhealth.live/")
                .addConverterFactory(GsonConverterFactory.create()).build();
        ApiService apiPost = retrofit.create(ApiService.class);
        Call<SendMailRes> call = apiPost.sendMail(getString(R.string.admin_mail),getString(R.string.admin_name),
                getString(R.string.mail_title),getString(R.string.mail_body1)+" "+broadcast.getTitle()+" "+getString(R.string.mail_body2));
        call.enqueue(new Callback<SendMailRes>() {
            @Override
            public void onResponse(Call<SendMailRes> call, Response<SendMailRes> response) {
                if (response.isSuccessful()) {
                    pd.dismiss();
                    chronometer.stop();
                    broadcast.setStatus(Constants.GoCoder.OFFLINE);
                    updateBroadcast(broadcast);
                } else {
                    pd.dismiss();
                    tv_start_end.performClick();
//                    Toast.makeText(LiveStreamActivity.this, "Process Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SendMailRes> call, Throwable t) {
                pd.dismiss();
                if (t instanceof SocketTimeoutException) {
                    Toast.makeText(LiveStreamActivity.this, "Time out, Please try again", Toast.LENGTH_SHORT).show();
                } else if (t instanceof IOException) {
                    Toast.makeText(LiveStreamActivity.this, "Check you internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendmessage(String text) {
        Completable.fromAction(() -> {
            Comments comments = new Comments();
            comments.setName(sharedVM.getLoggedUser().getName());
            comments.setEmail(sharedVM.getLoggedUser().getEmail());
            comments.setText(text);
            if (broadcast != null && broadcast.getArn() != null) {
                comments.setArn(broadcast.getArn());
            }
            comments.setUser(sharedVM.getLoggedUser().getUsername());
            PublishRequest publishRequest = new PublishRequest(broadcast.getArn(), new Gson().toJson(comments));
            PublishResult publishResult = snsClient.publish(publishRequest);
            String id = publishResult.getMessageId();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {


                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    private void postBroadcast(Broadcasts broadcast) {
        if (Utility.isNetworkAvailable(this)) {
            BroadcastRequestBody broadcastRequestBody = new BroadcastRequestBody();
            broadcastRequestBody.setBroadcasts(broadcast);
            HashMap<String, Object> map = new HashMap<>();
            map.put("resource", broadcast);
            sharedVM.getService(Constants.DreamFactory.URL)
                    .postBroadcast(broadcastRequestBody)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(new SingleObserver<Response<JsonObject>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Response<JsonObject> response) {
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    Log.v("broadcastposted", response.toString());
                                    int id = response.body().get("resource").getAsJsonArray().get(0).getAsJsonObject().get("id").getAsInt();
                                    broadcast.setId(id);
                                }
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            initAWS();

                        }
                    });
        } else {

        }

    }

    private void postThumbnail() {
        if (Utility.isNetworkAvailable(this)) {
            sharedVM.getService(Constants.DEFAULT_URL_PORT)
                    .postThumbnail(broadcast.getBroadcast())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Response<JsonObject>>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            Log.e("OnSubscribe" , String.valueOf(d));

                        }

                        @Override
                        public void onSuccess(Response<JsonObject> response) {
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    if (response.body() != null) {
                                        Log.e("none", "onSuccess: "+response.body().toString() );
                                    } else {
                                        Log.e("none", "onSuccess:2 "+response.body().toString() );
                                    }


                                } else {

                                    Log.e("none", "onSuccess:3 "+response.body().toString() );
                                }

                                Log.e("none", "onSuccess:4 "+response.body().toString() );
                            } else {
                                Log.e("none", "onSuccess:5 "+response.body().toString() );
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e("none", "onSuccess: "+e.getLocalizedMessage());
                        }
                    });
        }


    }

    private void updateBroadcast(Broadcasts broadcasts) {
        if (Utility.isNetworkAvailable(this)) {
//            pd.show();
            broadcasts.setTags(null);
            broadcasts.setJobPostStatus("Pending");
            BroadcastRequestBody broadcastRequestBody = new BroadcastRequestBody();
            broadcastRequestBody.setBroadcasts(broadcasts);
            HashMap<String, Object> map = new HashMap<>();
            map.put("resource", broadcasts);
            Log.v("update broadcast", new Gson().toJson(map));
            sharedVM.getService(Constants.DreamFactory.URL)
                    .updateBroadcast(broadcastRequestBody)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Response<JsonObject>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Response<JsonObject> response) {
//                            pd.dismiss();
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    if (response.body() != null) {
                                        if (!IS_COMMENT) {
                                            unsubscribe();
                                        }
                                        if (goCoderBroadcaster.getStatus().isRunning()) {
                                            goCoderBroadcaster.endBroadcast(LiveStreamActivity.this);
                                        }
                                        sharedVM.getSharedPreference().edit().putBoolean(SUB_AWS, false).apply();
                                        Intent intent = new Intent();
                                        intent.putExtra("int", 90);
                                        setResult(RESULT_OK, intent);
                                        sendMessage();
                                        finish();
                                    } else {
                                        Intent intent = new Intent();
                                        intent.putExtra("int", 90);
                                        setResult(RESULT_OK, intent);
                                        sendMessage();
                                        finish();

                                    }


                                } else {
                                    Intent intent = new Intent();
                                    intent.putExtra("int", 90);
                                    setResult(RESULT_OK, intent);
                                    sendMessage();
                                    finish();
                                }

                            } else {
                                Intent intent = new Intent();
                                intent.putExtra("int", 90);
                                setResult(RESULT_OK, intent);
                                sendMessage();
                                finish();
                            }

                        }

                        @Override
                        public void onError(Throwable e) {
//                            pd.dismiss();
                            Intent intent = new Intent();
                            intent.putExtra("int", 90);
                            setResult(RESULT_OK, intent);
                            sendMessage();
                            finish();
                        }
                    });

        } else {
            Toast.makeText(this, Constants.Messages.NETWORK_ERROR, Toast.LENGTH_SHORT).show();
        }

    }

    private boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @SuppressLint("NewApi")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        denied.clear();
        if (requestCode == CODE) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    denied.add(permission);
                }
            }
            if (denied.size() == 0) {


            } else if (denied.size() == 1) {
                requestPermissions(permissions, CODE);
            } else if (!(shouldShowRequestPermissionRationale(permissions[0]) && shouldShowRequestPermissionRationale(permissions[1]))) {
                showSettingDialogue();
            } else {
                requestPermissions(permissions, CODE);
            }
        }
    }

    private void showSettingDialogue() {
        AlertDialog.Builder myAlertDialog;
        myAlertDialog = new AlertDialog.Builder(this);
        myAlertDialog.setTitle("Permission Denied");
        myAlertDialog.setMessage("Camera and Audio Permissions are needed for live streaming, please allow them");
        myAlertDialog.setPositiveButton("Go to Setting",
                (arg0, arg1) -> openSettings());

        myAlertDialog.setNegativeButton("Nah, Im good",
                (arg0, arg1) -> {
                    arg0.dismiss();

                    finish();
                });
        myAlertDialog.show();

    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, SETTING_CODE);


    }

    private Comments getComment(String data) {
        try {
            JSONObject object = new JSONObject(data);
            if (object.getString("default") != null) {
                return new Gson().fromJson(object.getString("default"), Comments.class);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    @Override
    public void onWZStatus(WOWZStatus goCoderStatus) {
        final StringBuffer statusMessage = new StringBuffer("Broadcast status: ");

        switch (goCoderStatus.getState()) {
            case WOWZState.STARTING:
                statusMessage.append("Broadcast initialization");
                break;

            case WOWZState.READY:
                statusMessage.append("Ready to begin streaming");
                break;

            case WOWZState.RUNNING:
                statusMessage.append("Streaming is active");
                break;

            case WOWZState.STOPPING:
                statusMessage.append("Broadcast shutting down");
                break;

            case WOWZState.IDLE:
                statusMessage.append("The broadcast is stopped");
                break;

            default:
                return;
        }
        Log.v("wowza status", goCoderStatus.getState() + "");
    }

    @Override
    public void onWZError(WOWZStatus wowzStatus) {
        Log.v("wowza status", wowzStatus.getState() + "");
        /*  runOnUiThread(() -> updateBroadcast(broadcast));*/

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start the camera preview display
        try {
            if (goCoderCameraView != null) {
                if (goCoderCameraView.isPreviewPaused())
                    goCoderCameraView.onResume();
                else
                    goCoderCameraView.startPreview();
            }

        } catch (Exception e) {
            AlertDialog.Builder builder = Utility.getAlertDialoge(this, "Error", "Unable to start stream, something went wrong.");
            builder.show();

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (goCoderCameraView != null) {
            goCoderCameraView.onPause();
        }
//        goCoderCameraView = null;
//        broadcast.setStatus(Constants.GoCoder.OFFLINE);
//        updateBroadcast(broadcast);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    @Override
    public void onBackPressed() {
        String s = chronometer.getText().toString().split(":")[0] + chronometer.getText().toString().split(":")[1];
        int time = Integer.parseInt(s);
        if (time > 15) {
            sendMailAndEndLiveStream();
        } else {
            Toast.makeText(this, "You cannot End Stream Before 15 Seconds!", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onChronometerTick(Chronometer chronometer) {
        if (chronometer.getText().toString().equalsIgnoreCase("00:35")) {
            if (TWITTER || LINKED_IN) {
//                shareAPI();
            }
        } else if (chronometer.getText().toString().equalsIgnoreCase("00:14")) {
            postThumbnail();
        } else if (chronometer.getText().toString().equalsIgnoreCase(mDuration)) {
//            broadcast.setStatus(Constants.GoCoder.OFFLINE);
//            updateBroadcast(broadcast);
            sendMailAndEndLiveStream();
        }
    }

    private void shareAPI() {
        if (Utility.isNetworkAvailable(this)) {
            sharedVM.getService(Constants.DEFAULT_URL)
                    .shareAPI(broadcast.getTitle(), sharedVM.getLoggedUser().getUsername(), sharedVM.getSharedPreference().getString(Constants.SharedPreference.ACCESS_TOKEN, ""),
                            sharedVM.getSharedPreference().getString(Constants.AUTH.USER_AUTH_TOKEN, ""),
                            broadcast.getBroadcast(),
                            sharedVM.getSharedPreference().getString(Constants.AUTH.USER_AUTH_SECRET, ""))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Response<JsonObject>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Response<JsonObject> response) {
                            pd.dismiss();
                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    if (response.body() != null) {


                                    } else {


                                    }
                                } else {


                                }

                            } else {


                            }


                        }

                        @Override
                        public void onError(Throwable e) {


                        }
                    });


        } else {
            Toast.makeText(this, NETWORK_ERROR, Toast.LENGTH_SHORT).show();
        }
    }

    private void unsubscribe() {
        sharedVM.getSharedPreference().edit().putBoolean(SUB_AWS, false).apply();
        try {
            if (broadcast.getArn() != null) {
                new Thread(() -> {
                    snsClient.unsubscribe(subscription_arn);
                }).start();
                return;
            } else {
                return;
            }

        } catch (Exception e) {
            return;
        }

    }

    private void sendMessage() {
        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent("custom-event-name");
        // You can also include some extra data.
        intent.putExtra("message", "This is my message!");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}

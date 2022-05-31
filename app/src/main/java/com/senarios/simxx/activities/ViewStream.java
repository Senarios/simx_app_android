package com.senarios.simxx.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.ViewModelProviders;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.sns.AmazonSNSAsyncClient;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.SubscribeResult;
import com.google.gson.Gson;
import com.hdev.common.Constants;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.adaptors.CommentsAdaptor;
import com.hdev.common.datamodels.Broadcasts;
import com.hdev.common.datamodels.Comments;
import com.senarios.simxx.viewmodels.SharedVM;
import com.wowza.gocoder.sdk.api.configuration.WOWZMediaConfig;
import com.wowza.gocoder.sdk.api.player.WOWZPlayerConfig;
import com.wowza.gocoder.sdk.api.player.WOWZPlayerView;
import com.wowza.gocoder.sdk.api.status.WOWZState;
import com.wowza.gocoder.sdk.api.status.WOWZStatus;
import com.wowza.gocoder.sdk.api.status.WOWZStatusCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.recyclerview.animators.FadeInAnimator;

import static com.hdev.common.Constants.SharedPreference.SUB_AWS;
import static com.senarios.simxx.activities.MainActivity.STATIC_TOKEN;

public class ViewStream extends AppCompatActivity implements WOWZStatusCallback {
    private WOWZPlayerView playerView;
    private WOWZPlayerConfig mStreamPlayerConfig;
    private Broadcasts broadcast;
    private SharedVM sharedVM;
    private EditText comment;
    private ImageView send;
    private RecyclerView recyclerView;
    private CommentsAdaptor commentsAdaptor;
    private List<Comments> comments = new ArrayList<>();
    private ProgressDialog pd;
    private AWSCredentials credentials;
    private AmazonSNSClient snsClient;
    private String subscription_arn = "";
    private ConstraintLayout bottom;
    private Group group;
    private int viewer_counter = 0;
    private TextView viewers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_stream);

        //
        send = findViewById(R.id.send);
        comment = findViewById(R.id.comment);
        recyclerView = findViewById(R.id.recyclerview);
        bottom = findViewById(R.id.bottom);
        viewers = findViewById(R.id.viewers);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new FadeInAnimator());
        recyclerView.getItemAnimator().setRemoveDuration(1500);
        recyclerView.getItemAnimator().setAddDuration(1000);
        group = findViewById(R.id.group);

        sharedVM = ViewModelProviders.of(this).get(SharedVM.class);

        playerView = findViewById(R.id.vwStreamPlayer);

        if (getIntent().getExtras() != null) {
            broadcast = (Broadcasts) getIntent().getSerializableExtra("b");
        }

        sharedVM.getSharedPreference().edit().putBoolean(SUB_AWS, true).apply();
        //
        pd = Utility.setDialogue(this);

        if ((broadcast != null && broadcast.getArn() != null) && !broadcast.getArn().equalsIgnoreCase("NA")) {
            initAWS();
        } else {
            group.setVisibility(View.GONE);
        }

        initplayerview();

        init();


    }

    private void initAWS() {
        //aws sns using rxjava, cant use this on main thread.
        Completable.fromAction(() -> {

            //init aws credentials
            credentials = new BasicAWSCredentials(getResources().getString(R.string.AWSAppAccessKey), getResources().getString(R.string.AWSAppAccessSecretKey));

            //init aws SNsclient
            snsClient = new AmazonSNSAsyncClient(credentials);
            snsClient.setRegion(Region.getRegion("us-west-2"));

            CreatePlatformEndpointRequest endpointRequest = new CreatePlatformEndpointRequest();
            if (sharedVM.getSharedPreference().getString(Constants.SharedPreference.FCM, "") != null && !Objects.requireNonNull(sharedVM.getSharedPreference().getString(Constants.SharedPreference.FCM, "")).isEmpty())
                endpointRequest.setToken(sharedVM.getSharedPreference().getString(Constants.SharedPreference.FCM, ""));
            else
                endpointRequest.setToken(STATIC_TOKEN);

            endpointRequest.setPlatformApplicationArn(getResources().getString(R.string.APPLICATION_ARN));
            CreatePlatformEndpointResult endpointResult = snsClient.createPlatformEndpoint(endpointRequest);

            ////init sub to endpoint
            SubscribeRequest sub = new SubscribeRequest();
            sub.withTopicArn(broadcast.getArn());
            Log.e("Topic", "" + broadcast.getArn());
            sub.setEndpoint(endpointResult.getEndpointArn());
            sub.setProtocol("Application");
            SubscribeResult req = snsClient.subscribe(sub);
            Log.e("Subs", req.getSubscriptionArn());
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
                        sendmessage("");

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.v("error ARN", e.getMessage());
                    }
                });


    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (commentsAdaptor == null) {
                if (getComment(intent.getStringExtra("data")) != null) {
                    Comments comment = getComment(intent.getStringExtra("data"));
                    if (comment.getUser() != null && !comment.getUser().equalsIgnoreCase(sharedVM.getLoggedUser().getUsername())) {
                        if (comment.getText().isEmpty()) {
                            viewer_counter++;
                            viewers.setText("Viewers" + viewer_counter);
                        } else {
                            comments.add(comment);
                        }
                    }
                }
                commentsAdaptor = new CommentsAdaptor(comments, ViewStream.this);
                recyclerView.setAdapter(commentsAdaptor);
            } else {
                if (getComment(intent.getStringExtra("data")) != null) {
                    Comments comment = getComment(intent.getStringExtra("data"));
                    if (comment != null) {
                        if (comment.getUser() != null && !comment.getUser().equalsIgnoreCase(sharedVM.getLoggedUser().getUsername())) {

                            if (comment.getText().isEmpty()) {
                                viewer_counter++;
                                viewers.setText("Viewers " + viewer_counter);
                            } else {
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

    private void init() {

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter("custom"));

        send.setOnClickListener(v -> {
            String text = comment.getText().toString().trim();
            comment.setText("");
            if (!text.isEmpty()) {
                sendmessage(text);
                addtoList(text);
            }
        });


    }

    private void sendmessage(String text) {
        Completable.fromAction(() -> {
            Comments comments = new Comments();
            comments.setUser(sharedVM.getLoggedUser().getUsername());
            comments.setName(sharedVM.getLoggedUser().getName());
            comments.setEmail(sharedVM.getLoggedUser().getEmail());
            comments.setText(text);
            if (broadcast != null && broadcast.getArn() != null) {
                comments.setArn(broadcast.getArn());
            }
//            comments.setUser(sharedVM.getLoggedUser().getUsername());
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
                        Log.e("none", "onComplete: ");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("none", "onError: " + e.getMessage());
                    }
                });
    }

    private void addtoList(String text) {
        Comments comment = new Comments();
        comment.setUser(sharedVM.getLoggedUser().getUsername());
        comment.setName(sharedVM.getLoggedUser().getName());
        comment.setText(text);
        comment.setEmail(sharedVM.getLoggedUser().getEmail());
        if (commentsAdaptor == null) {
            comments.add(comment);
            commentsAdaptor = new CommentsAdaptor(comments, ViewStream.this);
            recyclerView.setAdapter(commentsAdaptor);
        } else {
            comments.add(comment);
            commentsAdaptor.notifyDataSetChanged();
            commentsAdaptor.notifyItemInserted(comments.size() - 1);
        }
    }

    private void initplayerview() {
        mStreamPlayerConfig = new WOWZPlayerConfig();
        mStreamPlayerConfig.setHostAddress(Constants.GoCoder.PUBLISH_VIDEO);
        mStreamPlayerConfig.setApplicationName("live");
        mStreamPlayerConfig.setStreamName(broadcast.getBroadcast());
        mStreamPlayerConfig.setPortNumber(1935);
        mStreamPlayerConfig.setAudioEnabled(true);
        mStreamPlayerConfig.setVideoEnabled(true);


        // WOWZMediaConfig.FILL_VIEW : WOWZMediaConfig.RESIZE_TO_ASPECT;
        playerView.setScaleMode(WOWZMediaConfig.FILL_VIEW);


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (playerView != null) {
            playerView.play(mStreamPlayerConfig, this);
        }
    }

    @Override
    public void onWZStatus(WOWZStatus goCoderStatus) {
        final StringBuilder statusMessage = new StringBuilder("Broadcast status: ");

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
                runOnUiThread(this::unsubscribe);

                statusMessage.append("Broadcast shutting down");
                break;

            case WOWZState.IDLE:
                statusMessage.append("The broadcast is stopped");
                break;

            default:
                return;
        }

    }

    @Override
    public void onWZError(WOWZStatus wowzStatus) {
        runOnUiThread(this::unsubscribe);

    }

    private Comments getComment(String data) {
        try {
            JSONObject object = new JSONObject(data);
            if (object.getString("default") != null) {
                return new Gson().fromJson(object.getString("default"), Comments.class);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return new Comments();
        }

        return new Comments();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);

    }

    @Override
    public void onBackPressed() {

        sendmessage("Left");
        addtoList("Left");
        unsubscribe();
    }

    private void unsubscribe() {
        sharedVM.getSharedPreference().edit().putBoolean(SUB_AWS, false).apply();
        try {
            if (broadcast.getArn() != null && !broadcast.getArn().equalsIgnoreCase("NA")) {
                new Thread(() -> {
                    try {
                        snsClient.unsubscribe(subscription_arn);
                    } catch (Exception e) {
                        playerView.stop(this);
                        finish();
                    }
                }).start();
                playerView.stop(this);
                finish();
            } else {
                playerView.stop(this);
                finish();
            }
        } catch (Exception e) {
            playerView.stop(this);
            finish();
        }

    }


}

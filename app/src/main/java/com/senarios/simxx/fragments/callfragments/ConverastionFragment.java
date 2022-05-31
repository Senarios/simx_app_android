package com.senarios.simxx.fragments.callfragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.quickblox.videochat.webrtc.BaseSession;
import com.quickblox.videochat.webrtc.QBMediaStreamManager;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionConnectionCallbacks;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;
import com.hdev.common.Constants;
import com.senarios.simxx.FragmentTags;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.callbacks.CallFragmentCallBacks;
import com.senarios.simxx.callbacks.CalltimeCallBack;
import com.senarios.simxx.callbacks.SessionController;
import com.senarios.simxx.databinding.FragmentConverastionBinding;

import org.webrtc.SurfaceViewRenderer;

import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConverastionFragment extends Fragment implements SessionController.QBRTCSessionUserCallback, FragmentTags,
        CalltimeCallBack,QBRTCClientVideoTracksCallbacks, QBRTCSessionConnectionCallbacks, SessionController.AudioStateCallback, Constants.CALL {
    private FragmentConverastionBinding binding;
    private ToggleButton cameraToggle,switchCameraToggle,dynamicToggleVideoCall,micToggleVideoCall;
    private ImageView handUpVideoCall,hangUpCall;
    private Chronometer timer;
    private SurfaceViewRenderer local,remote;
    private boolean isVideo=false;
    private SessionController controller;
    private QBRTCSession currentSession;
    private boolean isOutgoing=false;
    private  View view;
    private TextView call_type;
    private CallFragmentCallBacks fragmentCallBacks;
    private RelativeLayout video_call;
    private ImageView incoming_disconnect;



    public ConverastionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_converastion, container, false);
        binding= DataBindingUtil.bind(view);
        video_call=view.findViewById(R.id.video_call);


        if (getArguments()!=null){
            isVideo=getArguments().getBoolean(VIDEO);
            isOutgoing=getArguments().getBoolean(ISOUTGOING);
        }


        //initviews
        remote=view.findViewById(R.id.remoteview);
        if (Build.VERSION.SDK_INT<24)
            local = view.findViewById(R.id.localviwe);
        else
            local=view.findViewById(R.id.localviw);


        controller.addCalltime(this);

        if (isVideo) {
            switchCameraToggle = view.findViewById(R.id.switchCameraToggle);

           /* view.findViewById(R.id.video_call_settings_view).setVisibility(View.VISIBLE);*/
            cameraToggle = view.findViewById(R.id.cameraToggle);
            handUpVideoCall = view.findViewById(R.id.handUpVideoCall);
            timer = view.findViewById(R.id.video_call_timer);
            dynamicToggleVideoCall = view.findViewById(R.id.dynamicToggleVideoCall);
            micToggleVideoCall = view.findViewById(R.id.micToggleVideoCall);

            switchCameraToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final QBMediaStreamManager mediaStreamManager = currentSession.getMediaStreamManager();
                    if (mediaStreamManager == null) {
                        return;
                    }
                    mediaStreamManager.switchCameraInput(null);
                }
            });
            cameraToggle.setOnCheckedChangeListener((buttonView, isChecked) -> enableCamera(isChecked));
            dynamicToggleVideoCall.setChecked(true);

        } else {
            hangUpCall = view.findViewById(R.id.disconnect_button_ID);
            handUpVideoCall = view.findViewById(R.id.disconnct_button_ID2);
            timer = view.findViewById(R.id.call_timer);
            dynamicToggleVideoCall = view.findViewById(R.id.dynamicToggleAudioCall);
            micToggleVideoCall = view.findViewById(R.id.micToggleAudioCall);
            view.findViewById(R.id.audio_call).setVisibility(View.VISIBLE);
        }






        //ready for video callback
        if (isOutgoing){
            view.findViewById(R.id.calling).setVisibility(View.VISIBLE);
            call_type=view.findViewById(R.id.call_type);
            if (isVideo){
                call_type.setText("Outgoing Video Call..");
            }
           else  {
                call_type.setText("Outgoing Audio Call..");
            }
        }
        else{
            view.findViewById(R.id.incoming).setVisibility(View.VISIBLE);
            incoming_disconnect=view.findViewById(R.id.disconnect_incoming);
            incoming_disconnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    controller.hangUpCurrentSession("im busy");

                }
            });
            //incoming_disconnect.setOnClickListener(v -> );

        }

        initClicklisteners();
        initButtonsListener();

    return view;
    }


    private void initButtonsListener() {


        handUpVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.hangUpCurrentSession(" because I'm busy");
                SharedPreferences.Editor editor = getContext().getSharedPreferences("notificationString", Context.MODE_PRIVATE).edit();
                editor.putString("callreturn", "yes");
                editor.apply();
            }
        });



        dynamicToggleVideoCall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                controller.switchAudio();
            }
        });


        micToggleVideoCall.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                enableAudio(isChecked);
            }
        });

        /*hangUpCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                controller.hangUpCurrentSession("im busy");
            }
        });*/








    }


    private void initClicklisteners() {
    view.findViewById(R.id.disconnect_button_ID).setOnClickListener(v -> {

        controller.hangUpCurrentSession("im busy");
        fragmentCallBacks.OnChange(new OpponentFragment(),OPPONENTS_CALL_FRAGMENT);

    });


    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        controller=(SessionController)context;
        fragmentCallBacks=(CallFragmentCallBacks)context;

    }

    @Override
    public void onStart() {
        super.onStart();
        if (controller !=null && controller.getCurrentSession() !=null){
            currentSession=controller.getCurrentSession();

        }
        if (controller!=null && currentSession!=null) {
            controller.addTCClientConnectionCallback(this);
            controller.addRTCSessionUserCallback(this);
            controller.addAudioStateCallback(this);

            if (isVideo){
                if (controller!=null && controller.getCurrentSession()!=null) {
                    controller.addVideoTrackCallbacksListener(this);
                    if (currentSession.getMediaStreamManager() != null) {
                        currentSession.getMediaStreamManager().changeCaptureFormat(1280, 720, 30);
                    }
                }
            }
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentSession!=null && currentSession.getMediaStreamManager() !=null) {
            if (isVideo) {

                currentSession.getMediaStreamManager().startVideoSource();
                local.invalidate();
                local.requestLayout();
            }
        }
        }

    @Override
    public void onPause() {
        super.onPause();
        if (currentSession!=null && currentSession.getMediaStreamManager() !=null) {
            if (isVideo) {
                currentSession.getMediaStreamManager().stopVideoSource();

            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (currentSession!=null) {
            controller.removeRTCClientConnectionCallback(this);
            controller.removeRTCSessionUserCallback(this);

        }
    }

    @Override
    public void onLocalVideoTrackReceive(BaseSession baseSession, QBRTCVideoTrack qbrtcVideoTrack) {
        qbrtcVideoTrack.addRenderer(local);

    }

    @Override
    public void onRemoteVideoTrackReceive(BaseSession baseSession, QBRTCVideoTrack qbrtcVideoTrack, Integer integer) {
        qbrtcVideoTrack.addRenderer(remote);
        if (video_call!=null){
            video_call.invalidate();
            video_call.requestLayout();
        }
    }

    @Override
    public void onStartConnectToUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onDisconnectedTimeoutFromUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onConnectionFailedWithUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onStateChanged(QBRTCSession session, BaseSession.QBRTCSessionState qbrtcSessionState) {

    }

    @Override
    public void onConnectedToUser(QBRTCSession session, Integer integer) {

       if (timer!=null) {
           timer.setBase(SystemClock.elapsedRealtime());
           timer.start();
       }
        view.findViewById(R.id.calling).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.incoming).setVisibility(View.INVISIBLE);
        if (!isVideo){
            view.findViewById(R.id.audio_call).setVisibility(View.VISIBLE);
        }


    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession session, Integer integer) {

    }

    @Override
    public void onConnectionClosedForUser(QBRTCSession session, Integer integer) {
    }

    @Override
    public void onWiredHeadsetStateChanged(boolean plugged) {
    }

    @Override
    public void onUserNotAnswer(QBRTCSession session, Integer userId) {
        if (getActivity() != null) {
            Toast.makeText(getContext(), "User didn't answer", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onCallRejectByUser(QBRTCSession session, Integer userId, String userInfo) {
        if (controller!=null) {
            SharedPreferences.Editor editor = getContext().getSharedPreferences("notificationString", Context.MODE_PRIVATE).edit();
            editor.putString("callreturn", "yes");
            editor.apply();
            controller.hangUpCurrentSession("busy");
            getActivity().finish();
        }
    }

    @Override
    public void onCallAcceptByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
        view.findViewById(R.id.calling).setVisibility(View.GONE);
        if (!isVideo){
            view.findViewById(R.id.audio_call).setVisibility(View.VISIBLE);

        }

    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession session, Integer userId, String userInfo) {
       if (isOutgoing){
           SharedPreferences.Editor editor = getContext().getSharedPreferences("notificationString", Context.MODE_PRIVATE).edit();
           editor.putString("callreturn", "yes");
           editor.apply();
           fragmentCallBacks.OnChange(new OpponentFragment(),OPPONENTS_CALL_FRAGMENT);
       }
       else{
           if (getActivity()!=null){
               SharedPreferences.Editor editor = getContext().getSharedPreferences("notificationString", Context.MODE_PRIVATE).edit();
               editor.putString("callreturn", "yes");
               editor.apply();
               getActivity().finish();

           }
       }

    }

    //setting functions for views which effect qb call

    private void enableCamera(boolean isNeedEnableCam) {
        if (currentSession != null) {
            currentSession.getMediaStreamManager().setVideoEnabled(isNeedEnableCam);
        }
    }


    private void enableAudio(boolean enable) {
        if (currentSession != null) {
            currentSession.getMediaStreamManager().setAudioEnabled(enable);
        }
    }


    @Override
    public String OnFinished() {
        return timer.getText().toString();
    }



}

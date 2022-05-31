package com.senarios.simxx.fragments.callfragments;


import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.se.omapi.Session;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.firebase.FirebaseApp;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.BaseSession;
import com.quickblox.videochat.webrtc.QBPeerConnection;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionConnectionCallbacks;
import com.senarios.simxx.R;
import com.senarios.simxx.RingtonePlayer;
import com.senarios.simxx.Utility;
import com.senarios.simxx.activities.CallActivity;
import com.senarios.simxx.databinding.FragmentIncomingCallBinding;
import com.senarios.simxx.fragments.BaseFragment;

import org.jivesoftware.smack.ConnectionListener;

import java.io.Serializable;
import java.util.HashMap;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * A simple {@link Fragment} subclass.,
 */
public class IncomingCallFragment extends BaseFragment implements View.OnClickListener, Serializable {
    private FragmentIncomingCallBinding binding;
    private RingtonePlayer ringtonePlayer;
    private Vibrator vibrator;
    private QBRTCSession session;

    public IncomingCallFragment() {
        // Required empty public constructor
    }



    @Override
    protected void init() {
        super.init();
        ringtonePlayer = new RingtonePlayer(getActivity());

        //call
        binding.acceptButton.setOnClickListener(this);
        binding.rejectButton.setOnClickListener(this);


        NotificationManager manager = (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(3);

        //check and change title call text based on received session. for that we get session from callactivity using getsession
        //function
        if (getActivity()!=null){
            session=((CallActivity) getActivity()).getCurrentSession();
            if (session!=null){
                if (session.getConferenceType().equals(QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO)){
                    binding.type.setText("Incoming Video Call");
                }
                else{
                    Utility.showLog("Session "+session.getSessionID());
                    binding.type.setText("Incoming Audio Call");
                }

            }
        }
        QBUsers.getUser(session.getCallerID()).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                binding.callerName.setText(qbUser.getFullName());
            }

            @Override
            public void onError(QBResponseException e) {
            }
        });




    }


    @Override
    protected View getview(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_incoming_call, container, false);
        binding= DataBindingUtil.bind(view);

        init();

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reject_button:
                RejectCall();
                break;
            case R.id.accept_button:
                AcceptCall();
                break;
            default:
                break;
        }

    }

    private void AcceptCall() {
        Utility.showLog("accept clicked");
        Activity activity=getActivity();
        binding.acceptButton.setClickable(false);
        stopCallNotification();

        if (activity!=null) {
            ((CallActivity) activity).addConversationFragmentReceiveCall();
        }
    }

    private void RejectCall() {
        Utility.showLog("reject clicked");
        Activity activity=getActivity();
        stopCallNotification();
        if (activity!=null){
            session.rejectCall(new HashMap<>());
            activity.finish();
        }
        binding.rejectButton.setClickable(false);
    }



    private void stopCallNotification(){
        try {
            if (ringtonePlayer != null) {
                ringtonePlayer.stop();
            }
            if (vibrator != null) {
                vibrator.cancel();
            }

        }
        catch (Exception e){
            Utility.showLog(""+e.getMessage());
        }

    }
    private void startCallNotification() {
        ringtonePlayer.play(false);
        if (getActivity()!=null) {
            vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        }
        long[] vibrationCycle = {0, 1000, 1000};
        if (vibrator!=null && vibrator.hasVibrator()) {
            vibrator.vibrate(vibrationCycle, 1);
        }



    }



    @Override
    public void onStart() {
        super.onStart();
        startCallNotification();
    }

    @Override
    public void onStop() {
        super.onStop();
        Utility.showLog("ON STOP");
        stopCallNotification();
    }



}

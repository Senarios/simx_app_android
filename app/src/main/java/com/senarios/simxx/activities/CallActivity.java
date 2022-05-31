package com.senarios.simxx.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.FirebaseApp;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.AppRTCAudioManager;
import com.quickblox.videochat.webrtc.BaseSession;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.QBSignalingSpec;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionConnectionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSignalingCallback;
import com.quickblox.videochat.webrtc.exception.QBRTCSignalException;
import com.hdev.common.Constants;
import com.senarios.simxx.FragmentTags;
import com.senarios.simxx.R;
import com.senarios.simxx.RingtonePlayer;
import com.senarios.simxx.Utility;
import com.senarios.simxx.callbacks.CallFragmentCallBacks;
import com.senarios.simxx.callbacks.CalltimeCallBack;
import com.senarios.simxx.callbacks.OnCallSettingsController;
import com.senarios.simxx.callbacks.SessionController;
import com.hdev.common.datamodels.Users;
import com.senarios.simxx.fragments.callfragments.ConverastionFragment;
import com.senarios.simxx.fragments.callfragments.IncomingCallFragment;
import com.senarios.simxx.fragments.callfragments.OpponentFragment;
import com.senarios.simxx.viewmodels.SharedVM;

import org.webrtc.RendererCommon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

import static com.hdev.common.Constants.DreamFactory.URL;
import static com.hdev.common.Constants.Messages.ERROR;
import static com.hdev.common.Constants.Messages.NETWORK_ERROR;
import static com.hdev.common.Constants.SharedPreference.USER;

public class CallActivity extends AppCompatActivity implements QBRTCClientSessionCallbacks,
        SessionController, FragmentTags, Constants.CALL, CallFragmentCallBacks, Constants.QB, QBRTCSignalingCallback,
        QBSignaling, QBRTCSessionStateCallback, OnCallSettingsController, Runnable, AppRTCAudioManager.AudioManagerEvents {
    private QBRTCClient rtcClient;
    private AppRTCAudioManager audioManager;
    private AudioStateCallback audioStateCallback;
    private SharedVM sharedVM;
    private QBRTCSession currentSession;
    private QBRTCSessionUserCallback userCallback;
    private ProgressDialog pd;
    private boolean isIncoming = false;
    private RingtonePlayer ringtonePlayer;
    private CalltimeCallBack calltimeCallBack;
    private Handler h = new Handler();
    private Runnable r;
    private Users opponent_user;
    private final String TAG = "";
    private String rate = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                + WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                + WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_call_activiy);


        //flag to keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        //initviewmodel
        sharedVM = new ViewModelProvider(this).get(SharedVM.class);


        pd = Utility.setDialogue(this);

        if (getIntent().getBooleanExtra(QB_INCOMING_CALL, false)) {
            pd.show();
            isIncoming = true;
        }

        //init things required for qb like audio and video things.
        initSignal();
        initAudioManager();

        //
        if (getIntent().getSerializableExtra(QB_OPPONENT_USER) != null) {
            opponent_user = (Users) getIntent().getSerializableExtra(QB_OPPONENT_USER);
        }


        //initChatCallback
        ringtonePlayer = new RingtonePlayer(this, R.raw.beep);


        //if you want to call replace this on start
        if (!isIncoming) {
            changeFragment(new OpponentFragment(), OPPONENTS_CALL_FRAGMENT);
        }


        //runnable to check if the session is received or not, to wait more for session, increase the value in post delayed
        if (isIncoming) {
            r = this;
            h.postDelayed(r, 45000);
        }


    }


    //custom functions
    private void initSignal() {

        try {
            initQBRTCClient();
        } catch (Exception e) {
            e.printStackTrace();
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(this);
            builder
                    .setMessage("Unable to receieve call, please try again later")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

        }
    }

    private void initQBRTCClient() {
        rtcClient = QBRTCClient.getInstance(this);

        QBChatService.getInstance().getVideoChatWebRTCSignalingManager().addSignalingManagerListener(new QBVideoChatSignalingManagerListener() {
            @Override
            public void signalingCreated(QBSignaling qbSignaling, boolean createdLocally) {

                if (!createdLocally) {

                    rtcClient.addSignaling(qbSignaling);

                }
            }
        });


        QBRTCConfig.setMaxOpponentsCount(9);
        QBRTCConfig.setDisconnectTime(80);
        QBRTCConfig.setAnswerTimeInterval(60);
        QBRTCConfig.setStatsReportInterval(60);
        QBRTCConfig.setDebugEnabled(false);


        rtcClient.addSessionCallbacksListener(this);
        rtcClient.prepareToProcessCalls();


    }


    public void initCurrentSession(QBRTCSession session) {
        this.currentSession = session;
        session.addSignalingCallback(this);
        session.addSessionCallbacksListener(this);


    }

    public void releaseCurrentSession(QBRTCSession session) {
        currentSession = null;
        session.removeSessionCallbacksListener(this);
        session.removeSessionCallbacksListener(this);


    }

    private void initAudioManager() {
        try {
            audioManager = AppRTCAudioManager.create(this);
            audioManager.setOnWiredHeadsetStateListener((plugged, hasMicrophone) -> {

                if (audioStateCallback != null) {
                    audioStateCallback.onWiredHeadsetStateChanged(plugged);
                }
            });
        } catch (Exception e) {
            Log.v("CallActivity", "" + e.getMessage());
        }

    }


    public void addConversationFragmentReceiveCall() {
        if (getCurrentSession() != null) {
            getCurrentSession().acceptCall(getCurrentSession().getUserInfo());
            Fragment f = new ConverastionFragment();
            Bundle bundle = new Bundle();
            if (currentSession.getConferenceType() == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO) {
                bundle.putBoolean(VIDEO, true);
            } else {
                bundle.putBoolean(VIDEO, false);
            }
            f.setArguments(bundle);
            changeFragment(f, CONVERSATION_CALL_FRAGMENT);
            try {
                audioManager.start(this);
            } catch (Exception e) {
                Log.v("simxaudioissue", "" + e.getMessage());
            }

        }
    }

    public void addConversationFragmentStartCall(List<Integer> userids, QBRTCTypes.QBConferenceType qbConferenceType) {
        if (QBChatService.getInstance().isLoggedIn()) {
            QBRTCSession opponentSession = rtcClient.createNewSessionWithOpponents(userids, qbConferenceType);
            initCurrentSession(opponentSession);
            Map<String, String> map = new HashMap<>();
            map.put("call", "start");
            opponentSession.startCall(map);
            Fragment f = new ConverastionFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean(ISOUTGOING, true);
            if (qbConferenceType == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO) {
                bundle.putBoolean(VIDEO, true);
            } else {
                bundle.putBoolean(VIDEO, false);
                changeAudioSource(qbConferenceType);
            }

            f.setArguments(bundle);
            changeFragment(f, CONVERSATION_CALL_FRAGMENT);
            try {
                ringtonePlayer.play(true);
                audioManager.start(this);

            } catch (Exception e) {
                Log.v("simxaudioissue", "" + e.getMessage());
            }
        } else {
            Toast.makeText(this, "Something went wrong.", Toast.LENGTH_SHORT).show();
        }
    }


    ///qb session callbacks


    @Override
    public void onReceiveNewSession(QBRTCSession session) {
        if (getCurrentSession() == null) {
            initCurrentSession(session);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                    | WindowManager.LayoutParams.FLAG_FULLSCREEN);

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            runOnUiThread(() -> {
                try {
                    if (pd != null && pd.isShowing()) {
                        pd.dismiss();

                    }

                } catch (NullPointerException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            changeAudioSource(currentSession.getConferenceType());

            //load incoming call fragment
            changeFragment(new IncomingCallFragment(), INCOME_CALL_FRAGMENT);

        } else {
            Map<String, String> infoMap = new HashMap<>();
            infoMap.put(REJECT_REASON, "I'm on a call right now!");
            session.rejectCall(infoMap);
        }


    }

    private void changeAudioSource(QBRTCTypes.QBConferenceType types) {
        try {
            if (types == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO) {
                audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
                audioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
            } else {
                audioManager.setDefaultAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
                audioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
            }

        } catch (Exception e) {
            Log.v("simxaudioissue", "" + e.getMessage());
        }
    }

    public void rejectCurrentSession(String rejectReason) {
        if (getCurrentSession() != null) {
            Map<String, String> infoMap = new HashMap<>();
            infoMap.put(REJECT_REASON, rejectReason);
            getCurrentSession().rejectCall(infoMap);
        }
    }


    @Override
    public void onSessionStartClose(QBRTCSession session) {
        session.removeSessionCallbacksListener(this);
    }


    @Override
    public void onUserNoActions(QBRTCSession session, Integer integer) {


    }


    //overried from sessioneventcallback
    @Override
    public void onSessionClosed(QBRTCSession qbrtcSession) {
        if (audioManager != null) {
            try {
                audioManager.stop();
            } catch (Exception e) {
                Log.v("simaudioissue", e.getMessage());
            }

        }


        if (getSupportFragmentManager().findFragmentByTag(CONVERSATION_CALL_FRAGMENT) != null) {
            getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentByTag(CONVERSATION_CALL_FRAGMENT));
        }
        releaseCurrentSession(qbrtcSession);

        //getcalltime
        if (calltimeCallBack != null) {
            String call_time = calltimeCallBack.OnFinished();
            if (call_time != null) {
                sharedVM.getSharedPreference().edit().putString(CALL_TIME, call_time).apply();
            }
        }
    }


// callbacks from QB session event interface from QB SESSION CALLBACK

    @Override
    public void onUserNotAnswer(QBRTCSession qbrtcSession, Integer integer) {
        if (userCallback != null) {
            userCallback.onUserNotAnswer(qbrtcSession, integer);
        }
        ringtonePlayer.stop();
        changeFragment(new OpponentFragment(), OPPONENTS_CALL_FRAGMENT);
    }

    @Override
    public void onCallRejectByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        if (userCallback != null) {
            userCallback.onCallRejectByUser(qbrtcSession, integer, "accepted");
        }
        ringtonePlayer.stop();
        changeFragment(new OpponentFragment(), OPPONENTS_CALL_FRAGMENT);
    }

    @Override
    public void onCallAcceptByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        if (userCallback != null) {
            userCallback.onCallAcceptByUser(qbrtcSession, integer, map);
        }
        ringtonePlayer.stop();
    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        if (userCallback != null) {
            userCallback.onReceiveHangUpFromUser(qbrtcSession, integer, "rejected");
        }
        ringtonePlayer.stop();
        finish();
    }


//session state like callbacks

    @Override
    public void onStateChanged(BaseSession baseSession, BaseSession.QBRTCSessionState qbrtcSessionState) {


    }

    @Override
    public void onConnectedToUser(BaseSession baseSession, Integer integer) {


    }

    @Override
    public void onDisconnectedFromUser(BaseSession baseSession, Integer integer) {
        finish();
    }

    @Override
    public void onConnectionClosedForUser(BaseSession baseSession, Integer integer) {
        finish();
    }


    //session controller from callback package callbacks here


    @Override
    public void hangUpCurrentSession(String hangUpReason) {
        if (ringtonePlayer != null) {
            ringtonePlayer.stop();
        }
        if (currentSession != null) {
            Map<String, String> info = new HashMap<>();
            info.put(HANG_UP_REASON, hangUpReason);
            currentSession.hangUp(info);
        }
        if (isIncoming) {
            finish();
        } else {
            changeFragment(new OpponentFragment(), OPPONENTS_CALL_FRAGMENT);
            if (calltimeCallBack != null && calltimeCallBack.OnFinished() != null) {
                if (!opponent_user.getRate().isEmpty()) {
                    if (Integer.valueOf(opponent_user.getRate()) > 0) {
                        String time = calltimeCallBack.OnFinished();
                        int minutes = Integer.valueOf(time.split(":")[0]);
                        int seconds = Integer.valueOf(time.split(":")[1]);
                        if (minutes >= 5 && seconds > 0) {
                            // if (minutes > 0 || seconds > 0)
                            minutes = minutes - 5;
                            float cost = (minutes * 60) + seconds;
                            cost = cost / 3600;
                            cost = cost * Float.valueOf(opponent_user.getRate());
                            Users myuser = sharedVM.getLoggedUser();
                            myuser.setCredit(myuser.getCredit() - cost);
                            opponent_user.setCredit(opponent_user.getCredit() + cost);
                            updateUser(myuser, USER);
                        }
                    }
                }
            }


        }
    }

    @Override
    public QBRTCSession getCurrentSession() {
        return currentSession;
    }

    @Override
    public void addVideoTrackCallbacksListener(QBRTCClientVideoTracksCallbacks videoTracksCallbacks) {
        if (currentSession != null) {
            currentSession.addVideoTrackCallbacksListener(videoTracksCallbacks);
        }
    }

    @Override
    public void addTCClientConnectionCallback(QBRTCSessionConnectionCallbacks sessionConnectionCallbacks) {
        if (currentSession != null) {
            currentSession.addSessionCallbacksListener(sessionConnectionCallbacks);
        }
    }

    @Override
    public void addRTCSessionUserCallback(QBRTCSessionUserCallback qbrtcSessionUserCallback) {
        this.userCallback = qbrtcSessionUserCallback;
    }

    @Override
    public void addCalltime(CalltimeCallBack calltimeCallBack) {
        this.calltimeCallBack = calltimeCallBack;

    }

    @Override
    public void removeRTCClientConnectionCallback(QBRTCSessionConnectionCallbacks sessionConnectionCallbacks) {
        if (currentSession != null) {
            currentSession.removeSessionCallbacksListener(sessionConnectionCallbacks);
        }

    }

    @Override
    public void removeRTCSessionUserCallback(QBRTCSessionUserCallback sessionUserCallback) {

    }

    @Override
    public void addAudioStateCallback(AudioStateCallback audioStateCallback) {
        this.audioStateCallback = audioStateCallback;
    }

    @Override
    public void switchAudio() {
        if (audioManager.getSelectedAudioDevice() == AppRTCAudioManager.AudioDevice.WIRED_HEADSET
                || audioManager.getSelectedAudioDevice() == AppRTCAudioManager.AudioDevice.EARPIECE) {
            audioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
        } else {
            audioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
        }
    }

    @Override
    public void onUseHeadSet(boolean use) {

    }

    @Override
    public void onVideoScalingSwitch(RendererCommon.ScalingType scalingType) {

    }


    @Override
    public void onSwitchAudio() {
        if (audioManager.getSelectedAudioDevice() == AppRTCAudioManager.AudioDevice.WIRED_HEADSET
                || audioManager.getSelectedAudioDevice() == AppRTCAudioManager.AudioDevice.EARPIECE) {
            audioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
        } else {
            audioManager.selectAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
        }
    }

    @Override
    public void onCaptureFormatChange(int width, int height, int framerate) {

    }


    //fragments callbacks

    @Override
    public void OnChange(Fragment fragment, String tag) {
        changeFragment(fragment, tag);
    }


    //function to change delete fragment
    public void changeFragment(Fragment fragment, String tag) {
        FragmentTransaction fm = getSupportFragmentManager().beginTransaction();
        fm.replace(R.id.container, fragment, tag);
        fm.commitAllowingStateLoss();
    }

    private Fragment findFragment(String tag) {
        return getSupportFragmentManager().findFragmentByTag(tag);

    }


    @Override
    public void onBackPressed() {
        if (findFragment(OPPONENTS_CALL_FRAGMENT) != null && findFragment(OPPONENTS_CALL_FRAGMENT).isVisible()) {
            super.onBackPressed();
        }

    }

    //Ativitylifecycle
    @Override
    protected void onDestroy() {
        super.onDestroy();
        QBRTCClient.getInstance(this).destroy();
        try {
            audioManager.stop();
        } catch (Exception e) {
            Log.v("simxaudioissue", "" + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }


    @Override
    public void onSuccessSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer integer) {

    }

    @Override
    public void onErrorSendingPacket(QBSignalingSpec.QBSignalCMD qbSignalCMD, Integer integer, QBRTCSignalException e) {

    }


    @Override
    public void run() {
        if ((pd != null && pd.isShowing()) && !isFinishing()) {
            pd.dismiss();
            finish();
        }

    }

    private void updateUser(Users user, String type) {
        if (Utility.isNetworkAvailable(this)) {
            if (!isFinishing()) {
                pd.show();
            }
            HashMap<String, Object> map = new HashMap<>();
            map.put("resource", user);
            sharedVM.getService(URL).updateUser(map)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Response<JsonObject>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Response<JsonObject> response) {

                            if (response.isSuccessful()) {
                                if (response.code() == 200) {
                                    if (response.body() != null) {
                                        if (type.equalsIgnoreCase(USER)) {
                                            sharedVM.getSharedPreference().edit().putString(USER, new Gson().toJson(user)).apply();
                                            updateUser(opponent_user, QB_OPPONENT_USER);
                                        }
                                        if (type.equalsIgnoreCase(QB_OPPONENT_USER)) {
                                            if (!isFinishing()) {
                                                pd.dismiss();
                                            }
                                        }
                                    } else {
                                        pd.dismiss();
                                        Toast.makeText(getApplicationContext(), ERROR, Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    pd.dismiss();
                                    Toast.makeText(getApplicationContext(), ERROR, Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                pd.dismiss();
                                Toast.makeText(getApplicationContext(), ERROR, Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onError(Throwable e) {
                            pd.dismiss();
                            Toast.makeText(getApplicationContext(), ERROR, Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {
            Toast.makeText(this, NETWORK_ERROR, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onAudioDeviceChanged(AppRTCAudioManager.AudioDevice audioDevice, Set<AppRTCAudioManager.AudioDevice> set) {

    }
}

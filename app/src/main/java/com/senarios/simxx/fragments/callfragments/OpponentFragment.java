package com.senarios.simxx.fragments.callfragments;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.messages.model.QBNotificationType;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.hdev.common.Constants;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.activities.CallActivity;
import com.senarios.simxx.databinding.FragmentOpponentBinding;
import com.senarios.simxx.fragments.BaseFragment;
import com.senarios.simxx.services.QbSignUpService;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

import static org.greenrobot.eventbus.EventBus.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class OpponentFragment extends BaseFragment implements View.OnClickListener, Serializable , Constants.QB, Constants.SharedPreference{
    private FragmentOpponentBinding binding;
    private QBRTCTypes.QBConferenceType qbConferenceType = null;


    public OpponentFragment() {
        // Required empty public constructor
    }



    @Override
    protected View getview(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_opponent, container, false);
        binding= DataBindingUtil.bind(view);
        init();
        return view;
    }

    @Override
    protected void init() {
        super.init();
        binding.btnAudioCall.setOnClickListener(this);
        binding.btnVideoCall.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAudioCall:
                qbConferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;
                qbsignin();

                break;

            case R.id.btnVideoCall:
                qbConferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO;
                qbsignin();

                break;
        }

    }
   private void qbsignin(){
        getDialog().show();
        QBUsers.signIn(new QBUser(getViewModel().getSharedPreference().getString(Email,""), Constants.QB.QB_DEFAULT_PASSWORD)).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                sendNotification();
            }



            @Override
            public void onError(QBResponseException e) {
                Utility.showLog("HELP "+e.getLocalizedMessage());
                requireActivity().finish();

            }
        });
    }
    private void sendNotification(){
        try
        {
            StringifyArrayList<Integer> userIds = new StringifyArrayList<Integer>();
            userIds.add(getViewModel().getSharedPreference().getInt(QB_OPPONENT_USER,0));

            /*change environment to production or development depending on the build type*/
            QBEvent event = new QBEvent();
            try {
                event.setUserIds(userIds);
            }
            catch (Exception e)
            {
                Log.d(TAG, "sendNotification: message" + e);
            }
            event.setEnvironment(Utility.getDefaultEnvironment());
            event.setNotificationType(QBNotificationType.PUSH);
            JSONObject json = new JSONObject();
            try {
                json.put("message", getViewModel().getLoggedUser().getName()+" is calling you");
                json.put("VOIPCall","1");
                json.put("ios_voip",1 );

            } catch (Exception e) {
                e.printStackTrace();
            }

            event.setMessage(json.toString());

            try {
                QBPushNotifications.createEvent(event).performAsync(new QBEntityCallback<QBEvent>() {
                                                                        @Override
                                                                        public void onSuccess(QBEvent qbEvent, Bundle args) {
                                                                            if (getActivity() != null) {
                                                                                ((CallActivity) getActivity()).addConversationFragmentStartCall(getOpponentsIds(getViewModel().getSharedPreference().getInt(QB_OPPONENT_USER, -1)), qbConferenceType);

                                                                            }
                                                                            getDialog().dismiss();

                                                                        }

                                                                        @Override
                                                                        public void onError(QBResponseException errors) {
                                                                            if (getActivity() != null) {
                                                                                ((CallActivity) getActivity()).addConversationFragmentStartCall(getOpponentsIds(getViewModel().getSharedPreference().getInt(QB_OPPONENT_USER, -1)), qbConferenceType);
                                                                                Toast.makeText(getActivity(), errors.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                                            }
                                                                            getDialog().dismiss();

                                                                        }
                                                                    }


                );
            }
            catch (Exception e)
            {
                Log.d(TAG, "sendNotification: message:"+e);
            }

        }
        catch (Exception e){
            getDialog().dismiss();
            Toast.makeText(getContext(), "Something went wrong.", Toast.LENGTH_SHORT).show();
            Utility.showLog(""+e.getLocalizedMessage());
            requireActivity().finish();
        }
    }

    private ArrayList<Integer> getOpponentsIds(int opponents) {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        ids.add(opponents);
        return ids;
    }
}

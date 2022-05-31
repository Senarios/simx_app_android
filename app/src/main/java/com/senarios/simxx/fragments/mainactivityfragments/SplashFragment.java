package com.senarios.simxx.fragments.mainactivityfragments;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hdev.common.Constants;
import com.hdev.common.datamodels.Events;
import com.hdev.common.datamodels.ResponseUsers;
import com.hdev.common.datamodels.Users;
import com.hdev.common.retrofit.ApiResponse;
import com.hdev.common.retrofit.NetworkCall;
import com.senarios.simxx.FragmentTags;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.activities.MainActivity;
import com.senarios.simxx.databinding.FragmentSplashBinding;
import com.senarios.simxx.fragments.BaseFragment;
import com.senarios.simxx.fragments.homefragments.BroadcastsFragment;

import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class SplashFragment extends BaseFragment implements ApiResponse {
    private FragmentSplashBinding binding;
    private static final int delay = 1000;
    final Handler handler = new Handler();
    private FirebaseAuth auth;
    public static Users savedUser;


    public SplashFragment() {
        // Required empty public constructor
    }


    @Override
    protected View getview(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_splash, container, false);
        binding= DataBindingUtil.bind(view);
        return view;
    }



    @Override
    public void onResume() {
        super.onResume();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                callAPi();  //Do something after 100ms
//                auth = FirebaseAuth.getInstance();
//                FirebaseUser currentUser = auth.getCurrentUser();
//                Fragment fragment;
//                if (currentUser == null){
//                    fragment = new LoginWithLinkedIn();
//                }else {
//                    fragment = new BroadcastsFragment();
//                }
//                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                fragmentTransaction.replace(R.id.fragment_container, fragment);
//                fragmentTransaction.addToBackStack(null);
//                fragmentTransaction.commit();
//                getActivity().finish();
            }
        }, delay);

    }

    private void callAPi() {
        try {
            NetworkCall.CallAPI(requireContext(), Utility.getService(DreamFactory.URL).checkUser(getViewModel().getLoggedUser().getUsername(),DreamFactory.CV_RELATED)
            ,this,false, Users.class,Endpoints.POST_USER
            );
        }
        catch (Exception e){
            setFlow();
            Utility.showELog(e);
        }

    }


    private void setFlow(){
        if (getViewModel()!=null){
            if (getViewModel().getSharedPreference().getBoolean(Constants.SharedPreference.Login_Boolean, false)) {
                getActivityContainer().OnFragmentChange(new HomeFragment(), FragmentTags.HOME);
            }
            else{
                getActivityContainer().OnFragmentChange(new LoginWithLinkedIn(), FragmentTags.LOGIN);

            }
        }
    }



    @Override
    public void OnSuccess(Response<JsonObject> response, Object body, String endpoint) {
        if (body instanceof Users){
            savedUser = new Gson().fromJson(String.valueOf(body), Users.class);
            getViewModel().setPreferences( SharedPreference.USER,new Gson().toJson(body));
            setFlow();
            }
            else{
               if (requireActivity() instanceof MainActivity){
                   ((MainActivity) requireActivity()).logout(Events.Logout);
               }
            }


    }

    @Override
    public void OnError(Response<JsonObject> response, String endpoint) {
        if (response.code()==404){
            if (requireActivity() instanceof MainActivity){
                ((MainActivity) requireActivity()).logout(Events.Logout);
            }
        }
        else{
            setFlow();
        }

    }

    @Override
    public void OnException(Throwable e, String endpoint) {
        setFlow();
    }

    @Override
    public void OnNetWorkError(String endpoint, String message) {
        setFlow();
    }
}

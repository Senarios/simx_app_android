package com.senarios.simxx.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.gson.Gson;
import com.hdev.common.Constants;
import com.senarios.simxx.DreamFactoryFilters;
import com.senarios.simxx.FragmentTags;
import com.senarios.simxx.activities.MainActivity;
import com.senarios.simxx.callbacks.ActivityContainerCallback;
import com.senarios.simxx.callbacks.HomeContainerCallback;
import com.hdev.common.datamodels.Users;
import com.senarios.simxx.fragments.mainactivityfragments.HomeFragment;
import com.senarios.simxx.viewmodels.SharedVM;

import org.greenrobot.eventbus.EventBus;

import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 */
public abstract class BaseFragment extends Fragment implements DreamFactoryFilters,FragmentTags,Constants,Constants.DreamFactory,
        Constants.SharedPreference,Constants.GoCoder,Constants.Messages,Constants.AUTH,Constants.Twitter,Constants.LinkedIn,
        Constants.QB_CREDENTIALS,Constants.QB,Constants.Paypal,Constants.CALL {
    private SharedVM sharedVM;
    private ActivityContainerCallback activityContainerCallback;
    private HomeContainerCallback homeContainer;
    private ProgressDialog dialog;

    public BaseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            activityContainerCallback = (ActivityContainerCallback) context;
        }
        if (getParentFragment()!=null && getParentFragment() instanceof HomeFragment){
            homeContainer=(HomeContainerCallback) getParentFragment();
        }
        sharedVM= new ViewModelProvider(requireActivity()).get(SharedVM.class);
    }

    public SharedVM getViewModel(){
        return sharedVM;
    }

    public ActivityContainerCallback getActivityContainer() {
        return activityContainerCallback;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return getview(inflater,container,savedInstanceState);

    }

    protected void init(){
        setDialogue();

    }

    public HomeContainerCallback getHomeContainer() {
        return homeContainer;
    }

    protected abstract View getview(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public ProgressDialog setDialogue() {
        dialog = new ProgressDialog(requireContext());
        dialog.setCancelable(false);
        dialog.setMessage("Please Wait..");
        return dialog;
    }

    public ProgressDialog getDialog() {
        return dialog;
    }

    public static String getString(EditText editText){
        return editText.getText().toString().trim();
    }

    public static SharedPreferences getPreference(Context context){
        return context.getSharedPreferences(Preference,Preference_Mode);

    }
    public static Users getUser(Context context){
        return new Gson().fromJson(context.getSharedPreferences(Preference,Preference_Mode).getString(USER,""),Users.class );

    }

    public static Integer getColor(){
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }



    @Override
    public void onResume() {
        super.onResume();
        try {
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this);
            }
        }
        catch (Exception e){

        }

    }

    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getActivity().getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
        if (EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this);
        }
    }
        catch (Exception e){

    }
    }
}

package com.senarios.simxx.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.gson.Gson;
import com.hdev.common.Constants;
import com.hdev.common.datamodels.Users;
import com.senarios.simxx.viewmodels.SharedVM;

import java.util.Random;

public abstract class BaseActivity extends AppCompatActivity implements Constants,
        Constants.SharedPreference,Constants.GoCoder,Constants.Messages,Constants.AUTH,Constants.Twitter,Constants.LinkedIn,
        Constants.QB_CREDENTIALS,Constants.QB,Constants.Paypal,Constants.CALL {
    private ProgressDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getWindow().getDecorView().setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        }

        setDialogue();
        binding();
        getData(getIntent());
        init();
    }


    public void getData(Intent intent) {

    }

    public ViewDataBinding binding(){
        return null;
    }

    public void init(){

    }


    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void setDialogue() {
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Please Wait..");
    }

    public ProgressDialog getDialog() {
        return dialog;
    }

    public String getString(EditText editText){
        return editText.getText().toString().trim();
    }
    public SharedVM getViewModel(){
        return new ViewModelProvider(this).get(SharedVM.class);
    }

    public static void displayLocationSettingsRequest(final Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i("asd", "All location settings are satisfied.");

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i("asd", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult((Activity) context, 101);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i("asd", "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i("asd", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    public  SharedPreferences getSharedPreference(){
        return getSharedPreferences(Preference,Preference_Mode);
    }

    public static Users getUser(Context context){
        return new Gson().fromJson(context.getSharedPreferences(Preference,Preference_Mode).getString(USER,""),Users.class );
    }

    //unused fragments functions
    public void removeFragment(String tag) {
        Fragment removefragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (removefragment != null) {
            getSupportFragmentManager().beginTransaction().remove(removefragment).commit();
        }
    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    public void keepScreenOn(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public static Integer getColor(){
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

    public static AlertDialog.Builder getAlertDialoge(Context context, String title, String message){
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(context);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("Ok", (dialog, which) -> dialog.dismiss());
        return alertDialog;
    }

    public void setPreferences(String key,Object object){
        if (getSharedPreference()!=null) {
            if (object instanceof String) {
                getSharedPreference().edit().putString(key,(String)object).apply();
            } else if (object instanceof Integer) {
                getSharedPreference().edit().putInt(key,(Integer)object).apply();
            } else if (object instanceof Boolean) {
                getSharedPreference().edit().putBoolean(key,(Boolean)object).apply();
            } else if (object instanceof Users) {
                getSharedPreference().edit().putString(key,new Gson().toJson(object)).apply();
            }
        }
    }

    public Object getPreferences(String key,Object object) {
        if (getSharedPreference() != null) {
            if (object instanceof String) {
                return getSharedPreference().getString(key, "");
            } else if (object instanceof Integer) {
                return getSharedPreference().getInt(key, -1);
            } else if (object instanceof Boolean) {
                return getSharedPreference().getBoolean(key, false);
            }
        }
        return "";
    }

    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) this.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(
                    this.getCurrentFocus().getWindowToken(), 0);
        }
    }
}

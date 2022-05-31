package com.hdev.common;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import okhttp3.internal.Util;


/*
* helper class for location created by Hashim
* */
@SuppressLint("MissingPermission")
public class LocationHelper extends LifeCycleObserver implements OnFailureListener, OnSuccessListener<LocationSettingsResponse> {
    private Context context;
    private LocationHelperCallback callback;
    private LocationSettingsRequest.Builder builder;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private MutableLiveData<Location> mLocationObserver=new MutableLiveData<>();
    public static final int CODE = 386;
    public String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};


    public LocationHelper(@NonNull Context context,@NonNull  LocationHelperCallback callback) {
        this.context = context;
        this.callback = callback;
    }


    public LocationHelper(@NonNull Context context) {
        this.context = context;
    }


    @Override
    protected void onCreate() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(200);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        builder=new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null&& mLocationObserver!=null) {
                    mLocationObserver.setValue(locationResult.getLastLocation());
                }
            }
        };

    }

    private void locationRequestTask() {
        if (context!=null && builder!=null) {
            SettingsClient client = LocationServices.getSettingsClient(context);
            Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
            task.addOnSuccessListener(this);
            task.addOnFailureListener(this);
        }

    }


    @Override
    protected void onResume() {
        if (CommonUtils.hasPermissions(context,PERMISSIONS) && builder!=null) {
            locationRequestTask();
        }
        else{
            try {
                if (CommonUtils.shouldShowRationalPermission((Activity) context, PERMISSIONS)){
                    callback.onPermissionDenied();
                }
                else{
                    callback.onPermissionNeeded();
                }

            }
            catch (Exception e){
                CommonUtils.showELog(e);
            }
            }

    }

    @Override
    protected void onPause() {
        removeRequestLocation();
    }

    @Override
    protected void onStart() {

    }

    @Override
    protected void onStop() {

    }

    @Override
    protected void onDestroy() {
        context=null;
        locationCallback=null;
        builder=null;
        mLocationObserver=null;
        locationRequest=null;
        callback=null;
    }

    public LiveData<Location> getLocation() {
        return mLocationObserver;
    }

    @Override
    public void onFailure(@NonNull Exception e) {
        if (e instanceof ResolvableApiException){
            try {
                ((ResolvableApiException) e).startResolutionForResult((Activity) context,CODE);
            } catch (IntentSender.SendIntentException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
        requestLocation();
    }


    private void requestLocation(){
        LocationServices.getFusedLocationProviderClient(context).requestLocationUpdates(locationRequest,locationCallback,Looper.getMainLooper());
    }
    private void removeRequestLocation(){
        LocationServices.getFusedLocationProviderClient(context).removeLocationUpdates(locationCallback);
    }

    public void onRequestApproved(){
        locationRequestTask();
    }

    public interface LocationHelperCallback{
        void onPermissionNeeded();
        void onPermissionDenied();

    }
}

package com.senarios.simxx.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.senarios.simxx.Utility;
import com.hdev.common.datamodels.NetworkModel;

import org.greenrobot.eventbus.EventBus;

public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (context!=null){
            if (intent!=null){
                if (intent.getAction()!=null && intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
                    EventBus.getDefault().post(new NetworkModel(Utility.isNetworkAvailable(context)));
                }
                else if (intent.getAction()!=null && intent.getAction().equals("android.net.wifi.WIFI_STATE_CHANGED")){
                    EventBus.getDefault().post(new NetworkModel(Utility.isNetworkAvailable(context)));
                }
            }

        }

    }
}

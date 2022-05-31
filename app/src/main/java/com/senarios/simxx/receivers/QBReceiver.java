package com.senarios.simxx.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.hdev.common.Constants;
import com.hdev.common.datamodels.Users;
import com.senarios.simxx.Utility;
import com.senarios.simxx.services.ChatLoginService;

import java.util.Set;

public class QBReceiver extends BroadcastReceiver implements Constants.SharedPreference, Constants.QB {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle=intent.getExtras();
       Set<String> set=bundle.keySet();
        Log.v("simxset", set.toString());
        Log.v("simxset", set.size()+"");

        SharedPreferences preferences=context.getSharedPreferences(Preference,Preference_Mode);
        Users user=new Gson().fromJson(preferences.getString(USER,""), Users.class);
        if (!bundle.containsKey("default")&& bundle.containsKey("VOIPCall")) {
            if (preferences.getBoolean(Login_Boolean, false) && !preferences.getBoolean(SUB_AWS, false)) {
                Intent login = new Intent(context, ChatLoginService.class);
                login.putExtra(QB_INCOMING_CALL, true);
                login.putExtra(QB_USER_LOGIN, user.getUsername());
                login.putExtra(QB_PASSWORD, QB_DEFAULT_PASSWORD);
                login.putExtra(QB_FULL_NAME, user.getName());
                login.putExtra(QB_ID, Integer.valueOf(user.getQbid()));
                    context.startService(login);


            }
        }
    }
}

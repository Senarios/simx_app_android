package com.senarios.simxx.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.hdev.common.Constants;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;

public class QbSignUpService extends Service implements Constants.QB , Constants.Messages {


    private void Signup(QBUser qbUser) {
        QBUsers.signUp(qbUser).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                Log.wtf("qbiderro", String.valueOf(qbUser.getId()));
                getSharedPreferences(Constants.SharedPreference.Preference,Constants.SharedPreference.Preference_Mode)
                        .edit().putInt(QB_ID,qbUser.getId()).apply();
                stopForeground(true);
                stopSelf();
            }

            @Override
            public void onError(QBResponseException e) {
                Utility.showELog(e);
                Log.wtf("qbiderror",e.toString());
                    stopForeground(true);
                    stopSelf();

            }
        });

    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startMyOwnForeground();
        }
        else{
            startForeground(1,new Notification());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;

    }

    @SuppressLint("NewApi")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String login = intent.getStringExtra(QB_USER_LOGIN);
            String password=intent.getStringExtra(QB_PASSWORD);
            String name=intent.getStringExtra(QB_FULL_NAME);

            if (login!=null &&  password!=null) {
                QBUser qbUser = new QBUser(login, password);
                qbUser.setFullName(name);
                StringifyArrayList<String> tags= new StringifyArrayList<>();
                tags.add("android");
                qbUser.setTags(tags);
                Signup(qbUser);
            }

        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.simx_app_icon_square)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(999,notification);
    }
}

package com.hdev.common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;


/*
* created by hashim
* */

public class NotificationUtils {
    private final String DEFAULT_CHANNEL_ID="01";
    private final int NOTIFICATION_ID = 1;
    private final String CHANNEL_NAME="Simx Notifications";
    private final String CHANNEL_DESC="";
    private NotificationCompat.Builder builder;
    private Context context;

    public NotificationUtils(Context context) {
        this.context=context;
    }


    public NotificationUtils initNotification(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = CHANNEL_NAME;
            String description = CHANNEL_DESC;
            NotificationChannel channel = createNotificationChannel(DEFAULT_CHANNEL_ID, name, description);
            if (CommonUtils.getNotificationManager(context) != null) {
                if (channel != null) {
                    CommonUtils.getNotificationManager(context).createNotificationChannel(channel);
                }
            }
        }
        builder = new NotificationCompat.Builder(context, DEFAULT_CHANNEL_ID)
                .setSmallIcon(R.mipmap.alerter)
                .setChannelId(DEFAULT_CHANNEL_ID)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        return this;
    }

    public NotificationUtils setTitle(String title) {
        builder.setContentTitle(title);
        return this;
    }

    public NotificationUtils setTitle(Boolean isOngoing) {
        builder.setOngoing(isOngoing);
        return this;
    }

    public NotificationUtils setContent(String content) {
        builder.setContentText(content);
        return this;
    }

    public NotificationUtils setPending(Intent intent) {
        PendingIntent pending=PendingIntent.getActivity(context,0,intent,0);
        builder.setContentIntent(pending);
        return this;
    }



    public void buildAndNotify(){
        CommonUtils.getNotificationManager(context).notify(NOTIFICATION_ID,builder.build());
        context=null;
    }

    public NotificationUtils updateProgress(int progress){
        builder.setProgress(100,progress,false);
        return this;
    }


    private NotificationChannel createNotificationChannel(String ID,CharSequence name,String description) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(ID, name, importance);
            channel.setDescription(description);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            return channel;
        }
        return null;
    }


}

package com.senarios.simxx.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.hdev.common.Constants;
import com.senarios.simxx.R;

public abstract class BaseService extends Service {
    private final String CHANNEL_ID="00";
    public  final int NOTIFICATION_ID = 1;
    private NotificationCompat.Builder builder;

    public Notification notificationBuilder(String title, String content, Boolean isProgress,Boolean isOngoing,Boolean isAction){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            NotificationChannel channel = createNotificationChannel(CHANNEL_ID, name, description);
            if (getNotificationManager() != null) {
                if (channel != null) {
                    getNotificationManager().createNotificationChannel(channel);
                }
            }
        }
        builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.simx_app_icon_circle)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        if (isOngoing){
            builder.setOngoing(isOngoing);
        }

        if (isProgress){
            builder.setProgress(100,0,false);

        }
        if (isAction){
            builder.addAction(getAction());
        }
        return builder.build();
    }

    private NotificationCompat.Action getAction() {
        Intent intent=new Intent(this,AmazonS3UploadService.class);
        intent.putExtra(Constants.SharedPreference.ISUPLOADING,true);
        PendingIntent pending=PendingIntent.getService(this,0,intent,0);
       return new NotificationCompat.Action(R.mipmap.simx_app_icon_circle,"Stop Upload",pending);

    }

    public void updateProgress(int progress){
        builder.setProgress(100,progress,false);
        getNotificationManager().notify(NOTIFICATION_ID, builder.build());
    }
    public NotificationChannel createNotificationChannel(String ID,CharSequence name,String description) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(ID, name, importance);
            channel.setDescription(description);
            return channel;
        }
        return null;
    }


    public NotificationManager getNotificationManager(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return this.getApplicationContext().getSystemService(NotificationManager.class);
        }
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

   
}

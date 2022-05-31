package com.senarios.simxx.services;

import static com.senarios.simxx.activities.MainActivity.STATIC_TOKEN;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.hdev.common.CommonUtils;
import com.hdev.common.datamodels.Events;
import com.hdev.common.datamodels.NotificationKeys;
import com.hdev.common.datamodels.NotificationType;
import com.hdev.common.datamodels.Users;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.activities.MainActivity;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class FCMMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        STATIC_TOKEN = s;
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String,String> map=remoteMessage.getData();
        Utility.showLog(map.toString());
        Set<String> keys=map.keySet();

//        keys.stream().forEach(Utility::showLog);
        Utility.showLog("Message Received");

        String message=map.get("message");
        if (map.containsKey("default")) {
            Intent intent = new Intent("custom");
            JSONObject jsonObject = new JSONObject(remoteMessage.getData());
            intent.putExtra("data", jsonObject.toString());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
        else if (keys.contains(NotificationKeys.Type.toString())){
            NotificationType type=NotificationType.valueOf(map.get(NotificationKeys.Type.toString()));
            Users user=new Gson().fromJson(map.get(NotificationKeys.User.toString()),Users.class);
            switch (type){
                case JobRequest:
                    Utility.showDefaultNotification(getApplicationContext(),"SimX",user.getName()+" Applied on Job You posted");
                    break;
                case Appointment:
                    Utility.showDefaultNotification(getApplicationContext(),"SimX",user.getName()+" Requested for An Appointment");
                    EventBus.getDefault().post(Events.APPOINTMENT);
                    break;

                case Follower:
                    Utility.showDefaultNotification(getApplicationContext(),"SimX",user.getName()+" Followed you");
                    EventBus.getDefault().post(Events.FOLLOWER);
                    break;

                case PROFILEPICTURE:
                    Utility.refreshImages(getApplicationContext());
                    break;




            }


         }

        else if ( message!=null && !message.contains("calling") ){
            showNotification(map);
        }




    }

    private void showNotification(Map<String, String> map) {
        try{

        String name=map.get("message").split(":")[0].trim();
        String message =map.get("message").split(":")[1].trim();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("0", "Chat Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);

        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "0");
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
             builder.setSmallIcon(R.mipmap.h2p2);
        }
        else{
            builder.setSmallIcon(R.mipmap.h2p2);
        }

        builder .setContentTitle(name)
                .setContentText(message)
                .setAutoCancel(true);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        if (mNotificationManager != null) {
            mNotificationManager.notify(0, builder.build());
        }
        }
        catch (Exception e){
            Log.v("simxnotiexception", e.getMessage()+"");
        }

    }
}

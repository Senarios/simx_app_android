package com.senarios.simxx.services;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hdev.common.datamodels.Events;
import com.hdev.common.datamodels.Users;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.connections.tcp.QBTcpChatConnectionFabric;
import com.quickblox.chat.connections.tcp.QBTcpConfigurationBuilder;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.messages.services.QBPushManager;
import com.quickblox.messages.services.SubscribeService;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.hdev.common.Constants;
import com.senarios.simxx.R;
import com.senarios.simxx.Utility;
import com.senarios.simxx.activities.CallActivity;
import com.senarios.simxx.fragments.homefragments.ProfileFragment;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

import static com.hdev.common.Constants.DreamFactory.URL;
import static com.hdev.common.Constants.SharedPreference.USER;
import static com.quickblox.videochat.webrtc.QBRTCClient.TAG;

public class ChatLoginService extends Service implements Constants.QB  {

   private QBChatService chatService;
    private QBRTCClient rtcClient;
    private boolean isIncoming=false;
    private QBUser user;
    private Context context=this;
    public PendingIntent pendingIntent;
    int id;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if (intent != null) {
                String login = intent.getStringExtra(QB_USER_LOGIN);
                String password = intent.getStringExtra(QB_PASSWORD);
                String name = intent.getStringExtra(QB_FULL_NAME);
//                int id = Integer.parseInt(Objects.requireNonNull(intent.getStringExtra(QB_ID)));
            if (!TextUtils.isEmpty(intent.getStringExtra(QB_ID)) && TextUtils.isDigitsOnly(intent.getStringExtra(QB_ID))) {
                id = Integer.parseInt(intent.getStringExtra(QB_ID));
            } else {
                id = 0;
            }

                isIncoming = intent.getBooleanExtra(QB_INCOMING_CALL, false);
                if (login != null && password != null) {
                    QBUser qbUser = new QBUser(login, password);
                    qbUser.setFullName(name);
                    qbUser.setId(id);
                    StringifyArrayList<String> tags = new StringifyArrayList<>();
                    tags.add("android");
                    qbUser.setTags(tags);
                    user = qbUser;
                    signInQB(qbUser);

                }


            }


        return START_STICKY;
    }

    @SuppressLint("WrongConstant")
    private void LoginwithChat(QBUser qbUser) {
        getSharedPreferences(Constants.SharedPreference.Preference, MODE_PRIVATE).edit().putString(Constants.SharedPreference.QB_USER, new Gson().toJson(qbUser)).apply();
        QBChatService.ConfigurationBuilder chatServiceConfigurationBuilder = new QBChatService.ConfigurationBuilder();
        chatServiceConfigurationBuilder.setSocketTimeout(360); //Sets chat socket's read timeout in seconds
        chatServiceConfigurationBuilder.setKeepAlive(true); //Sets connection socket's keepAlive option.
        QBChatService.setConfigurationBuilder(chatServiceConfigurationBuilder);
        if (chatService.isLoggedIn()){
            Utility.showLog("If Chat Logged");

            if (isIncoming) {

                Intent i=new Intent(getApplicationContext(), CallActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED +
                                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD +
                                WindowManager.LayoutParams.FLAG_FULLSCREEN +
                                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON +
                                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON).putExtra(QB_INCOMING_CALL, true);

                if(!isRunning(context) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startMyOwnForeground(i);
                }
                else
                {
                    startActivity(i);
                }
            }
            stopForeground(true);
            stopSelf();


        }
        else{
           chatService.login(qbUser, new QBEntityCallback<QBUser>() {
               @Override
               public void onSuccess(QBUser loggeduser, Bundle bundle) {
                   Utility.showLog("Else Chat Logged");

                   if (isIncoming) {


                        Intent i=new Intent(getApplicationContext(), CallActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                               .addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED +
                                       WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD +
                                       WindowManager.LayoutParams.FLAG_FULLSCREEN +
                                       WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON +
                                       WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON).putExtra(QB_INCOMING_CALL, true);

                       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                           startMyOwnForeground(i);
                       }
                       else{
                           startActivity(i);
                       }

                   }
                       stopForeground(true);
                       stopSelf();

               }

               @Override
               public void onError(QBResponseException e) {
                   Utility.showELog(e);
                   stopForeground(true);
                   stopSelf();
               }
           });

        }


    }

    public static boolean isRunning(Context ctx) {
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> tasks = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo task : tasks) {
            if (task.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && ctx.getPackageName().equalsIgnoreCase(task.processName))
                return true;
        }
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        chatService = QBChatService.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //startMyOwnForeground();
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



    private void logout() {
        if (rtcClient != null) {
            rtcClient.destroy();
        }

        if (chatService != null) {
            chatService.logout(new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                    Log.d(TAG, "Logout Successful");
                    chatService.destroy();
                }

                @Override
                public void onError(QBResponseException e) {
                    Log.d(TAG, "Logout Error: " + e.getMessage());
                    chatService.destroy();
                }
            });
        }
        stopSelf();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (!isCallServiceRunning()) {
        }


    }
    private boolean isCallServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        boolean running = false;
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (ChatLoginService.class.getName().equals(service.service.getClassName())) {
                    running = true;
                }
            }
        }
        return running;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(Intent i){
        String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
        String channelName = "My Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder
                .setOngoing(false)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.mipmap.simx_app_icon_square)
                .setContentTitle("Incoming Call")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(Notification.CATEGORY_CALL)
                .setTimeoutAfter(45000)
                .setAutoCancel(false)
                .build();
        //startForeground(0,notification);
        manager.notify(3,notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("loginchatservice", "destroyed");

    }

    private void signInQB(QBUser EqbUser){
        QBUsers.signIn(EqbUser).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                Utility.showLog("User Logged");
                getSharedPreferences(Constants.SharedPreference.Preference, MODE_PRIVATE).edit().putBoolean(Constants.SharedPreference.Login_Boolean,true).apply();
                SubscribeService.subscribeToPushes(getApplicationContext(),true);
                Users user=Utility.getloggedUser(getApplicationContext());
                user.setQbid(qbUser.getId().toString().trim());
                getSharedPreferences(Constants.SharedPreference.Preference, MODE_PRIVATE).edit().putString(USER,new Gson().toJson(user)).apply();
                EventBus.getDefault().post(Events.UPDATE);
                LoginwithChat(EqbUser);
            }

            @Override
            public void onError(QBResponseException e) {
                Utility.showELog(e);
                signUpQB(EqbUser);


            }
        });
    }

    private void signUpQB(QBUser EqbUser) {
        QBUsers.signUp(EqbUser).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                Utility.showLog("User signed up");
                getSharedPreferences(Constants.SharedPreference.Preference,Constants.SharedPreference.Preference_Mode)
                        .edit().putInt(QB_ID,qbUser.getId()).apply();
                signInQB(EqbUser);
            }

            @Override
            public void onError(QBResponseException e) {
                Utility.showELog(e);
                stopForeground(true);
                stopSelf();

            }
        });

    }


}

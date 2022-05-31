package com.senarios.simxx.application;

import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hdev.common.Constants;
import com.quickblox.auth.session.QBSettings;
import com.quickblox.chat.QBChatService;
import com.senarios.simxx.R;
import com.wowza.gocoder.sdk.api.WowzaGoCoder;

import io.reactivex.plugins.RxJavaPlugins;

public class SimX extends MultiDexApplication implements Constants.QB_CREDENTIALS {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        //
        WowzaGoCoder.init(this, getResources().getString(R.string.gocoder_licsensekey));
        //QB Universal settings, init and everything.
        QBSettings.getInstance().init(getApplicationContext(), APPLICATION_ID, AUTH_KEY, AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);
        QBSettings.getInstance().setAutoCreateSession(true);
        QBSettings.getInstance().setEnablePushNotification(true);

        FirebaseMessaging.getInstance().subscribeToTopic("S");

        //QBChat settings
        QBChatService.ConfigurationBuilder chatServiceConfigurationBuilder = new QBChatService.ConfigurationBuilder();
        chatServiceConfigurationBuilder.setSocketTimeout(60); //Sets chat socket's read timeout in seconds
        chatServiceConfigurationBuilder.setKeepAlive(true); //Sets connection socket's keepAlive option.
        chatServiceConfigurationBuilder.setUseTls(true); //Sets the TLS security mode used when making the connection. By default TLS is disabled.
        QBChatService.setConfigurationBuilder(chatServiceConfigurationBuilder);
        QBChatService.setDebugEnabled(true); // enable chat logging
        QBChatService.setDefaultPacketReplyTimeout(10000);

        RxJavaPlugins.setErrorHandler(throwable -> {
        }); // nothing or some logging


    }

}

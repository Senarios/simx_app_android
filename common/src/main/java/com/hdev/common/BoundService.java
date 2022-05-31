package com.hdev.common;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class BoundService extends Service {
    private SBinder binder=new SBinder();


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    public class SBinder extends Binder{

        public SBinder() {
        }

        public BoundService getService(){
            return BoundService.this;
        }
    }
}

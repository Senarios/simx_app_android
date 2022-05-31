package com.hdev.common;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

public class CustomThread extends HandlerThread {

    public CustomThread(String name) {
        super(name);
    }

    @Override
    public void run() {
        super.run();

    }
}

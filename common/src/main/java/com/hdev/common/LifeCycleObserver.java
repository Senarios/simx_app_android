package com.hdev.common;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

public abstract class LifeCycleObserver implements LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    abstract protected void onCreate();

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    abstract protected void onResume();

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    abstract protected void onPause();

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
   abstract protected void onStart();

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
   abstract protected void onStop();

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    abstract protected void onDestroy();


}

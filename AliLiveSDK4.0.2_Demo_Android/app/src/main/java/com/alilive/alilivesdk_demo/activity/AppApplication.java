package com.alilive.alilivesdk_demo.activity;
import android.app.Application;
import android.content.Context;

/**
 * data:2020-09-08
 */
public class AppApplication extends Application {

    public static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
    }
}

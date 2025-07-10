package com.union_test.toutiao;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.LifecycleRegistry;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;


public class DemoApplication extends MultiDexApplication implements Application.ActivityLifecycleCallbacks {

    public static String PROCESS_NAME_XXXX = "process_name_xxxx";
    private static Context context;
    private String tag = "----DemoApplication";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
        DemoApplication.context = this;
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static Context getAppContext() {
        return DemoApplication.context;
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        Log.d(tag, "onActivityCreated " + activity.getLocalClassName());
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        Log.d(tag, "onActivityResumed " + activity.getLocalClassName());
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}

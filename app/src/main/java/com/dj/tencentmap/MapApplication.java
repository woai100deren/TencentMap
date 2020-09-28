package com.dj.tencentmap;

import android.app.Application;

import com.dj.library.LogUtils;

public class MapApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.debug(BuildConfig.DEBUG);
    }
}

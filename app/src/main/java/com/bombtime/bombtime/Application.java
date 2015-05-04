package com.bombtime.bombtime;

import android.util.Log;

import org.bbs.android.commonlib.ExceptionCatcher;

public class Application extends android.app.Application {
    private static final java.lang.String TAG = Application.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "hello world!!!");
        ExceptionCatcher.attachExceptionHandler(this);
    }
}

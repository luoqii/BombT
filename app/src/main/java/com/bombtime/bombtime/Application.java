package com.bombtime.bombtime;

import org.bbs.android.commonlib.ExceptionCatcher;

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();

        ExceptionCatcher.attachExceptionHandler(this);
    }
}

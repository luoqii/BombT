package com.bombtime.bombtime;

import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import org.bbs.android.commonlib.ExceptionCatcher;

public class Application extends android.app.Application {
    private static final java.lang.String TAG = Application.class.getSimpleName();
    private static Application sInstance;

    public DatabaseHelper databaseHelper = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "hello world!!!");
        ExceptionCatcher.attachExceptionHandler(this);
        sInstance = this;
        getHelper();

    }

    public static Application getInstance(){
        return sInstance;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

		/*
		 * You'll need this in your class to release the helper when done.
		 */
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }

    protected DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return databaseHelper;
    }
}

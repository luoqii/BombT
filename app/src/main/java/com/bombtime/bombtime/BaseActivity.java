package com.bombtime.bombtime;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.j256.ormlite.android.apptools.OpenHelperManager;

import org.bbs.android.commonlib.CrazyClicker;
import org.bbs.android.commonlib.activity.LogcatActivity;

public class BaseActivity extends Activity {
    private final String TAG = getClass().getSimpleName();
    private DatabaseHelper databaseHelper = null;
    private CrazyClicker mCrazyClicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCrazyClicker = new CrazyClicker(new CrazyClicker.Callback() {

            @Override
            public void onFireInTheHole() {
                LogcatActivity.start(BaseActivity.this);
            }
        });
    }

    public void onUserInteraction() {
        if (BuildConfig.DEBUG) {
            mCrazyClicker.onClick();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (BuildConfig.DEBUG) {
            menu.add(0, R.id.android_comm_lib_menu_logcat, 0, "Logcat");
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.android_comm_lib_menu_logcat:
                LogcatActivity.start(this);
                break;
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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

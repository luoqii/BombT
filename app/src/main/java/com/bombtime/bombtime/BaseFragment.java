package com.bombtime.bombtime;

import android.app.Fragment;

import com.j256.ormlite.android.apptools.OpenHelperManager;

public class BaseFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();
    private DatabaseHelper databaseHelper = null;

    @Override
	public void onDestroy() {
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
            databaseHelper = OpenHelperManager.getHelper(getActivity(), DatabaseHelper.class);
        }
        return databaseHelper;
    }
}

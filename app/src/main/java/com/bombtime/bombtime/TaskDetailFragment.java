package com.bombtime.bombtime;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


/**
 * A placeholder fragment containing a simple view.
 */
public class TaskDetailFragment extends BaseFragment implements  View.OnClickListener {

    /**
     * type {@link int}
     */
    public static final String ARG_TASK_ID = TaskDetailActivity.EXTRA_TASK_ID;
    private static final boolean DEBUG_UI = BuildConfig.DEBUG && true;
    private static final java.lang.String TAG = TaskDetailFragment.class.getSimpleName();

    private static final int ACTION_START = 1;
    private static final int ACTION_PAUSE = 2;
    private  int mButtonAction = ACTION_START;

    private int mTaskId;

    @InjectView(R.id.button_start_or_pause)
	/*private*/ TextView mStartOrPauseV;
    @InjectView(R.id.button_delete)
    /*private*/ View mDeleteV;
    @InjectView(R.id.button_done)
    /*private*/ View mDoneV;
    @InjectView(R.id.reminder)
	/*private*/ ReminderTimeView mReminderV;

    private TaskData mTask;

    public static TaskDetailFragment newInstance(int id) {
        TaskDetailFragment fragment = new TaskDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TASK_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    public TaskDetailFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        mTaskId = getArguments().getInt(ARG_TASK_ID);

        try {
            mTask = getHelper().getTaskDataDao().queryForId(mTaskId);
            Log.d(TAG, "task: " + mTask);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @OnClick({R.id.button_delete, R.id.button_done, R.id.button_start_or_pause,R.id.tob_bar})
    @Override
    public void onClick(View view) {
        try {
            Dao<TaskData, Integer> dao = getHelper().getTaskDataDao();
            switch (view.getId()) {
                case R.id.button_delete:
//                    dao.deleteById(mTaskId);

                    DoUndoWindow undo = new DoUndoWindow(Application.getInstance(), new DoUndoWindow.DeleteTasksAction(mTask));
                    undo.show();

                    getActivity().finish();
                    break;
                case R.id.button_done:
                    mTask.setState(TaskData.STATE_DONE);
                    dao.update(mTask);
                    getActivity().finish();
                    break;
                case R.id.button_start_or_pause:
                    if (mButtonAction == ACTION_PAUSE) {
                        mTask.setState(TaskData.STATE_PAUSE);
                        mReminderV.stop();
                    } else {
                        mTask.setState(TaskData.STATE_START);
                        mReminderV.start(mTask.getEndTime() - System.currentTimeMillis());
                    }
                    mButtonAction = mButtonAction == ACTION_PAUSE ? ACTION_START : ACTION_PAUSE;
                    updateStartButtonText(mButtonAction);
                    dao.update(mTask);
                    updateUi();
                    break;
                case R.id.tob_bar:
                    doEdit();
                    break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_task_detail_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (mTask.getState() == TaskData.STATE_DONE){
            menu.findItem(R.id.action_edit).setEnabled(false);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                doEdit();
                getActivity().finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        mStartOrPauseV = (TextView) view.findViewById(R.id.button_start_or_pause);
//        mDeleteV = view.findViewById(R.id.button_delete);
//        mDoneV = view.findViewById(R.id.button_done);
//        mReminderV = (ReminderTimeView)view.findViewById(R.id.reminder);

//        mStartOrPauseV.setOnClickListener(mListerner);
//        mDeleteV.setOnClickListener(mListerner);
//        mDoneV.setOnClickListener(mListerner);
//        getView().findViewById(R.id.tob_bar).setOnClickListener(mListerner);

        ButterKnife.inject(this,view);

        updateUi();

        if (DEBUG_UI){
            TextView debugV = (TextView) view.findViewById(R.id.debug_info);
            debugV.setVisibility(View.VISIBLE);
            String debugText = mTask.toDebugStr() + " startTime: " + getReminderTime(mTask.getStartTime());
            debugV.setText(debugText);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    private void doEdit() {
        Intent add = new Intent(this.getActivity(), AddTaskActivity.class);
        add.putExtra(TaskDetailActivity.EXTRA_TASK_ID, mTaskId);
        startActivity(add);
    }

    private void updateUi() {

        View contentV = getView();

        int state = mTask.getState();
        if (mTask.getState() == TaskData.STATE_START) {
            mButtonAction = ACTION_PAUSE;
            mReminderV.start(mTask.getEndTime() - System.currentTimeMillis());
        } else if (state == TaskData.STATE_PAUSE){
            mReminderV.stop();
        } else if (state == TaskData.STATE_NEW_CREATE){
            mReminderV.stop();
        } else if (state == TaskData.STATE_DONE){
            mReminderV.setText("Done");
            mStartOrPauseV.setEnabled(false);
            mDoneV.setEnabled(false);
        }

        updateStartButtonText(mButtonAction);

        ((TextView) contentV.findViewById(R.id.name)).setText(mTask.getName());
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(mTask.getEndTime());
        String time = AddTaskFragment.getDateStr(c) + " " + AddTaskFragment.getTimeStr(c);
        ((TextView) contentV.findViewById(R.id.end_time)).setText(time);
    }

    private void updateStartButtonText(int action) {
        if (!mStartOrPauseV.isEnabled()) return;
        mStartOrPauseV.setText(action == ACTION_PAUSE ? "Pause" : "Start");
    }

    public static class ReminderTimeView extends TextView  {
        private static final int MSG_UPDATE = 1;
        public static final int DELAY_MILLIS = 500;

        private final Calendar mCalendar;
        private long mEndTime;
        private long mStartTime;
		private SimpleDateFormat mFormater;

		private Handler mUiHandler;

        public ReminderTimeView(Context context, AttributeSet attrs) {
            super(context, attrs);

            mCalendar = Calendar.getInstance();
            mCalendar.setTimeZone(new SimpleTimeZone(0, "reminderview"));
            mFormater = new SimpleDateFormat("hh:mm:ss");
//            mFormater.setTimeZone(new SimpleTimeZone(0, "reminderview"));

            mUiHandler = new Handler (){

				@Override
				public void handleMessage(Message msg) {
					update();
					super.handleMessage(msg);
				}
            	
            };
        }

        public void start(long time){
            mEndTime = time + System.currentTimeMillis();
            mStartTime = System.currentTimeMillis();
            
            mUiHandler.sendEmptyMessageDelayed(MSG_UPDATE, DELAY_MILLIS);
        }

        public void stop(){

            mUiHandler.removeMessages(MSG_UPDATE);
            setText("paused");
        }
        
        public void onDetachedFromWindow(){
        	super.onDetachedFromWindow();

            mUiHandler.removeMessages(MSG_UPDATE);
        }

        private void update() {
        	long currentTime = System.currentTimeMillis();
        	long reminder = mEndTime - currentTime;
        	if (reminder >= 0){
                mCalendar.setTimeInMillis(reminder);
                reminder = mCalendar.getTimeInMillis();
                Date d = new Date(reminder);
                d = new Date(0);
        		String text = mFormater.format(d);
//                text = getReminderTime(mCalendar);
                text = getReminderTime(reminder);
        		setText(text);

        		mUiHandler.sendEmptyMessageDelayed(MSG_UPDATE, DELAY_MILLIS);
        	} else {
            	mUiHandler.removeMessages(MSG_UPDATE);

                Log.i(TAG, "reminder timer is over.");
        	}
        }

    }
    public static String getReminderTime(long  millSec) {
        final long milliSecPerHour = (1000 * 60 * 60);
        final long milliSecPerMintue = (1000 * 60);
        final long milliSecPerSecond = (1000);
        String str = "";
        long field = millSec / milliSecPerHour;
        if (field < 10) {
            str += "0";
        }
        str += field + ":";
        field = millSec % milliSecPerHour / milliSecPerMintue ;
        if (field < 10) {
            str += "0";
        }
        str += field + ":";
        field = millSec % milliSecPerHour % milliSecPerMintue / milliSecPerSecond ;
        if (field < 10) {
            str += "0";
        }
        str += field + "";

        return str;
    }

    public static  String getReminderTime(Calendar  c) {
        String str = "";
        int f = c.get(Calendar.YEAR);
        if (f != 0){
            if (f < 10){
                str += "";
            }

            str += f + "Y:";
        }
        c.get(Calendar.DAY_OF_YEAR);
        if (f != 0){
            if (f < 10){
                str += "";
            }

            str += f + "D:";
        }
        c.get(Calendar.HOUR_OF_DAY);
        if (f != 0){
            if (f < 10){
                str += "0";
            }

            str += f + ":";
        }
        c.get(Calendar.HOUR_OF_DAY);
        if (f != 0){
            if (f < 10){
                str += "0";
            }

            str += f + ":";
        }
        c.get(Calendar.MINUTE);
        if (f != 0){
            if (f < 10){
                str += "0";
            }

            str += f + ":";
        }
        c.get(Calendar.SECOND);
        if (f != 0){
            if (f < 10){
                str += "0";
            }

            str += f + ":";
        }

        return str;
    }
}

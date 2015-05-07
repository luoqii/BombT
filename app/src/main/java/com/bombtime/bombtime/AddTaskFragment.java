package com.bombtime.bombtime;


import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CancellationException;

import com.j256.ormlite.dao.Dao;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddTaskFragment extends BaseFragment implements View.OnClickListener {
    private static final java.lang.String TAG = AddTaskFragment.class.getSimpleName();
    private static final boolean DEBUG_UI = BuildConfig.DEBUG && true;

    @InjectView(R.id.name)
    /*private*/ EditText mNameV;
    @InjectView(R.id.date)
    /*private*/ TextView mDateV;
    @InjectView(R.id.time)
    /*private*/ TextView mTimeV;
    @InjectView(R.id.start_now)
    /*private*/ ToggleButton mToggleV;
//    @InjectView(R.id.button_ok)
//    Button mOkV;
//    @InjectView(R.id.button_cance)
//    Button mCancleV;

    private Calendar mTaskC;
    private int mTaskId;
    private TaskData mTask;

    public static AddTaskFragment newInstance(int id) {
        AddTaskFragment fragment = new AddTaskFragment();
        Bundle args = new Bundle();
        args.putInt(TaskDetailActivity.EXTRA_TASK_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    public AddTaskFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        mTaskId = getArguments().getInt(TaskDetailActivity.EXTRA_TASK_ID);

        try {
            mTask = getHelper().getTaskDataDao().queryForId(mTaskId);
            Log.d(TAG, "task: " + mTask);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateWith(TaskData task) {
        if (null == task) {
            return;
        }
        mNameV.setText(task.getName());
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(task.getEndTime());
        updateDate(c);
        updateTime(c);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_task, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        mNameV = ((EditText) view.findViewById(R.id.name));
//        mDateV = (TextView) view.findViewById(R.id.date);
//        mTimeV = (TextView) view.findViewById(R.id.time);
//        mToggleV = (ToggleButton) view.findViewById(R.id.start_now);

//        mDateV.setOnClickListener(this);
//        mTimeV.setOnClickListener(this);

        ButterKnife.inject(this, view);
        if (DEBUG_UI){
            mNameV.setFilters(new InputFilter[]{new InputFilter.LengthFilter(40)});
        }

        mTaskC = Calendar.getInstance();

        Calendar rightNow = Calendar.getInstance();
        rightNow.add(Calendar.HOUR, 2);
        updateDate(rightNow.get(Calendar.YEAR), rightNow.get(Calendar.MONTH), rightNow.get(Calendar.DAY_OF_MONTH));
        updateTime(rightNow.get(Calendar.HOUR_OF_DAY), rightNow.get(Calendar.MINUTE), true);
        mTaskC = rightNow;

        updateWith(mTask);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_add_task_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                checkAndSave();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkAndSave() {
        if (dataIsReady()) {
            TaskData task = null;
            if (mTask == null) {
                task = new TaskData();
            } else {
                task = mTask;
            }
            task.setName(mNameV.getText().toString());
            task.setEndTime(mTaskC.getTimeInMillis());
            int type = TaskData.TYPE_BOMB;
            Calendar rightnow = Calendar.getInstance();
            rightnow.add(Calendar.HOUR, 5);
            if (mTaskC.after(rightnow)) {
                type = TaskData.TYPE_SUPER_BOMB;
            }
            rightnow = Calendar.getInstance();
            rightnow.add(Calendar.HOUR, 1);
            if (mTaskC.after(rightnow)) {
                type = TaskData.TYPE_TIMED_BOMB;
            }
            task.setType(type);

            int state = TaskData.STATE_NEW_CREATE;
            if (mToggleV.isChecked()) {
                state = TaskData.STATE_START;
                task.setStartTime(System.currentTimeMillis());
            }

            // start it now.
            state = TaskData.STATE_START;
            task.setStartTime(System.currentTimeMillis());

            task.setState(state);

            Dao<TaskData, Integer> dao;
            try {
                dao = getHelper().getTaskDataDao();
                dao.createOrUpdate(task);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            getActivity().finish();
        }
    }


    private boolean dataIsReady() {
        boolean ready = !TextUtils.isEmpty(mNameV.getText())
                && !TextUtils.isEmpty(mTimeV.getText())
                && !TextUtils.isEmpty(mDateV.getText());
        if (TextUtils.isEmpty(mNameV.getText())) {
            mNameV.setError("not null");
        }
        return ready;
    }


    @OnClick({R.id.time, R.id.date, R.id.button_ok, R.id.button_cance})
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.time:
                new TimePickerFragment().show(getFragmentManager(), "timepicker");
                break;
            case R.id.date:
                new DatePickerFragment().show(getFragmentManager(), "datepicker");
                break;
            case R.id.button_ok:
                checkAndSave();
                break;
            case R.id.button_cance:
                getActivity().finish();
                break;
        }
    }


    private void updateTime(Calendar c) {
        updateTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
    }

    private void updateTime(int hourOfDay, int minute, boolean is2fHour) {
        String time = getTimeStr(hourOfDay, minute);
        mTimeV.setText(time);
        if (DEBUG_UI){
            String secStr = String.format("%tS", new Date());
            String text = mNameV.getText().toString();
            final char C = 'T';
            text = text.replaceAll(C + "[0-9:/]*" + C, "");
            text += " " + C + time +
                    ":" + secStr +
                    C;
            mNameV.setText(text);
        }

        mTaskC.set(Calendar.HOUR_OF_DAY, hourOfDay);
        mTaskC.set(Calendar.MINUTE, minute);
    }


    public static String getTimeStr(Calendar c) {
        return getTimeStr(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
    }

    public static String getTimeStr(int hourOfDay, int minute) {
        String time = "new time";
        time = ((hourOfDay <= 9 ? "0" : "") + hourOfDay) +  ":" + ((minute <= 9 ? "0" : "") + minute);
        return time;
    }

    private void updateDate(Calendar c) {
        updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
    }

    private void updateDate(int year, int month, int day) {
        String date = getDateStr(year, month, day);
        mDateV.setText(date);

        if (DEBUG_UI){
            String text = mNameV.getText().toString();
            final char C = 'D';
            text = text.replaceAll(C + "[0-9:/]*" + C, "");
            text += " " + C + date + C;
            mNameV.setText(text);
        }

        mTaskC.set(Calendar.YEAR, year);
        mTaskC.set(Calendar.MONTH, month);
        mTaskC.set(Calendar.DAY_OF_MONTH, day);
    }

    public static String getDateStr(Calendar c) {
        return getDateStr(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
    }

    public static String getDateStr(int year, int month, int day) {
        String date = "new date";
        date = year + "/" + month + "/" + day;
        return date;
    }

    class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            updateTime(hourOfDay, minute, view.is24HourView());
        }
    }

    class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            updateDate(year, month, day);
        }
    }
}

package com.bombtime.bombtime;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.sql.SQLException;


/**
 * A placeholder fragment containing a simple view.
 */
public class TaskDetailFragment extends BaseFragment {

    /**
     * type {@link int}
     */
    public static final String ARG_TASK_ID = "ARG_TASK_ID";

    private int mTaskId;
    private View.OnClickListener mListerner;

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

        mTaskId = getArguments().getInt(ARG_TASK_ID);
        mListerner = new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.button_delete:
                        try {
                            getHelper().getTaskDataDao().deleteById(mTaskId);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        getActivity().finish();
                        break;
                }
            }
        };
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        updateUi();
    }

    private void updateUi() {
        try {
            TaskData task = getHelper().getTaskDataDao().queryForId(mTaskId);
            View contentV = getView();
            contentV.findViewById(R.id.button_pause).setOnClickListener(mListerner);
            contentV.findViewById(R.id.button_delete).setOnClickListener(mListerner);
            contentV.findViewById(R.id.button_done).setOnClickListener(mListerner);

            ((TextView)contentV.findViewById(R.id.name)).setText(task.name);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

package com.bombtime.bombtime;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import org.bbs.android.commonlib.activity.LogcatActivity;


/**
 * A simple {@link Fragment} subclass.
 */
public class TaskCollectionFragment extends BaseFragment {
    private static final String TAG = TaskCollectionFragment.class.getSimpleName()
            ;
    private static final boolean DEBUG_UI = BuildConfig.DEBUG && true;
    public static final int DELAY_MILLIS = 1000;
    private final Handler mHandler;

    private  RecyclerView mTaskV;
    private  TasksAdapter mAdapter;

    static private int[] sColors = null;
    private DataModel mDataMode;

    public TaskCollectionFragment() {
        // Required empty public constructor
        mDataMode = new DataModel();

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                updateData();

            }
        };
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mHandler.removeMessages(0);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        sColors = new int[]{
                getResources().getColor(R.color.android_comm_lib_silver),
                getResources().getColor(R.color.android_comm_lib_gray)
        };
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_task_collection, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTaskV = (RecyclerView)view.findViewById(R.id.task_collection);
        mTaskV.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTaskV.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateUi();
    }

    @Override
    public void onPause() {
        super.onPause();

        mHandler.removeMessages(0);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.menu_add_task_fragment, menu);
    }

    private void updateUi() {
        mAdapter = new TasksAdapter();
        mTaskV.setAdapter(mAdapter);

        updateData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_done:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    void updateData() {
        if (null == mAdapter) {
            return;
        }

        Log.d(TAG, "updateData");
        mAdapter.updateData(mDataMode.getData());
        mHandler.removeMessages(0);

        mHandler.sendEmptyMessageDelayed(0, DELAY_MILLIS);
    }

    class DataModel {
        private final int MSG_UPDATE = 0;
        ArrayList<TaskData> mData;

        DataModel(){
            mData = new ArrayList<TaskData>();
        }

        public ArrayList<TaskData> getData(){
           ArrayList<TaskData> tasks = new ArrayList<>();
            try {
                Dao<TaskData, Integer> dao = getHelper().getTaskDataDao();

                // all start task
                dao = getHelper().getTaskDataDao();
                QueryBuilder<TaskData, Integer> builder = dao.queryBuilder();
                builder.where().eq(TaskData.FIELD_STATE, TaskData.STATE_START);
                builder.orderBy(TaskData.FIELD_END_TIME, true);
                List<TaskData> tmp = dao.query(builder.prepare());
                for (TaskData t : tmp){
                    tasks.add(t);
                }

                // other task
                builder = dao.queryBuilder();
                builder.where().ne(TaskData.FIELD_STATE, TaskData.STATE_START);
                builder.orderBy(TaskData.FIELD_END_TIME, true);
                tmp = dao.query(builder.prepare());
                for (TaskData t : tmp){
                    tasks.add(t);
                }

                updateData(tasks);
            } catch (SQLException e) {
                e.printStackTrace();
            }

           return tasks;
        }

        private void updateData(ArrayList<TaskData> tasks) {
            long time = System.currentTimeMillis();
            for (TaskData t: tasks){
                t.setCurrentTime(time);

                if (t.getState() == TaskData.STATE_START && time > t.getEndTime()){
                    t.setState(TaskData.STATE_FAIL);
                    try {
                        getHelper().getTaskDataDao().update(t);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    class TasksAdapter extends RecyclerView.Adapter {

        private ArrayList<TaskData> mTasks = new ArrayList<>();

        public TasksAdapter(){
        }

        public void updateData(ArrayList<TaskData> tasks){
            mTasks = tasks;
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = View.inflate(parent.getContext(), R.layout.task_item_container, null);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TaskData t = (TaskData) view.getTag();

                    Intent detail = new Intent(getActivity(), TaskDetailActivity.class);
                    detail.putExtra(TaskDetailActivity.EXTRA_TASK_ID, t.getId());
                    startActivity(detail);
                }
            });

            RecyclerView.ViewHolder vh = new VH(view);

            return vh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            TaskData t = mTasks.get(position);
            VH vh = (VH) viewHolder;
            vh.itemView.setTag(t);
            vh.itemView.setBackgroundColor(sColors[position % sColors.length]);
            vh.nameV.setText(t.getName());

            if (DEBUG_UI){
                vh.debugV.setVisibility(View.VISIBLE);
                vh.debugV.setText(t.toDebugStr());
            }

        }

        @Override
        public int getItemCount() {
            return mTasks.size();
        }

        class VH extends RecyclerView.ViewHolder {

            final TextView nameV;
            private final TextView debugV;

            public VH(View itemView) {
                super(itemView);

                nameV = (TextView)itemView.findViewById(R.id.name);
                debugV = (TextView)itemView.findViewById(R.id.debug_info);
            }
        }
    }
}

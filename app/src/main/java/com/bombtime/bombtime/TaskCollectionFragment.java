package com.bombtime.bombtime;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;


/**
 * A simple {@link Fragment} subclass.
 */
public class TaskCollectionFragment extends BaseFragment {
    private static final boolean DEBUG_UI = BuildConfig.DEBUG && true;

    private  RecyclerView mTaskV;
    private  RecyclerView.Adapter mAdapter;

    static private int[] sColors = null;

    public TaskCollectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.menu_add_task_fragment, menu);
    }

    private  void updateUi() {
        try {
            Dao<TaskData, Integer> dao = getHelper().getTaskDataDao();
            List<TaskData> tasks = dao.queryForAll();
            tasks = getData();

            mAdapter = new TasksAdapter(tasks);
            mTaskV.setAdapter(mAdapter);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    List<TaskData> getData(){
        List<TaskData> tasks = new ArrayList<>();
        try {
            Dao<TaskData, Integer> dao = getHelper().getTaskDataDao();
            QueryBuilder<TaskData, Integer> builder = dao.queryBuilder();
            builder.where().lt(TaskData.FIELD_STATE, TaskData.STATE_DONE);
            builder.orderBy(TaskData.FIELD_END_TIME, true);
            tasks.addAll(dao.query(builder.prepare()));
            builder = dao.queryBuilder();
            builder.where().ge(TaskData.FIELD_STATE, TaskData.STATE_DONE);
            builder.orderBy(TaskData.FIELD_END_TIME, true);
            tasks.addAll(dao.query(builder.prepare()));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_done:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    class TasksAdapter extends RecyclerView.Adapter {

        private final  List<TaskData> mTasks;

        public TasksAdapter(List<TaskData> tasks){
            mTasks = tasks;
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

package com.bombtime.bombtime;


import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.dao.Dao;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class TaskCollectionFragment extends BaseFragment {

    private  RecyclerView mTaskV;
    private  RecyclerView.Adapter mAdapter;

    public TaskCollectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
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

            mAdapter = new TasksAdapter(tasks);
            mTaskV.setAdapter(mAdapter);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = View.inflate(viewGroup.getContext(), R.layout.task_item_container, null);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TaskData t = (TaskData) view.getTag();

                    Intent detail = new Intent(getActivity(), TaskDetailActivity.class);
                    detail.putExtra(TaskDetailActivity.EXTRA_TASK_ID, t.id);
                    startActivity(detail);
                }
            });

            RecyclerView.ViewHolder vh = new VH(view);

            return vh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
            TaskData t = mTasks.get(i);
            VH vh = (VH) viewHolder;
            vh.itemView.setTag(t);
            vh.nameV.setText(t.name);


        }

        @Override
        public int getItemCount() {
            return mTasks.size();
        }

        class VH extends RecyclerView.ViewHolder {

            final TextView nameV;

            public VH(View itemView) {
                super(itemView);

                nameV = (TextView)itemView.findViewById(R.id.name);
            }
        }
    }
}
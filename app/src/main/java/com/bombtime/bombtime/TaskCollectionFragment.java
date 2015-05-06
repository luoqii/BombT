package com.bombtime.bombtime;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;


/**
 * A simple {@link Fragment} subclass.
 */
public class TaskCollectionFragment extends BaseFragment {
    private static final String TAG = TaskCollectionFragment.class.getSimpleName()
            ;
    private static final boolean DEBUG_UI = BuildConfig.DEBUG && true;
    public static final int DELAY_MILLIS = 1000;
    private final Handler mHandler;
    private List<Integer> mSelectItems;

    private  RecyclerView mTaskV;
    private RecyclerTasksAdapter mRecyclerAdapter;

    static private int[] sColors = null;
    private DataModel mDataMode;

    private ListView mListV;
    private ListTasksAdapter mListAdapter;

    private ActionMode mActionMode;
    private AbsListView.MultiChoiceModeListener mActionModeListener;

    public TaskCollectionFragment() {
        // Required empty public constructor
        mDataMode = new DataModel();

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                updateData();

            }
        };
        mActionModeListener = new AbsListView.MultiChoiceModeListener(){

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mSelectItems = new ArrayList<>();
                new MenuInflater(getActivity()).inflate(R.menu.taks_collection_cab, menu);
                unScheduleUpdate();
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                TaskData t = mListAdapter.getItem(mListV.getSelectedItemPosition());
                if (t.getState() == TaskData.STATE_FAIL) {
                    mode.getMenu().findItem(R.id.action_done).setVisible(false);
                    return true;
                }
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_done:
                        prepareMarkAsDone(getTask(mSelectItems));
                        break;
                    case R.id.action_delete:
                        prepareMaskAdDelete(getTask(mSelectItems));
                        break;
                }

                updateData();
                mode.finish();
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                scheduleUpdate();
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                TaskData t = mListAdapter.getItem(position);
                if (checked){

                    if (!mSelectItems.contains(position)) {
                        mSelectItems.add(position);
                    }
                } else {

                    if (mSelectItems.contains(position)) {
                        mSelectItems.remove(new Integer(position));
                    }
                }

                boolean hasFail = false;
                for (Integer i : mSelectItems){
                    if (mListAdapter.getItem(i).getState() == TaskData.STATE_FAIL){
                        hasFail = true;
                        break;
                    }
                }
                mode.getMenu().findItem(R.id.action_done).setVisible(!hasFail);

                String title = mSelectItems.size() == 1 ? mListAdapter.getItem(mSelectItems.get(0)).getName() : mSelectItems.size() + " tasks";
                mode.setTitle(title);
//                mode.setSubtitle("subtitle");
            }
        };
    }

    List<TaskData> getTask(List<Integer> ids){
        List<TaskData> tasks = new ArrayList<>();

        for (Integer i : ids){
            tasks.add(mListAdapter.getItem(i));
        }
        return tasks;
    }

    private void prepareMarkAsDone(List<TaskData> tasks) {
        DoUndoWindow undo = new DoUndoWindow(getActivity(), new DoneTasksAction(tasks));

        undo.showAsDropDown(mListV);
    }

    private void prepareMaskAdDelete(List<TaskData> tasks) {
        DoUndoWindow undo = new DoUndoWindow(getActivity(), new DeleteTasksAction(tasks));

        undo.showAsDropDown(mListV);
    }

    private void unScheduleUpdate() {
        mHandler.removeMessages(0);
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

        unScheduleUpdate();
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
        View view = inflater.inflate(R.layout.fragment_task_collection, container, false);

        View contentV = initContentView();

        ((ViewGroup)view).addView(contentV, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        return view;
    }

    private View initContentView() {
        mListV = new ListView(getActivity());
        mListV.setId(R.id.task_list);
        mListV.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        mListV.setMultiChoiceModeListener(mActionModeListener);
        mListV.setBackgroundColor(Color.alpha(0));
        mListV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TaskCollectionFragment.this.onItemClick(parent, view, position, id);
            }
        });
        return mListV;
    }

    @OnItemClick(R.id.task_list)
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        TaskData t = (TaskData) view.getTag(R.id.TAG_DATA);

        viewDetail(view.getContext(), t);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateUi();
    }

    @Override
    public void onPause() {
        super.onPause();

        unScheduleUpdate();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.menu_add_task_fragment, menu);
    }

    private void updateUi() {

//        mRecyclerAdapter = new RecyclerTasksAdapter();
//        mTaskV.setAdapter(mRecyclerAdapter);

        mListAdapter = new ListTasksAdapter(getActivity());
        mListV.setAdapter(mListAdapter);

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
        Log.d(TAG, "updateData");

//        mRecyclerAdapter.updateData(mDataMode.getData());

        mListAdapter.updateData(mDataMode.getData());

        unScheduleUpdate();
        scheduleUpdate();
    }

    private void scheduleUpdate() {
        mHandler.sendEmptyMessageDelayed(0, DELAY_MILLIS);
    }

    public static class DataModel {
        private final int MSG_UPDATE = 0;

        ArrayList<TaskData> mData;

        DataModel(){
            mData = new ArrayList<TaskData>();
        }

        public ArrayList<TaskData> getData(){
           ArrayList<TaskData> tasks = new ArrayList<>();
            try {
                Dao<TaskData, Integer> dao = Application.getInstance().getHelper().getTaskDataDao();
                QueryBuilder<TaskData, Integer> builder = dao.queryBuilder();
                builder.where()
                        .eq(TaskData.FIELD_STATE, TaskData.STATE_START)
                        .and()
                        .ne(TaskData.FIELD_MARK_AS_DELETE, true);
                builder.orderBy(TaskData.FIELD_END_TIME, true);
                List<TaskData> tmp = dao.query(builder.prepare());
                for (TaskData t : tmp){
                    tasks.add(t);
                }

                // other task
                builder = dao.queryBuilder();
                builder.where()
                        .ne(TaskData.FIELD_STATE, TaskData.STATE_START)
                        .and()
                        .ne(TaskData.FIELD_MARK_AS_DELETE, true);
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
                        Application.getInstance().getHelper().getTaskDataDao().update(t);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    class ListTasksAdapter extends ArrayAdapter<TaskData> {

        public ListTasksAdapter(Context context) {
            super(context, 0);
        }

        public void updateData(ArrayList<TaskData> tasks){
            clear();
            addAll(tasks);
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            VH viewHolder = null;
            if (convertView != null){
                viewHolder = (VH) convertView.getTag();
            } else {
                convertView = View.inflate(getContext(), R.layout.task_item_container, null);
                viewHolder = new VH(convertView);
                convertView.setTag(viewHolder);
            }

            ((VH) viewHolder).bindData(position, getItem(position));

            return convertView;
        }
    }

    class RecyclerTasksAdapter extends RecyclerView.Adapter {

        private ArrayList<TaskData> mTasks = new ArrayList<>();

        public RecyclerTasksAdapter(){
        }

        public void updateData(ArrayList<TaskData> tasks){
            mTasks = tasks;
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = View.inflate(parent.getContext(), R.layout.task_item_container, null);

            RecyclerView.ViewHolder vh = new VH(view);

            return vh;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            TaskData t = mTasks.get(position);
            ((VH) viewHolder).bindData(position, t);
        }

        @Override
        public int getItemCount() {
            return mTasks.size();
        }
    }

    class VH extends RecyclerView.ViewHolder {
        @InjectView(R.id.name)
        protected TextView nameV;
        @InjectView(R.id.debug_info)
        protected TextView debugV;

        public VH(View itemView) {
            super(itemView);

//                nameV = (TextView)itemView.findViewById(R.id.name);
//                debugV = (TextView)itemView.findViewById(R.id.debug_info);
            ButterKnife.inject(this, itemView);

        }

        public void bindData(int position, TaskData t) {
            int color = sColors[position % sColors.length];
            itemView.setTag(R.id.TAG_DATA, t);
            itemView.setBackgroundColor(color);
            nameV.setText(t.getName());


//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    TaskData t = (TaskData) view.getTag(R.id.TAG_DATA);
//
//                    viewDetail(view.getContext(), t);
//                }
//            });
//            itemView.setOnLongClickListener(new View.OnLongClickListener(){
//                @Override
//                public boolean onLongClick(View v) {
//                    getActivity().startActionMode(mActionModeListener);
//                    return false;
//                }
//            });

            if (DEBUG_UI){
                debugV.setVisibility(View.VISIBLE);
                debugV.setText(t.toDebugStr());
            }
        }

    }

    private void viewDetail(Context context, TaskData t) {
        Intent detail = new Intent(context, TaskDetailActivity.class);
        detail.putExtra(TaskDetailActivity.EXTRA_TASK_ID, t.getId());
        context.startActivity(detail);
    }

    public static class DeleteTasksAction implements  DoUndoWindow.IDoUnDo {

        private final List<TaskData> mTask;

        DeleteTasksAction(List<TaskData> task){
            mTask = task;
        }
        DeleteTasksAction(TaskData task){
            mTask = new ArrayList<>();
            mTask.add(task);
        }

        @Override
        public void doIt() {
            try {
                Dao<TaskData, Integer> dao = Application.getInstance().getHelper().getTaskDataDao();

                for (TaskData t: mTask) {
                    t.setMarkAsDelete(true);
                    dao.update(t);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void undoIt() {
            try {
                Dao<TaskData, Integer> dao = Application.getInstance().getHelper().getTaskDataDao();

                for (TaskData t: mTask) {
                    t.setMarkAsDelete(false);
                    dao.update(t);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public static class DoneTasksAction implements  DoUndoWindow.IDoUnDo {

        private final List<TaskData> mTask;

        DoneTasksAction(List<TaskData> task){
            mTask = task;
        }
        DoneTasksAction(TaskData task){
            mTask = new ArrayList<>();
            mTask.add(task);
        }

        @Override
        public void doIt() {
            try {
                Dao<TaskData, Integer> dao = Application.getInstance().getHelper().getTaskDataDao();

                for (TaskData t: mTask) {
                    t.setPendingState(t.getState());
                    t.setState(TaskData.STATE_DONE);
                    dao.update(t);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void undoIt() {
            try {
                Dao<TaskData, Integer> dao = Application.getInstance().getHelper().getTaskDataDao();

                for (TaskData t: mTask) {
                    t.setState(t.getPendingState());
                    dao.update(t);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

package com.bombtime.bombtime;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;

import org.bbs.android.commonlib.AlwaysOnTopWindow;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DoUndoWindow extends AlwaysOnTopWindow implements View.OnClickListener {
    private static final String TAG = DoUndoWindow.class.getSimpleName();
    public static final int DELAY_MILLIS = 3 * 1000;

    private final TextView mTextV;
    private IDoUnDo mAction;
    private Runnable mDismissAction;

    public DoUndoWindow(Context context,IDoUnDo action) {
        super(context);
        mAction = action;
        mDismissAction = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "dismiss myself.");
                DoUndoWindow.this.dismiss();
            }
        };

        setContentView(View.inflate(context, R.layout.window_doundo, null));

        mTextV = (TextView)getContentView().findViewById(R.id.text);
        getContentView().findViewById(R.id.button_undo).setOnClickListener(this);
    }

    @Override
    protected WindowManager.LayoutParams onCreateLayoutParams() {
        WindowManager.LayoutParams p =  super.onCreateLayoutParams();

        p.x = 10;
        p.y = 10;
        p.width = 300;
        p.height = 100;
        p.gravity = Gravity.BOTTOM | Gravity.RIGHT;

        return p;
    }

    public void show(){
        super.show();

        String text = "";
        text = mAction.toString();
        mTextV.setText(text);
        doit();
    }


    private void doit() {
        Log.d(TAG, "doit");
        getContentView().postDelayed(mDismissAction, DELAY_MILLIS);
        mAction.doIt();
    }

    @Override
    public void onClick(View v) {

        Log.d(TAG, "undoIt");
        getContentView().removeCallbacks(mDismissAction);
        mAction.undoIt();

        dismiss();
    }

    public static interface IDoUnDo {
        public void doIt();
        public void undoIt();
    }

    public static class DeleteTasksAction implements DoUndoWindow.IDoUnDo {

        private final List<TaskData> mTask;

        DeleteTasksAction(List<TaskData> task){
            mTask = task;
        }
        DeleteTasksAction(TaskData task){
            mTask = new ArrayList<TaskData>();
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

        @Override
        public String toString() {
            return mTask.size() == 1 ? "task " + mTask.get(0).getName() + " has deleted!"
                                    : mTask.size() + " tasks have deleted!";
        }
    }
    public static class DoneTasksAction implements DoUndoWindow.IDoUnDo {

        private final List<TaskData> mTask;

        DoneTasksAction(List<TaskData> task){
            mTask = task;
        }
        DoneTasksAction(TaskData task){
            mTask = new ArrayList<TaskData>();
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


        @Override
        public String toString() {
            return mTask.size() == 1 ? "task " + mTask.get(0).getName() + " mark as done!"
                    : mTask.size() + " tasks mark as done!";
        }
    }
}

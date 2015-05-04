package com.bombtime.bombtime;


import android.os.Bundle;
import android.view.Menu;

public class AddTaskActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_task);

        getFragmentManager().beginTransaction()
                .add(R.id.fragment_container,
                        AddTaskFragment.newInstance(getIntent().getIntExtra(TaskDetailActivity.EXTRA_TASK_ID, -1)))
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}

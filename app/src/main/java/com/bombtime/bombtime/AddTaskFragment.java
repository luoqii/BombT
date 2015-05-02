package com.bombtime.bombtime;


import java.sql.SQLException;

import com.j256.ormlite.dao.Dao;

import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddTaskFragment extends BaseFragment {


    private EditText mNameV;

    public AddTaskFragment() {
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
        return inflater.inflate(R.layout.fragment_add_task, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mNameV = ((EditText)view.findViewById(R.id.name));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_add_task_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_done:
                checkAndSave();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkAndSave() {
    	if (dataIsReady()) {
    		TaskData task = new TaskData();
    		task.name = mNameV.getText().toString();
    		task.endTime = System.currentTimeMillis() + 2 * 10 * 1000;
    		
    		Dao<TaskData, Integer> dao;
			try {
				dao = getHelper().getTaskDataDao();
	    		dao.create(task);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			getActivity().finish();
    	}
    }


	private boolean dataIsReady() {
		boolean ready = !TextUtils.isEmpty(mNameV.getText());
		return ready;
	}
    
    

}

package com.bombtime.bombtime;

import android.util.Log;

import com.j256.ormlite.field.DatabaseField;

public class TaskData {
	private static final String TAG = TaskData.class.getSimpleName();

	public static final String FIELD_NAME = "name";
	public static final String FIELD_TYPE = "type";
	public static final String FIELD_STATE = "state";
	public static final String FIELD_START_TIME = "startTime";
	public static final String FIELD_END_TIME = "endTime";
	public static final String FIELD_MARK_AS_DELETE = "markAsDelete";
	public static final String FIELD_PENDING_STATE = "pendingState";

	public static final int STATE_NEW_CREATE = 0;
	public static final int STATE_START = 1;
	public static final int STATE_PAUSE = 2;
	public static final int STATE_DONE = 3;
	public static final int STATE_FAIL = 4;

	public static final int TYPE_BOMB = 0;
	public static final int TYPE_TIMED_BOMB = 1;
	public static final int TYPE_SUPER_BOMB = 2;

	@DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(columnName = FIELD_NAME)
    private String name;
	@DatabaseField
	private long createdTime;
    /**
     * in milliseconds
     */
    @DatabaseField
    private long startTime;
    /**
     * in milliseconds
     */
    @DatabaseField
    private long endTime;
    @DatabaseField
    private long workTime;
	private long currentTime;
	@DatabaseField(columnName = FIELD_STATE)
	private int state;
	/**
	 * for do-redo.
	 */
	@DatabaseField(columnName = FIELD_PENDING_STATE)
	private int pendingState;
	@DatabaseField(columnName = FIELD_TYPE)
	private int type;
	@DatabaseField(columnName = FIELD_MARK_AS_DELETE)
	private boolean markAsDelete;

    
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	public long getEndTime() {
		return endTime;
	}
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	public long getWorkTime() {
		return workTime;
	}
	public void setWorkTime(long workTime) {
		this.workTime = workTime;
	}
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
		if (state == STATE_START && startTime == 0){
			startTime = System.currentTimeMillis();
		}
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String toDebugStr(){
		String pStr = String.format("% 2.5f", ((float)(currentTime - startTime) / (endTime - startTime)));
		return toTypeStr(type) + "|" + toStateStr(state)
				+ (markAsDelete == true ? "|D" : "" )
				+ (state == STATE_START ? "|" + pStr : "")
				;
	}

	String toTypeStr(int type){
		String str = "unknown type";
		switch (type){
			case TYPE_BOMB:
				str = "TYPE_BOME";
				break;
			case TYPE_TIMED_BOMB:
				str = "TYPE_TIMED_BOMB";
				break;
			case TYPE_SUPER_BOMB:
				str = "TYPE_SUPER_BOMB";
				break;
		}

		return str;
	}

	String toStateStr(int state){
		String str = "unknown type";
		switch (state){
			case STATE_DONE:
				str = "STATE_DONE";
				break;
			case STATE_NEW_CREATE:
				str = "STATE_NEW_CREATE";
				break;
			case STATE_PAUSE:
				str = "STATE_PAUSE";
				break;
			case STATE_START:
				str = "STATE_START";
				break;
			case STATE_FAIL:
				str = "STATE_FAIL";
				break;
		}

		return str;
	}

	/**
	 * in milliseconds
	 */
	public long getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
	}

	public long getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(long currentTime) {
		this.currentTime = currentTime;
		if (currentTime < startTime) {
			Log.w(TAG, "currentTime is before startTime");
		}

	}

	public boolean isMarkAsDelete() {
		return markAsDelete;
	}

	public void setMarkAsDelete(boolean markAsDelete) {
		this.markAsDelete = markAsDelete;
	}

	public int getPendingState() {
		return pendingState;
	}

	public void setPendingState(int pendingState) {
		this.pendingState = pendingState;
	}
}

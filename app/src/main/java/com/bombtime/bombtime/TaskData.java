package com.bombtime.bombtime;

import com.j256.ormlite.field.DatabaseField;

public class TaskData {
	public static final String FIELD_NAME = "name";
	public static final String FIELD_TYPE = "type";
	public static final String FIELD_STATE = "state";
	public static final String FIELD_NAME = "name";
	public static final String FIELD_NAME = "name";
    public static final int STATE_NEW_CREATE = 0;
    public static final int STATE_START = 1;
    public static final int STATE_PAUSE = 2;
	public static final int STATE_DONE = 3;

	public static final int TYPE_BOMB = 0;
	public static final int TYPE_TIMED_BOMB = 1;
	public static final int TYPE_SUPER_BOMB = 2;

	@DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(foreignColumnName = FIELD_NAME)
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
	@DatabaseField(foreignColumnName = FIELD_STATE)
	private int state;
	@DatabaseField(foreignColumnName = FIELD_TYPE)
	private int type;
    
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
		return toTypeStr(type) + "|" + toStateStr(state);
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
}

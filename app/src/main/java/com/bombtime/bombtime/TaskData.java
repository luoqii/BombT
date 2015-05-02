package com.bombtime.bombtime;

import com.j256.ormlite.field.DatabaseField;

public class TaskData {
	@DatabaseField(generatedId = true)
    public int id;

	@DatabaseField
    public long endTime;
	@DatabaseField
    public String name;
}

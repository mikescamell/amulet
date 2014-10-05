package scamell.michael.amulet;

import android.util.Log;

import org.json.JSONObject;

public class TaskEntry {

    protected String date, taskType, taskValue, units;

    public TaskEntry() {
        //default constructor
    }

    public TaskEntry(String mTimeStamp, String mTaskType, String mTaskValue, String mUnits) {
        this.date = mTimeStamp;
        this.taskType = mTaskType;
        this.taskValue = mTaskValue;
        this.units = mUnits;
    }

    public TaskEntry(JSONObject jsonObject) {
        try {
            date = jsonObject.getString("date");
            taskType = jsonObject.getString("tasktype");
            taskValue = jsonObject.getString("taskvalue");
            units = jsonObject.getString("units");
        } catch (Exception e) {
            Log.e("ERROR_TASK_ENTRY_OBJECT", "Couldn't create object from JSONObject");
            e.printStackTrace();
        }
    }
}

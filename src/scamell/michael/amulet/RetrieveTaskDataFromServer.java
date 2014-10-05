package scamell.michael.amulet;


import android.content.Context;
import android.util.Log;

import java.text.SimpleDateFormat;


public class RetrieveTaskDataFromServer implements OnRetrieveHttpData {

    private final Context context;

    RetrieveTaskDataFromServer(Context context) {
        this.context = context;
    }

    protected void retrieveInspectionTaskEntries() {
        RetrieveHTTPDataAsync retrieveTaskEntries = new RetrieveHTTPDataAsync(this);
        String mEmail = SharedPreferencesWrapper.getFromPrefs(context, "email", "NO_EMAIL");
        String mPassword = SharedPreferencesWrapper.getFromPrefs(context, "password", "NO_PASSWORD");
        retrieveTaskEntries.execute("http://08309.net.dcs.hull.ac.uk/api/admin/taskhistory?username=" + mEmail + "&password=" + mPassword + "&tasktype=inspection");
    }

    protected void retrieveSequenceTaskEntries() {
        RetrieveHTTPDataAsync retrieveTaskEntries = new RetrieveHTTPDataAsync(this);
        String mEmail = SharedPreferencesWrapper.getFromPrefs(context, "email", "NO_EMAIL");
        String mPassword = SharedPreferencesWrapper.getFromPrefs(context, "password", "NO_PASSWORD");
        retrieveTaskEntries.execute("http://08309.net.dcs.hull.ac.uk/api/admin/taskhistory?username=" + mEmail + "&password=" + mPassword + "&tasktype=sequence");
    }

    @Override
    public void onRetrieveTaskCompleted(String httpData) {
        Log.i("Server Response", httpData);
        processTaskEntries(httpData);
    }

    private void processTaskEntries(String httpData) {
        SimpleDateFormat convertToDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        TaskEntries taskEntries = new TaskEntries();
        taskEntries = TaskEntries.createDDEntriesFromWebServer(httpData);
        int numEntries = taskEntries.getNumEntries();
        //if the number of entries isn't 0 then save to local storage. Otherwise it is a completely
        //new user and there's nothing to save yet.
        if (numEntries != 0) {
            //find the task type of the data returned to determine if we are looking for an inspection
            //calibration time or sequence calibration time
            String taskType = taskEntries.getEntry(0).taskType;
            if (taskType.equals("sequence")) {
                float sequenceTaskCalibrationTime = taskEntries.findTaskCalibrationTime();
                if (sequenceTaskCalibrationTime != 0) {
                    SharedPreferencesWrapper.saveToPrefs(context, "calibration_time_sequence_task", String.valueOf(sequenceTaskCalibrationTime));
                }
                taskEntries.saveToStorage(context, "SequenceTaskEntries.json");
            } else if (taskType.equals("inspection")) {
                float inspectionTaskCalibrationTime = taskEntries.findTaskCalibrationTime();
                if (inspectionTaskCalibrationTime != 0) {
                    SharedPreferencesWrapper.saveToPrefs(context, "calibration_time_inspection_task", (int) inspectionTaskCalibrationTime);
                }
                taskEntries.saveToStorage(context, "InspectionTaskEntries.json");
            }
        }
    }
}

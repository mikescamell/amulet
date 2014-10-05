package scamell.michael.amulet;


import android.content.Context;
import android.util.Log;

import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;


public class SendTaskEntryToServer implements OnPostHttpData {

    private static final String taskServerURL = "http://08309.net.dcs.hull.ac.uk/api/admin/task";

    private final Context context;
    private final TaskEntry taskEntry;

    SendTaskEntryToServer(Context context, TaskEntry taskEntry) {
        this.context = context;
        this.taskEntry = taskEntry;
    }

    /**
     * -post reference
     * http://hmkcode.com/android-send-json-data-to-server/
     * -setting header content type ref
     * http://stackoverflow.com/questions/14119410/json-not-working-with-httppost-probably-around-setentity
     */
    public void sendTaskEntryToServer() {
        StringEntity se = null;
        JSONObject taskEntriesJSONObject = TaskEntries.buildTaskEntriesJSONObjectForServer(context, taskEntry);
        String taskEntriesJSON = taskEntriesJSONObject.toString();
        try {
            se = new StringEntity(taskEntriesJSON);
            se.setContentType("application/json");
        } catch (UnsupportedEncodingException e) {
            Log.e("ERROR_UNSUPPORTED_ENCODING_EXCEPTION", "Couldn't create string entity for post");
            e.printStackTrace();
        }
        PostHTTPDataAsync postDrinkDiaryEntries = new PostHTTPDataAsync(this, se);
        postDrinkDiaryEntries.execute(taskServerURL);
    }

    @Override
    public void onPostTaskCompleted(String httpData) {
        Log.i("INFO_SERVER_RESPONSE_DRINK_DIARY", httpData);
        checkServerResponseToPost(httpData);
    }

    public void checkServerResponseToPost(String response) {
        if (response.contains("success") || response.contains("received")) {
            Log.i("TASK_UPLOAD", "Task uploaded successfully," + response);
            //each time it's successful set it to false. See MainActivity onCreate for why
            SharedPreferencesWrapper.saveToPrefs(context, "reUpload_tasks", false);
        } else if (response.equals("INVALID")) {
            Log.w("WARNING_SERVER_RESPONSE", "Drink diary entry sent was invalid");
        } else {
            Log.e("TASK_UPLOAD", "Task upload failed" + response);
            TaskEntries taskEntries = TaskEntries.createTaskEntriesFromStorage(context, "ReUploadTasks.json");
            taskEntries.addEntry(taskEntry);
            taskEntries.saveToStorage(context, "ReUploadTasks.json");
            SharedPreferencesWrapper.saveToPrefs(context, "reUpload_tasks", true);
        }
    }

}

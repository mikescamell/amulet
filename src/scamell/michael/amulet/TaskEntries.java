package scamell.michael.amulet;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

public class TaskEntries {

    private final LinkedList<TaskEntry> entries;

    public TaskEntries() {
        entries = new LinkedList<TaskEntry>();
    }

    public static TaskEntries createDDEntriesFromWebServer(String serviceData) {
        TaskEntries taskEntries = new TaskEntries();
        try {
            JSONArray entriesArray = new JSONArray(serviceData);
            for (int i = 0; i < entriesArray.length(); i++) {
                JSONObject entryObject = entriesArray.getJSONObject(i);
                TaskEntry taskEntry = new TaskEntry();
                taskEntry.date = entryObject.getString("timestamp");
                taskEntry.taskType = entryObject.getString("tasktype");
                taskEntry.taskValue = entryObject.getString("taskvalue");
                taskEntry.units = entryObject.getString("unitsconsumed");
                taskEntries.addFirstEntry(taskEntry);
            }
        } catch (JSONException e) {
            Log.e("JSON_EXCEPTION", e.getMessage());
        }
        return taskEntries;
    }

    public static TaskEntries createTaskEntriesFromStorage(Context context, String file) {
        TaskEntries taskEntries = new TaskEntries();
        taskEntries.loadStateFromFileStream(context, file);
        return taskEntries;
    }

    public static JSONObject buildTaskEntriesJSONObjectForServer(Context context, TaskEntry taskEntry) {
        JSONObject jsonObject = new JSONObject();
        try {
            JSONArray entriesArray = new JSONArray();
            JSONObject entriesArrayJSONObject = new JSONObject();
            entriesArrayJSONObject.put("tasktype", taskEntry.taskType);
            entriesArrayJSONObject.put("taskvalue", taskEntry.taskValue);
            entriesArrayJSONObject.put("unitsconsumed", taskEntry.units);
            entriesArrayJSONObject.put("timestamp", taskEntry.date);
            entriesArray.put(entriesArrayJSONObject);
            jsonObject.put("tasks", entriesArray);
            jsonObject.put("password", SharedPreferencesWrapper.getFromPrefs(context, "password", "NO_PASSWORD"));
            jsonObject.put("username", SharedPreferencesWrapper.getFromPrefs(context, "email", "NO_EMAIL"));
        } catch (Exception e) {
            Log.e("ERROR_JSON_OBJECT", "Couldn't create JSON object to send to server");
        }
        return jsonObject;
    }

    public static JSONObject buildTaskEntriesJSONObjectForServer(Context context, TaskEntries taskEntries) {
        JSONObject jsonObject = new JSONObject();
        try {
            JSONArray entriesArray = new JSONArray();
            JSONObject entriesArrayJSONObject = null;
            for (int i = 0; i < taskEntries.getNumEntries(); i++) {
                entriesArrayJSONObject = new JSONObject();
                entriesArrayJSONObject.put("tasktype", taskEntries.getEntry(i).taskType);
                entriesArrayJSONObject.put("taskvalue", taskEntries.getEntry(i).taskValue);
                entriesArrayJSONObject.put("unitsconsumed", taskEntries.getEntry(i).units);
                entriesArrayJSONObject.put("timestamp", taskEntries.getEntry(i).date);
                entriesArray.put(entriesArrayJSONObject);
            }
            jsonObject.put("tasks", entriesArray);
            jsonObject.put("password", SharedPreferencesWrapper.getFromPrefs(context, "password", "NO_PASSWORD"));
            jsonObject.put("username", SharedPreferencesWrapper.getFromPrefs(context, "email", "NO_EMAIL"));
        } catch (Exception e) {
            Log.e("ERROR_JSON_OBJECT", "Couldn't create JSON object to send to server");
        }
        return jsonObject;
    }

    protected void addFirstEntry(TaskEntry taskEntry) {
        entries.addFirst(taskEntry);
    }

    protected void addEntry(TaskEntry taskEntry) {
        entries.add(taskEntry);
    }

    public TaskEntry getEntry(int index) {
        return entries.get(index);
    }

//    protected void removeEntry(int entryPosition) {
//        entries.remove(entryPosition);
//    }

    public int getNumEntries() {
        return entries.size();
    }

//    public LinkedList<TaskEntry> getEntries() {
//        return entries;
//    }

    public float findTaskCalibrationTime() {
        float quickestCalibration = 0;
        for (TaskEntry taskEntry : entries) {
            if (taskEntry.units.equals("0.0")) {
                float currentCalibration = Float.valueOf(taskEntry.taskValue);
                if (quickestCalibration == 0) {
                    quickestCalibration = currentCalibration;
                }
                if (currentCalibration < quickestCalibration) {
                    quickestCalibration = currentCalibration;
                }
            }
        }
        return quickestCalibration;
    }

    public void saveToStorage(Context context, String file) {
        outputToFileStream(context, file);
    }

    private void outputToFileStream(Context context, String fileName) {
        FileOutputStream outputStream;
        try {
            outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            outputToFileStream(outputStream);
            outputStream.close();
        } catch (Exception e) {
            Log.e("OUTPUT_TO_FILE_STREAM", "Couldn't save file");
        }
    }

    public void outputToFileStream(FileOutputStream outputFileStream) {
        String jsonString;
        JSONObject jsonObject = toJSONObject();
        jsonString = jsonObject.toString();
        if (jsonString != null) {
            try {
                outputFileStream.write(jsonString.getBytes());
            } catch (Exception e) {
                Log.e("OUTPUT_TO_FILE_STREAM", "Couldn't save file");
            }
        }
    }

    private JSONObject toJSONObject() {
        JSONObject returnObject = new JSONObject();
        JSONArray taskEntriesArray = new JSONArray();
        for (TaskEntry taskEntry : entries) {
            JSONObject taskJSONObject = new JSONObject();
            try {
                taskJSONObject.put("tasktype", taskEntry.taskType);
                taskJSONObject.put("taskvalue", taskEntry.taskValue);
                taskJSONObject.put("date", taskEntry.date);
                taskJSONObject.put("units", taskEntry.units);
            } catch (Exception e) {
                Log.e("ERROR_TOJSONOBJECT", "Couldn't create a JSONObject");
            }
            taskEntriesArray.put(taskJSONObject);
        }
        try {
            returnObject.put("taskEntries", taskEntriesArray);
        } catch (JSONException e) {
            Log.e("JSON_EXCEPTION", "Couldn't create JSON Object");
            e.printStackTrace();
        }
        return returnObject;
    }

    private void loadStateFromFileStream(Context context, String fileName) {
        FileInputStream inputStream;
        try {
            inputStream = context.openFileInput(fileName);
            JSONObject jsonObject = convertInputStreamToJSONObject(inputStream);
            if (jsonObject != null) {
                loadStateFromJSONObject(jsonObject);
            }
            inputStream.close();
        } catch (Exception e) {
            Log.e("ERROR_INPUT_STREAM", "Couldn't load from input stream");
            e.printStackTrace();
        }
    }

    private JSONObject convertInputStreamToJSONObject(FileInputStream inputStream) {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            String jsonString = builder.toString();
            return new JSONObject(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("Convert_Input_Stream_To_JSON_Object", "Couldn't read from input stream");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("Convert_Input_Stream_To_JSON_Object", "Couldn't create JSON object");
        }
        return null;
    }

    public String toString() {
        JSONObject jsonObject = toJSONObject();
        return jsonObject.toString();
    }

    private void loadStateFromJSONObject(JSONObject jsonObject) {
        JSONArray entriesArray;
        try {
            entriesArray = jsonObject.getJSONArray("taskEntries");
            for (int i = 0; i < entriesArray.length(); i++) {
                JSONObject tempJSONObject = entriesArray.getJSONObject(i);

                TaskEntry taskEntry = new TaskEntry(tempJSONObject);
                if (taskEntry.taskValue == null || taskEntry.taskType == null || taskEntry.units == null || taskEntry.date == null) {
                    Log.w("WARNING_LOAD_STATE_FROM_JSONOBJECT", "JSONObject value returned null for an entry");
                } else {
                    entries.add(taskEntry);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("Load_State_From_JSON_Object", "Couldn't load JSON Object");
        }
    }
}

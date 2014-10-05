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


public class DrinkDiaryEntries {
    private final LinkedList<DrinkDiaryEntry> entries;

    public DrinkDiaryEntries() {
        entries = new LinkedList<DrinkDiaryEntry>();
    }

    //only needs activating if new login
    public static DrinkDiaryEntries createDDEntriesFromWebServer(String serviceData) {
        DrinkDiaryEntries drinkDiaryEntries = new DrinkDiaryEntries();
        try {
            JSONArray entriesArray = new JSONArray(serviceData);
            for (int i = 0; i < entriesArray.length(); i++) {
                JSONObject entryObject = entriesArray.getJSONObject(i);
                DrinkDiaryEntry dDEO = new DrinkDiaryEntry();
                dDEO.date = entryObject.getString("timestamp");
                String temp = entryObject.getString("drinktype");
                int dashPos = temp.indexOf("-");
                dDEO.drinkName = temp.substring(0, dashPos - 1);
                dDEO.drinkType = temp.substring(dashPos + 2, temp.length());
                dDEO.units = entryObject.getString("unitsconsumed");
                drinkDiaryEntries.addEntry(dDEO);
            }
        } catch (JSONException e) {
            Log.e("JSON_EXCEPTION", e.getMessage());
        }
        return drinkDiaryEntries;
    }

    public static DrinkDiaryEntries createDrinkDiaryEntriesFromStorage(Context context, String fileName) {
        DrinkDiaryEntries drinkDiaryEntries = new DrinkDiaryEntries();
        drinkDiaryEntries.loadStateFromFileStream(context, fileName);
        return drinkDiaryEntries;
    }

    public static JSONObject buildDrinkDiaryEntriesJSONObjectForServer(Context context, DrinkDiaryEntry dDEO) {
        JSONObject jsonObject = new JSONObject();
        try {
            JSONArray entriesArray = new JSONArray();
            JSONObject entriesArrayJSONObject = new JSONObject();
            entriesArrayJSONObject.put("drinktype", dDEO.drinkName + " - " + dDEO.drinkType);
            entriesArrayJSONObject.put("unitsconsumed", dDEO.units);
            entriesArrayJSONObject.put("timestamp", dDEO.date);
            entriesArray.put(entriesArrayJSONObject);
            jsonObject.put("entries", entriesArray);
            jsonObject.put("password", SharedPreferencesWrapper.getFromPrefs(context, "password", "NO_PASSWORD"));
            jsonObject.put("username", SharedPreferencesWrapper.getFromPrefs(context, "email", "NO_EMAIL"));
        } catch (Exception e) {
            Log.e("ERROR_JSON_OBJECT", "Couldn't create JSON object to send to server");
        }
        return jsonObject;
    }

    public static JSONObject buildDrinkDiaryEntriesJSONObjectForServer(Context context, DrinkDiaryEntries drinkDiaryEntries) {
        JSONObject jsonObject = new JSONObject();
        try {
            JSONArray entriesArray = new JSONArray();
            JSONObject entriesArrayJSONObject = null;
            for (int i = 0; i < drinkDiaryEntries.getNumEntries(); i++) {
                entriesArrayJSONObject = new JSONObject();
                entriesArrayJSONObject.put("drinktype", drinkDiaryEntries.getEntry(i).drinkName + " - " + drinkDiaryEntries.getEntry(i).drinkType);
                entriesArrayJSONObject.put("unitsconsumed", drinkDiaryEntries.getEntry(i).units);
                drinkDiaryEntries.getEntry(i).date = drinkDiaryEntries.getEntry(i).date;
                entriesArrayJSONObject.put("timestamp", drinkDiaryEntries.getEntry(i).date);
                entriesArray.put(entriesArrayJSONObject);
            }
            jsonObject.put("entries", entriesArray);
            jsonObject.put("password", SharedPreferencesWrapper.getFromPrefs(context, "password", "NO_PASSWORD"));
            jsonObject.put("username", SharedPreferencesWrapper.getFromPrefs(context, "email", "NO_EMAIL"));
        } catch (Exception e) {
            Log.e("ERROR_JSON_OBJECT", "Couldn't create JSON object to send to server");
        }
        return jsonObject;
    }

    protected void addFirstEntry(DrinkDiaryEntry drinkDiaryEntry) {
        entries.addFirst(drinkDiaryEntry);
    }

    protected void addEntry(DrinkDiaryEntry drinkDiaryEntry) {
        entries.add(drinkDiaryEntry);
    }

    public DrinkDiaryEntry getEntry(int index) {
        return entries.get(index);
    }

    protected void removeAllEntries() {
        entries.clear();
    }

    public int getNumEntries() {
        return entries.size();
    }

    public LinkedList<DrinkDiaryEntry> getEntries() {
        return entries;
    }

//    public String getTimeStamp(int index) {
//        DrinkDiaryEntry drinkDiaryEntry = entries.get(index);
//        return drinkDiaryEntry.date;
//    }

    public Boolean getSelectedStatus(int index) {
        DrinkDiaryEntry drinkDiaryEntry = entries.get(index);
        return drinkDiaryEntry.isSelected;
    }

    public void setSelectedStatus(int index, Boolean selected) {
        DrinkDiaryEntry drinkDiaryEntry = entries.get(index);
        drinkDiaryEntry.isSelected = selected;
    }

//    public String getDrinkType(int index) {
//        DrinkDiaryEntry drinkDiaryEntry = entries.get(index);
//        return drinkDiaryEntry.drinkType;
//    }

//    public String getUnitsConsumed(int index) {
//        DrinkDiaryEntry drinkDiaryEntry = entries.get(index);
//        return drinkDiaryEntry.units;
//    }

    public void saveToStorage(Context context, String fileName) {
        outputToFileStream(context, fileName);
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
        JSONArray drinkDiaryEntriesArray = new JSONArray();
        for (DrinkDiaryEntry dDE : entries) {
            JSONObject dDEJSONObject = new JSONObject();
            try {
                dDEJSONObject.put("drinkName", dDE.drinkName + " - " + dDE.drinkType);
                dDEJSONObject.put("date", dDE.date);
                dDEJSONObject.put("units", dDE.units);
            } catch (Exception e) {
                Log.e("ERROR_TOJSONOBJECT", "Couldn't create a JSONObject");
            }
            drinkDiaryEntriesArray.put(dDEJSONObject);
        }
        try {
            returnObject.put("drinkDiaryEntries", drinkDiaryEntriesArray);
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
            entriesArray = jsonObject.getJSONArray("drinkDiaryEntries");
            for (int i = 0; i < entriesArray.length(); i++) {
                JSONObject tempJSONObject = entriesArray.getJSONObject(i);

                DrinkDiaryEntry dDEO = new DrinkDiaryEntry(tempJSONObject);
                if (dDEO.drinkName == null || dDEO.units == null || dDEO.date == null) {
                    Log.w("WARNING_LOAD_STATE_FROM_JSONOBJECT", "JSONObject value returned null");
                } else {
                    entries.add(dDEO);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i("Load_State_From_JSON_Object", "Couldn't load JSON Object");
        }
    }
}

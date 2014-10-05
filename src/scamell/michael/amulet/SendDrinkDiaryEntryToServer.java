package scamell.michael.amulet;

import android.content.Context;
import android.util.Log;

import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class SendDrinkDiaryEntryToServer implements OnPostHttpData {

    private final Context context;
    private final DrinkDiaryEntry drinkDiaryEntry;
    private final String drinkDiaryServerURL = "http://08309.net.dcs.hull.ac.uk/api/admin/drink";


    public SendDrinkDiaryEntryToServer(Context context, DrinkDiaryEntry drinkDiaryEntry) {
        this.context = context;
        this.drinkDiaryEntry = drinkDiaryEntry;
    }

    /**
     * -post reference
     * http://hmkcode.com/android-send-json-data-to-server/
     * -setting header content type ref
     * http://stackoverflow.com/questions/14119410/json-not-working-with-httppost-probably-around-setentity
     */
    public void sendDrinkDiaryEntryToServer() {
        StringEntity se = null;
        JSONObject drinkDiaryEntriesJSONObject = DrinkDiaryEntries.buildDrinkDiaryEntriesJSONObjectForServer(context, drinkDiaryEntry);
        String drinkDiaryEntriesJSON = drinkDiaryEntriesJSONObject.toString();
        try {
            se = new StringEntity(drinkDiaryEntriesJSON);
            se.setContentType("application/json");
        } catch (UnsupportedEncodingException e) {
            Log.e("ERROR_UNSUPPORTED_ENCODING_EXCEPTION", "Couldn't create string entity for post");
            e.printStackTrace();
        }
        PostHTTPDataAsync postDrinkDiaryEntries = new PostHTTPDataAsync(this, se);
        postDrinkDiaryEntries.execute(drinkDiaryServerURL);
    }


    @Override
    public void onPostTaskCompleted(String httpData) {
        Log.i("INFO_SERVER_RESPONSE_DRINK_DIARY", httpData);
        checkServerResponseToPost(httpData);
    }

    private void checkServerResponseToPost(String response) {
        if (response.contains("received")) {
            Log.i("DRINK_DIARY_UPLOAD", "Drink Diary Entry uploaded successfully," + response);
            SharedPreferencesWrapper.saveToPrefs(context, "drinkDiaryUploadSuccess", true);
        } else if (response.contains("invalid")) {
            Log.w("WARNING_SERVER_RESPONSE", "Drink diary entry sent was invalid");
        } else {
            Log.e("DRINK_DIARY_UPLOAD", "Drink Diary Entry upload failed" + response);
            DrinkDiaryEntries drinkDiaryReUploadEntries = DrinkDiaryEntries.createDrinkDiaryEntriesFromStorage(context, "ReUploadDrinkDiary.json");
            drinkDiaryReUploadEntries.addFirstEntry(drinkDiaryEntry);
            drinkDiaryReUploadEntries.saveToStorage(context, "ReUploadDrinkDiary.json");
            SharedPreferencesWrapper.saveToPrefs(context, "reUpload_drink_diary", true);
        }
    }

}

package scamell.michael.amulet;

import android.content.Context;
import android.util.Log;

import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class SendDrinkDiaryEntriesToServer implements OnPostHttpData {

    private final Context context;
    private final DrinkDiaryEntries drinkDiaryEntries;
    private final String drinkDiaryServerURL = "http://08309.net.dcs.hull.ac.uk/api/admin/drink";


    public SendDrinkDiaryEntriesToServer(Context context, DrinkDiaryEntries drinkDiaryEntries) {
        this.context = context;
        this.drinkDiaryEntries = drinkDiaryEntries;
    }

    /**
     * -post reference
     * http://hmkcode.com/android-send-json-data-to-server/
     * -setting header content type ref
     * http://stackoverflow.com/questions/14119410/json-not-working-with-httppost-probably-around-setentity
     */
    public void sendDrinkDiaryEntriesToServer() {
        StringEntity se = null;
        JSONObject drinkDiaryEntriesJSONObject = DrinkDiaryEntries.buildDrinkDiaryEntriesJSONObjectForServer(context, drinkDiaryEntries);
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
            SharedPreferencesWrapper.saveToPrefs(context, "reUpload_drink_diary", false);
            Log.i("DRINK_DIARY_UPLOAD", "Drink Diary Entry uploaded successfully," + response);
        } else if (response.contains("INVALID")) {
            Log.w("WARNING_SERVER_RESPONSE", "Drink diary entry sent was invalid");
        } else {
            SharedPreferencesWrapper.saveToPrefs(context, "reUpload_drink_diary", true);
        }
    }

}

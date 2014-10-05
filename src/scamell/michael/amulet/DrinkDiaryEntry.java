package scamell.michael.amulet;

import android.util.Log;

import org.json.JSONObject;

public class DrinkDiaryEntry {

    protected String date, drinkName, drinkType, units;
    protected Boolean isSelected = false;

    public DrinkDiaryEntry() {
        //default constructor
    }

    public DrinkDiaryEntry(String mTimeStamp, String mDrinkName, String mDrinkType, String mUnits) {
        date = mTimeStamp;
        drinkName = mDrinkName;
        drinkType = mDrinkType;
        units = mUnits;
    }

    public DrinkDiaryEntry(JSONObject jsonObject) {
        try {
            date = jsonObject.getString("date");
            String s = jsonObject.getString("drinkName");
            int dashPos = s.indexOf("-");
            drinkName = s.substring(0, dashPos - 1);
            drinkType = s.substring(dashPos + 2, s.length());
            units = jsonObject.getString("units");
        } catch (Exception e) {
            Log.e("ERROR_DRINK_DIARY_OBJECT", "Couldn't create object from JSONObject");
            e.printStackTrace();
        }
    }


}

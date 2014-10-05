package scamell.michael.amulet;


import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class FavouriteDrinkUtility {

    public static void saveFavouriteDrinkToStorage(Context context, String drinkName, String drinkType, String drinkVolume, String drinkVolumeType, String drinkQuantity, String drinkUnits, String drinkABV, String drinkVolumeTypePos, String drinkTypePos) {
        String favouriteDrinks = SharedPreferencesWrapper.getFromPrefs(context, "favourite_drinks", "NO_FAVOURITE_DRINKS");
        if (favouriteDrinks.equals("NO_FAVOURITE_DRINKS")) {
            favouriteDrinks = drinkName + "^" + drinkType + "^" + drinkVolume + "^" + drinkVolumeType + "^" + drinkQuantity + "^" + drinkUnits + "^" + drinkABV + "^" + drinkVolumeTypePos + "^" + drinkTypePos + ";";
        } else {
            favouriteDrinks = favouriteDrinks + drinkName + "^" + drinkType + "^" + drinkVolume + "^" + drinkVolumeType + "^" + drinkQuantity + "^" + drinkUnits + "^" + drinkABV + "^" + drinkVolumeTypePos + "^" + drinkTypePos + ";";
        }
        SharedPreferencesWrapper.saveToPrefs(context, "favourite_drinks", favouriteDrinks);
    }

    private static List<String[]> createFavouriteDrinksFromStorage(Context context) {
        String favouriteDrinks = SharedPreferencesWrapper.getFromPrefs(context, "favourite_drinks", "No Favourite Drinks");
        if (!favouriteDrinks.equals("No Favourite Drinks")) {
            String[] temp1 = favouriteDrinks.split(";");
            List<String[]> favouriteDrinksArray = new ArrayList<String[]>();
            for (String string : temp1) {
                String[] temp2 = string.split("\\^");
                favouriteDrinksArray.add(temp2);
            }
            return favouriteDrinksArray;
        } else {
            List<String[]> noDrinks = new ArrayList<String[]>();
            String[] noDrinksStringArray = new String[1];
            noDrinksStringArray[0] = "No Favourite Drinks";
            noDrinks.add(noDrinksStringArray);
            return noDrinks;
        }
    }

    public static List<String> getFavouriteDrinkTitles(Context context) {
        List<String[]> favouriteDrinksArray = createFavouriteDrinksFromStorage(context);
        List<String> favouriteDrinkTitles = new ArrayList<String>();
        for (String[] stringArray : favouriteDrinksArray) {
            favouriteDrinkTitles.add(stringArray[0]);
        }
        return favouriteDrinkTitles;
    }

    public static String[] getFavouriteDrink(Context context, int pos) {
        List<String[]> favouriteDrinksArray = createFavouriteDrinksFromStorage(context);
        return favouriteDrinksArray.get(pos);
    }
}

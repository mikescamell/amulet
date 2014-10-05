package scamell.michael.amulet;

import android.content.Context;

public class SaveDrinkDiaryUtility {

    private final Context context;
    private final DrinkDiaryEntry drinkDiaryEntry;
    private final DrinkDiaryEntries drinkDiaryEntries;

    public SaveDrinkDiaryUtility(Context context, DrinkDiaryEntry drinkDiaryEntry, DrinkDiaryEntries drinkDiaryEntries) {
        this.context = context;
        this.drinkDiaryEntry = drinkDiaryEntry;
        this.drinkDiaryEntries = drinkDiaryEntries;
    }

    protected void saveDrinkDiaryEntryToStorage() {
        drinkDiaryEntries.addFirstEntry(drinkDiaryEntry);
        drinkDiaryEntries.saveToStorage(context, "DrinkDiaryEntries.json");
        SendDrinkDiaryEntryToServer sendDrinkDiaryEntryToServer = new SendDrinkDiaryEntryToServer(context, drinkDiaryEntry);
        sendDrinkDiaryEntryToServer.sendDrinkDiaryEntryToServer();
    }

}

package scamell.michael.amulet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class DrinkDiaryFavouriteDrinksDialogFragment extends DialogFragment {

    private final int DRINK_NAME = 0;
    private final int DRINK_TYPE = 1;
    private final int DRINK_UNITS = 5;

    private AlertDialog aD;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Favourite Drinks");
        ListView favouriteList = new ListView(getActivity());
        List<String> favouriteTitlesList = FavouriteDrinkUtility.getFavouriteDrinkTitles(getActivity());
        String[] favouriteTitlesArray = favouriteTitlesList.toArray(new String[favouriteTitlesList.size()]);
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1, favouriteTitlesArray);
        favouriteList.setAdapter(modeAdapter);
        favouriteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView item = (TextView) view;
                if (!item.getText().toString().equals("No Favourite Drinks")) {
                    String[] favouriteDrink = FavouriteDrinkUtility.getFavouriteDrink(getActivity(), position);
                    DrinkDiaryEntry drinkDiaryEntry = new DrinkDiaryEntry();
                    drinkDiaryEntry.drinkName = favouriteDrink[DRINK_NAME];
                    drinkDiaryEntry.drinkType = favouriteDrink[DRINK_TYPE];
                    drinkDiaryEntry.units = favouriteDrink[DRINK_UNITS];
                    drinkDiaryEntry.date = DateAndTime.getDateAndTimeNowForTasks();
                    DrinkDiaryEntries drinkDiaryEntries = DrinkDiaryEntries.createDrinkDiaryEntriesFromStorage(getActivity(), "DrinkDiaryEntries.json");
                    SaveDrinkDiaryUtility saveDrinkDiaryUtility = new SaveDrinkDiaryUtility(getActivity(), drinkDiaryEntry, drinkDiaryEntries);
                    saveDrinkDiaryUtility.saveDrinkDiaryEntryToStorage();
                    SharedPreferencesWrapper.saveToPrefs(getActivity(), "lastDrinkAdded", favouriteDrink[DRINK_NAME]);
                    SharedPreferencesWrapper.saveToPrefs(getActivity(), "unitsOfLastDrinkAdded", favouriteDrink[DRINK_UNITS]);
                    getTargetFragment().onActivityResult(getTargetRequestCode(), 1234, null);
                }
                aD.dismiss();
            }
        });
        builder.setView(favouriteList);
        aD = builder.create();
        aD.show();

        return aD;
    }
}
package scamell.michael.amulet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

public class UnitCalculatorFavouriteDrinksDialogFragment extends DialogFragment {

    private final int DRINK_NAME = 0;
    private final int DRINK_TYPE = 1;
    private final int DRINK_VOLUME = 2;
    private final int DRINK_VOLUME_TYPE = 3;
    private final int DRINK_QUANTITY = 4;
    private final int DRINK_UNITS = 5;
    private final int DRINK_ABV = 6;
    private final int DRINK_VOLUME_TYPE_POS = 7;
    private final int DRINK_TYPE_POS = 8;

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
                    Intent intent = new Intent();
                    intent.putExtra("drinkName", favouriteDrink[DRINK_NAME]);
                    intent.putExtra("drinkType", favouriteDrink[DRINK_TYPE]);
                    intent.putExtra("drinkVolume", favouriteDrink[DRINK_VOLUME]);
                    intent.putExtra("drinkVolumeType", favouriteDrink[DRINK_VOLUME_TYPE]);
                    intent.putExtra("drinkQuantity", Integer.valueOf(favouriteDrink[DRINK_QUANTITY]));
                    intent.putExtra("drinkUnits", favouriteDrink[DRINK_UNITS]);
                    intent.putExtra("drinkABV", favouriteDrink[DRINK_ABV]);
                    intent.putExtra("drinkVolumeTypePos", Integer.valueOf(favouriteDrink[DRINK_VOLUME_TYPE_POS]));
                    intent.putExtra("drinkTypePos", Integer.valueOf(favouriteDrink[DRINK_TYPE_POS]));
                    getTargetFragment().onActivityResult(getTargetRequestCode(), 4444, intent);
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
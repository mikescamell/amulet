package scamell.michael.amulet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;


public class DrinkDiaryFragment extends ListFragment {

    public static final int REQUEST_CODE = 1234;

    private DrinkDiaryEntries dDE = new DrinkDiaryEntries();
    private DrinkDiaryEntryAdapter adapter;
    private DrinkDiaryFavouriteDrinksDialogFragment drinkDiaryFavouriteDrinksDialogFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drink_diary, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        dDE = DrinkDiaryEntries.createDrinkDiaryEntriesFromStorage(getActivity(), "DrinkDiaryEntries.json");
        if (dDE != null) {
            adapter = new DrinkDiaryEntryAdapter(getActivity(), dDE);
            setListAdapter(adapter);
        }

        try {
            //noinspection ConstantConditions
            getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    if (!dDE.getSelectedStatus(position)) {
                        //set the drink entries selected status to track it for deletion
                        dDE.setSelectedStatus(position, true);
                    }
                    view.setBackgroundResource(R.color.AmuletBlue);
                    return true;
                }
            });
        } catch (Exception e) {
            Log.e("ERROR_NULL_POINTER", "Couldn't get view onLongItemClick");
            e.printStackTrace();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.action_bar_drink_diary, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.action_add:
                UnitCalculatorFragment unitCalculatorFragment = new UnitCalculatorFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                Bundle args = new Bundle();
                args.putBoolean("drink_diary_unit_calculator", true);
                unitCalculatorFragment.setArguments(args);
                fragmentManager.beginTransaction().replace(R.id.content_frame, unitCalculatorFragment, "unit_calculator").addToBackStack("Unit Calculator").commit();
                return true;
            case R.id.action_add_favourite:
                drinkDiaryFavouriteDrinksDialogFragment = new DrinkDiaryFavouriteDrinksDialogFragment();
                drinkDiaryFavouriteDrinksDialogFragment.setTargetFragment(this, REQUEST_CODE);
                drinkDiaryFavouriteDrinksDialogFragment.show(getFragmentManager(), "FavouriteDrinksDialogFragment");
                //delete drink diary item code
//            case R.id.action_delete:
//                for(int i = 0; i < dDE.getNumEntries(); i++)  {
//                    if(dDE.getEntry(i).isSelected) {
//                        dDE.removeEntry(i);
//                    }
//                }
//                dDE.saveToStorage(getActivity());
//                adapter.notifyDataSetChanged();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 1234) {
            dDE.removeAllEntries();
            DrinkDiaryEntries drinkDiaryEntries = DrinkDiaryEntries.createDrinkDiaryEntriesFromStorage(getActivity(), "DrinkDiaryEntries.json");
            for (int i = 0; i < drinkDiaryEntries.getNumEntries(); i++) {
                dDE.addEntry(drinkDiaryEntries.getEntry(i));
            }
            adapter.updateView(dDE);
        }
    }

}
package scamell.michael.amulet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class UnitCalculatorFragment extends Fragment {

    private static final int EXAMPLE_LIST_REQUEST_CODE = 1234;
    private static final int FAVOURITE_LIST_REQUEST_CODE = 4444;
    private final int maxQuantity = 100;
    private final int minQuantity = 1;
    private final int volMeasurementSpinnerMlLocation = 0;
    private final int volMeasurementSpinnerPintLocation = 1;
    private final int volMeasurementSpinnerLitresLocation = 2;
    private final int volMeasurementSpinnerClLocation = 3;
    private UnitCalculatorListener mListener;
    private Spinner drinkTypeSpinner;
    private EditText abvEditText, volumeEditText;
    private TextView unitValueTextView;
    private NumberPicker quantityNumberPicker;
    private Spinner volumeMeasurementSpinner;
    private EditText drinkNameEditText;
    private ImageButton confirmButton;
    private String drinkName;
    private String mABV;
    private String drinkVolume;
    private String drinkVolumeType;
    private int drinkVolumeTypePos;
    private String drinkType;
    private int drinkTypePos;
    private int drinkQuantity;
    private String unitsString;
    private Boolean saveToDrinkDiary = false;
    private Boolean firstStart;
    private Boolean stopCalcUnits = false;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mListener = (UnitCalculatorListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement UnitCalculatorListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_unit_calculator, container, false);

        final Bundle bundle = getArguments();
        firstStart = true;
        setHasOptionsMenu(true);

        drinkNameEditText = (EditText) rootView.findViewById(R.id.unit_calculator_drink_name_edittext);
        drinkTypeSpinner = (Spinner) rootView.findViewById(R.id.unit_calculator_drink_type_spinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_unit_calculator_example_item, getResources().getStringArray(R.array.drink_type_array));
        drinkTypeSpinner.setAdapter(spinnerArrayAdapter);
        abvEditText = (EditText) rootView.findViewById(R.id.unit_calc_enter_abv_edittext);
        volumeEditText = (EditText) rootView.findViewById(R.id.unit_calc_volume_edittext);
        volumeMeasurementSpinner = (Spinner) rootView.findViewById(R.id.unit_calc_volume_measurement_spinner);
        quantityNumberPicker = (NumberPicker) rootView.findViewById(R.id.numberPicker);
        quantityNumberPicker.setMaxValue(maxQuantity);
        quantityNumberPicker.setMinValue(minQuantity);
        quantityNumberPicker.setWrapSelectorWheel(false);
        unitValueTextView = (TextView) rootView.findViewById(R.id.unit_calc_unit_value);

        //sets up the start button as long as there is a boolean that specifies it is need included
        //in a bundle to start the fragment, otherwise it is ignored. used for starting tasks after
        //getting units
        confirmButton = (ImageButton) rootView.findViewById(R.id.unit_calc_start_task_button);
        final CheckBox saveToDrinkDiaryCheckbox = (CheckBox) rootView.findViewById(R.id.save_to_drink_diary_checkbox);
        if (bundle != null) {
            if (bundle.getBoolean("task_unit_calculator")) {
                confirmButton.setVisibility(View.VISIBLE);
                confirmButton.setBackgroundColor(Color.TRANSPARENT);
            } else if (bundle.getBoolean("drink_diary_unit_calculator")) {
                confirmButton.setImageResource(R.drawable.tick);
                confirmButton.setPadding(10, 10, 10, 10);
                confirmButton.setVisibility(View.VISIBLE);
                bundle.getBoolean("drink_diary_unit_calculator");
            }
            {
                confirmButton.setVisibility(View.VISIBLE);
                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (bundle.getBoolean("task_unit_calculator")) {
                            getFieldEntries();
                            if (checkEntries(true)) {
                                DrinkDiaryEntry drinkDiaryEntry = new DrinkDiaryEntry();
                                drinkDiaryEntry.date = DateAndTime.getDateAndTimeNowForTasks();
                                drinkDiaryEntry.drinkName = drinkName;
                                drinkDiaryEntry.drinkType = drinkType;
                                drinkDiaryEntry.units = unitValueTextView.getText().toString();
                                mListener.unitCalculationComplete(drinkDiaryEntry, saveToDrinkDiary);
                                if (saveToDrinkDiary) {
                                    SharedPreferencesWrapper.saveToPrefs(getActivity(), "lastDrinkAdded", drinkName);
                                    SharedPreferencesWrapper.saveToPrefs(getActivity(), "unitsOfLastDrinkAdded", unitsString);
                                }
                                SharedPreferencesWrapper.saveToPrefs(getActivity(), "task_session_units", unitValueTextView.getText().toString());
                                clearEntryFields();
                                removeUnitCalcEntriesFromSharedPrefs();
                            }
                        } else if (bundle.getBoolean("drink_diary_unit_calculator")) {
                            confirmButton.setImageResource(R.drawable.tick);
                            getFieldEntries();
                            if (checkEntries(true)) {
                                DrinkDiaryEntry drinkDiaryEntry = new DrinkDiaryEntry();
                                drinkDiaryEntry.date = DateAndTime.getDateAndTimeNowForTasks();
                                drinkDiaryEntry.drinkName = drinkName;
                                drinkDiaryEntry.drinkType = drinkType;
                                drinkDiaryEntry.units = unitValueTextView.getText().toString();
                                mListener.unitCalculationComplete(drinkDiaryEntry, true);
                                SharedPreferencesWrapper.saveToPrefs(getActivity(), "lastDrinkAdded", drinkName);
                                SharedPreferencesWrapper.saveToPrefs(getActivity(), "unitsOfLastDrinkAdded", unitsString);
                                clearEntryFields();
                                removeUnitCalcEntriesFromSharedPrefs();
                            }
                        }
                    }
                });
            }
            if (bundle.getBoolean("task_unit_calculator")) {
                saveToDrinkDiaryCheckbox.setVisibility(View.VISIBLE);
                saveToDrinkDiaryCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            saveToDrinkDiary = true;
                        } else if (!isChecked) {
                            saveToDrinkDiary = false;
                        }
                    }
                });
            }
        }

        abvEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!stopCalcUnits && !firstStart) {
                    calculateUnits();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() < 3) {
                    abvEditText.requestFocus();
                }
            }
        });

        volumeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!stopCalcUnits && !firstStart) {
                    calculateUnits();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() < 5) {
                    volumeEditText.requestFocus();
                }

            }
        });

        volumeMeasurementSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!stopCalcUnits && !firstStart) {
                    calculateUnits();
                } else {
                    firstStart = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //don't do anything until something has been changed
                //otherwise is simply default of "ml"
            }
        });

        quantityNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if (!stopCalcUnits && !firstStart) {
                    calculateUnits();
                }
            }
        });
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferencesWrapper.saveToPrefs(getActivity(), "unit_calc_drinkName", drinkNameEditText.getText().toString());
        SharedPreferencesWrapper.saveToPrefs(getActivity(), "unit_calc_abv", abvEditText.getText().toString());
        SharedPreferencesWrapper.saveToPrefs(getActivity(), "unit_calc_drinkVolume", volumeEditText.getText().toString());
        SharedPreferencesWrapper.saveToPrefs(getActivity(), "unit_calc_volPos", volumeMeasurementSpinner.getSelectedItemPosition());
        SharedPreferencesWrapper.saveToPrefs(getActivity(), "unit_calc_typePos", drinkTypeSpinner.getSelectedItemPosition());
        SharedPreferencesWrapper.saveToPrefs(getActivity(), "unit_calc_quantity", quantityNumberPicker.getValue());
        SharedPreferencesWrapper.saveToPrefs(getActivity(), "unit_calc_units", unitValueTextView.getText().toString());
    }

    @Override
    public void onResume() {
        super.onResume();
        drinkNameEditText.setText(SharedPreferencesWrapper.getFromPrefs(getActivity(), "unit_calc_drinkName", ""));
        abvEditText.setText(SharedPreferencesWrapper.getFromPrefs(getActivity(), "unit_calc_abv", ""));
        volumeEditText.setText(SharedPreferencesWrapper.getFromPrefs(getActivity(), "unit_calc_drinkVolume", ""));
        volumeMeasurementSpinner.setSelection(SharedPreferencesWrapper.getFromPrefs(getActivity(), "unit_calc_volPos", 0));
        drinkTypeSpinner.setSelection(SharedPreferencesWrapper.getFromPrefs(getActivity(), "unit_calc_typePos", 0));
        quantityNumberPicker.setValue(SharedPreferencesWrapper.getFromPrefs(getActivity(), "unit_calc_quantity", 1));
        unitValueTextView.setText(SharedPreferencesWrapper.getFromPrefs(getActivity(), "unit_calc_units", "0.0"));
    }

    public void removeUnitCalcEntriesFromSharedPrefs() {
        SharedPreferencesWrapper.removeFromPrefs(getActivity(), "unit_calc_drinkName");
        SharedPreferencesWrapper.removeFromPrefs(getActivity(), "unit_calc_abv");
        SharedPreferencesWrapper.removeFromPrefs(getActivity(), "unit_calc_drinkVolume");
        SharedPreferencesWrapper.removeFromPrefs(getActivity(), "unit_calc_volPos");
        SharedPreferencesWrapper.removeFromPrefs(getActivity(), "unit_calc_typePos");
        SharedPreferencesWrapper.removeFromPrefs(getActivity(), "unit_calc_quantity");
        SharedPreferencesWrapper.removeFromPrefs(getActivity(), "unit_calc_units");
    }


    private void calculateUnits() {
        float aBV;
        float volume;
        Boolean allFieldsEntered = false;

        getFieldEntries();

        if (!firstStart) {
            allFieldsEntered = checkEntries(false);
        } else {
            firstStart = false;
        }

        //if neither are empty perform the calculation (quantity is default 1)
        if (allFieldsEntered) {
            aBV = Float.parseFloat(mABV);
            volume = Float.parseFloat(drinkVolume);
            float unitTotal = UnitCalculator.UnitCalculation(drinkQuantity, drinkVolumeType, volume, aBV);
            unitValueTextView.setText(getString(R.string.unit_calc_units_value, Float.toString(unitTotal)));
        }
    }

    //for overriding in activity/fragments that implement.
    protected void startTaskButtonClicked() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.unit_calculator_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.action_save:
                getFieldEntries();
                if (checkEntries(true)) {
                    Toast.makeText(getActivity(), "Saved to Favourites", Toast.LENGTH_SHORT).show();
                    FavouriteDrinkUtility.saveFavouriteDrinkToStorage(getActivity(), drinkName, drinkType, drinkVolume, drinkVolumeType, String.valueOf(drinkQuantity), unitsString, mABV, String.valueOf(drinkVolumeTypePos), String.valueOf(drinkTypePos));
                }
                return true;
            case R.id.action_example_list:
                ExampleDrinksDialogFragment exampleDialog = new ExampleDrinksDialogFragment();
                exampleDialog.setTargetFragment(this, EXAMPLE_LIST_REQUEST_CODE);
                exampleDialog.show(getActivity().getSupportFragmentManager(), "EXAMPLE_DIALOG");
                return true;
            case R.id.action_clear:
                clearEntryFields();
                Toast.makeText(getActivity(), "Cleared", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_add_favourite:
                UnitCalculatorFavouriteDrinksDialogFragment unitCalculatorFavouriteDrinksDialogFragment = new UnitCalculatorFavouriteDrinksDialogFragment();
                unitCalculatorFavouriteDrinksDialogFragment.setTargetFragment(this, FAVOURITE_LIST_REQUEST_CODE);
                unitCalculatorFavouriteDrinksDialogFragment.show(getActivity().getSupportFragmentManager(), "FAVOURITE_DIALOG");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void clearEntryFields() {
        drinkNameEditText.setError(null);
        abvEditText.setError(null);
        volumeEditText.setError(null);
        stopCalcUnits = true;
        drinkNameEditText.setText("");
        abvEditText.setText("");
        volumeEditText.setText("");
        volumeMeasurementSpinner.setSelection(volMeasurementSpinnerMlLocation);
        quantityNumberPicker.setValue(1);
        drinkTypeSpinner.setSelection(0);
        unitValueTextView.setText("0.0");
        stopCalcUnits = false;
    }

    private Boolean checkEntries(Boolean savingToFavourites) {
        drinkNameEditText.setError(null);

        Boolean cancel = false;
        View focusView = null;

        if (drinkVolume.isEmpty()) {
            volumeEditText.setError(getString(R.string.error_field_required));
            focusView = volumeEditText;
            cancel = true;
        } else if (!drinkVolume.matches(".*[0-9].*")) {
            volumeEditText.setError(getString(R.string.dialog_error_no_units));
            focusView = volumeEditText;
            cancel = true;
        }

        if (mABV.isEmpty()) {
            abvEditText.setError(getString(R.string.error_field_required));
            focusView = abvEditText;
            cancel = true;
        } else if (!mABV.matches(".*[0-9].*")) {
            abvEditText.setError(getString(R.string.dialog_error_no_drink_quantity));
            focusView = abvEditText;
            cancel = true;
        }

        if (savingToFavourites) {
            if (drinkName.isEmpty()) {
                drinkNameEditText.setError(getString(R.string.error_field_required));
                focusView = drinkNameEditText;
                cancel = true;
            } else if (!drinkName.matches(".*[a-zA-Z].*|.*[0-9].*")) {
                drinkNameEditText.setError(getString(R.string.dialog_error_no_drink_name));
                focusView = drinkNameEditText;
                cancel = true;
            }
        }

        if (cancel) {
            // There was an error; don't attempt drink diary entry and focus the first
            // form field with an error.
            focusView.requestFocus();
            return false;
        }
        return true;
    }

    private void getFieldEntries() {
        drinkName = drinkNameEditText.getText().toString();
        mABV = abvEditText.getText().toString();
        drinkVolume = volumeEditText.getText().toString();
        drinkType = drinkTypeSpinner.getSelectedItem().toString();
        drinkTypePos = drinkTypeSpinner.getSelectedItemPosition();
        drinkQuantity = quantityNumberPicker.getValue();
        //volume measurement will always have a selection as an array is loaded up with measurements on start
        //noinspection ConstantConditions
        drinkVolumeType = volumeMeasurementSpinner.getSelectedItem().toString();
        drinkVolumeTypePos = volumeMeasurementSpinner.getSelectedItemPosition();
        unitsString = unitValueTextView.getText().toString();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == EXAMPLE_LIST_REQUEST_CODE) {
            int drinkExample = intent.getExtras().getInt("drinkExample");
            switch (drinkExample) {
                case 0:
                    setBeerExample();
                    break;
                case 1:
                    setBottledBeerExample();
                    break;
                case 2:
                    setCiderExample();
                    break;
                case 3:
                    setWineExample();
                    break;
                case 4:
                    setChampagneExample();
                    break;
                case 5:
                    setSpiritsExample();
                    break;
                case 6:
                    setAlcopopExample();
                    break;
            }
        } else if (requestCode == FAVOURITE_LIST_REQUEST_CODE) {
            if (intent != null) {
                drinkNameEditText.setError(null);
                abvEditText.setError(null);
                volumeEditText.setError(null);
                stopCalcUnits = true;
                String favouriteName = intent.getExtras().getString("drinkName");
                drinkNameEditText.setText(favouriteName);
                abvEditText.setText(intent.getExtras().getString("drinkVolume"));
                volumeEditText.setText(intent.getExtras().getString("drinkABV"));
                volumeMeasurementSpinner.setSelection(intent.getExtras().getInt("drinkVolumeTypePos"));
                quantityNumberPicker.setValue(intent.getExtras().getInt("drinkQuantity"));
                Toast.makeText(getActivity(), favouriteName + "Set", Toast.LENGTH_SHORT).show();
                drinkTypeSpinner.setSelection(intent.getExtras().getInt("drinkTypePos"));
                unitValueTextView.setText(intent.getExtras().getString("drinkUnits"));
                stopCalcUnits = false;
            }
        }
    }

    public void setBeerExample() {
        abvEditText.setError(null);
        volumeEditText.setError(null);
        stopCalcUnits = true;
        abvEditText.setText(Float.toString(UnitCalculator.beerPintABV));
        volumeEditText.setText("1");
        volumeMeasurementSpinner.setSelection(volMeasurementSpinnerPintLocation);
        quantityNumberPicker.setValue(1);
        Toast.makeText(getActivity(), "Beer example set", Toast.LENGTH_SHORT).show();
        calculateUnits();
        stopCalcUnits = false;
        drinkTypeSpinner.setSelection(0);
    }

    private void setBottledBeerExample() {
        abvEditText.setError(null);
        volumeEditText.setError(null);
        stopCalcUnits = true;
        abvEditText.setText(Float.toString(UnitCalculator.beerBottleABV));
        volumeEditText.setText("330");
        volumeMeasurementSpinner.setSelection(volMeasurementSpinnerMlLocation);
        quantityNumberPicker.setValue(1);
        Toast.makeText(getActivity(), "Bottled Beer example set", Toast.LENGTH_SHORT).show();
        calculateUnits();
        stopCalcUnits = false;
        drinkTypeSpinner.setSelection(1);
    }

    private void setCiderExample() {
        abvEditText.setError(null);
        volumeEditText.setError(null);
        stopCalcUnits = true;
        abvEditText.setText(Float.toString(UnitCalculator.ciderABV));
        volumeEditText.setText("1");
        volumeMeasurementSpinner.setSelection(volMeasurementSpinnerPintLocation);
        quantityNumberPicker.setValue(1);
        Toast.makeText(getActivity(), "Cider example set", Toast.LENGTH_SHORT).show();
        calculateUnits();
        stopCalcUnits = false;
        drinkTypeSpinner.setSelection(2);
    }

    private void setWineExample() {
        abvEditText.setError(null);
        volumeEditText.setError(null);
        stopCalcUnits = true;
        abvEditText.setText(Float.toString(UnitCalculator.wineABV));
        volumeEditText.setText(Float.toString(UnitCalculator.wineGlassVolume));
        volumeMeasurementSpinner.setSelection(volMeasurementSpinnerMlLocation);
        quantityNumberPicker.setValue(1);
        Toast.makeText(getActivity(), "Wine example set", Toast.LENGTH_SHORT).show();
        calculateUnits();
        stopCalcUnits = false;
        drinkTypeSpinner.setSelection(3);
    }

    private void setChampagneExample() {
        abvEditText.setError(null);
        volumeEditText.setError(null);
        stopCalcUnits = true;
        abvEditText.setText(Float.toString(UnitCalculator.champagneABV));
        volumeEditText.setText(Float.toString(UnitCalculator.champagneGlassVolume));
        volumeMeasurementSpinner.setSelection(volMeasurementSpinnerMlLocation);
        quantityNumberPicker.setValue(1);
        Toast.makeText(getActivity(), "Champagne example set", Toast.LENGTH_SHORT).show();
        calculateUnits();
        stopCalcUnits = false;
        drinkTypeSpinner.setSelection(4);
    }

    private void setSpiritsExample() {
        abvEditText.setError(null);
        volumeEditText.setError(null);
        stopCalcUnits = true;
        abvEditText.setText(Float.toString(UnitCalculator.spiritsABV));
        volumeEditText.setText(Float.toString(UnitCalculator.spiritGlassVolume));
        volumeMeasurementSpinner.setSelection(volMeasurementSpinnerMlLocation);
        quantityNumberPicker.setValue(1);
        Toast.makeText(getActivity(), "Spirits example set", Toast.LENGTH_SHORT).show();
        calculateUnits();
        stopCalcUnits = false;
        drinkTypeSpinner.setSelection(5);
    }

    private void setAlcopopExample() {
        abvEditText.setError(null);
        volumeEditText.setError(null);
        stopCalcUnits = true;
        abvEditText.setText(Float.toString(UnitCalculator.alcopopABV));
        volumeEditText.setText(Float.toString(UnitCalculator.alcopopBottleVolume));
        volumeMeasurementSpinner.setSelection(volMeasurementSpinnerMlLocation);
        quantityNumberPicker.setValue(1);
        Toast.makeText(getActivity(), "Alcopop example set", Toast.LENGTH_SHORT).show();
        calculateUnits();
        stopCalcUnits = false;
        drinkTypeSpinner.setSelection(6);
    }

    public interface UnitCalculatorListener {
        public void unitCalculationComplete(DrinkDiaryEntry drinkDiaryEntry, Boolean save);
    }
}
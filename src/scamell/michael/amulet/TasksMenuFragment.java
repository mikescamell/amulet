package scamell.michael.amulet;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class TasksMenuFragment extends Fragment {

    private Boolean calibrationMode = false;
    private Menu mMenu;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_task_menu, container, false);

        Button inspectionTaskButton = (Button) rootView.findViewById(R.id.games_fragment_inspection_task_button);
        Button sequenceTaskButton = (Button) rootView.findViewById(R.id.games_fragment_sequence_task_button);

        //set the task menu buttons font to that of the app title
        Typeface coolvetica = Typeface.createFromAsset(getActivity().getAssets(), "fonts/coolvetica.ttf");
        inspectionTaskButton.setTypeface(coolvetica);
        sequenceTaskButton.setTypeface(coolvetica);

        inspectionTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //in case the user has ticked task calibration despite being their first time
                // reset the task calibration check
                // box to false
                MenuItem taskCalibration = mMenu.findItem(R.id.action_task_calibration);
                taskCalibration.setChecked(false);
                getActivity().invalidateOptionsMenu();
                //if a fresh login and new register, assume user has never played, show instructions and force a
                // calibration to take place
                if (SharedPreferencesWrapper.getFromPrefs(getActivity(), "new_user", false)) {
                    if (SharedPreferencesWrapper.getFromPrefs(getActivity(), "first_time_inspection_task", false)) {
                        Intent intent = new Intent(getActivity(), TaskInstructionsActivity.class);
                        intent.putExtra("task_type", "inspection");
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(getActivity(), InspectionTaskActivity.class);
                        intent.putExtra("calibration_mode", calibrationMode);
                        startActivity(intent);
                    }
                } else {
                    Intent intent = new Intent(getActivity(), InspectionTaskActivity.class);
                    intent.putExtra("calibration_mode", calibrationMode);
                    startActivity(intent);
                }
                calibrationMode = false;
            }
        });

        sequenceTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MenuItem taskCalibration = mMenu.findItem(R.id.action_task_calibration);
                taskCalibration.setChecked(false);
                getActivity().invalidateOptionsMenu();
                if (SharedPreferencesWrapper.getFromPrefs(getActivity(), "new_user", false)) {
                    if (SharedPreferencesWrapper.getFromPrefs(getActivity(), "first_time_sequence_task", false)) {
                        Intent intent = new Intent(getActivity(), TaskInstructionsActivity.class);
                        intent.putExtra("task_type", "sequence");
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(getActivity(), SequenceTaskActivity.class);
                        intent.putExtra("calibration_mode", calibrationMode);
                        startActivity(intent);
                    }
                } else {
                    Intent intent = new Intent(getActivity(), SequenceTaskActivity.class);
                    intent.putExtra("calibration_mode", calibrationMode);
                    startActivity(intent);
                }
                calibrationMode = false;
            }
        });

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //ensure task calibration is set to false always on startup
        MenuItem taskCalibration = menu.findItem(R.id.action_task_calibration);
        taskCalibration.setChecked(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.task_menu, menu);
        mMenu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case R.id.action_task_calibration:
                if (item.isChecked()) {
                    item.setChecked(false);
                    calibrationMode = false;
                    Toast.makeText(getActivity(), "Calibration Mode Disabled", Toast.LENGTH_SHORT).show();
                } else {
                    item.setChecked(true);
                    calibrationMode = true;
                    Toast.makeText(getActivity(), "Calibration Mode Enabled", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
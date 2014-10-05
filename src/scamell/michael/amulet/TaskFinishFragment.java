package scamell.michael.amulet;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TaskFinishFragment extends Fragment {

    private TextView speedTextView;
    private TextView lastSpeedTextView;
    private TextView baselineTextView;
    private TextView baselineComparisonTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task_finish, container, false);
        //registers a touch event and closes the activity and fragments
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("DEBUG", "Touch Registered");
                getActivity().finish();
                return true;
            }
        });

        Bundle bundle = getArguments();

        String taskType = bundle.getString("task_type");

        speedTextView = (TextView) rootView.findViewById(R.id.fragment_task_finish_speed);
        lastSpeedTextView = (TextView) rootView.findViewById(R.id.fragment_last_task_finish_speed);
        baselineTextView = (TextView) rootView.findViewById(R.id.fragment_task_finish_baseline);
        TextView calibrationTextView = (TextView) rootView.findViewById(R.id.fragment_task_finish_calibration_confirmation);
        baselineComparisonTextView = (TextView) rootView.findViewById(R.id.fragment_task_finish_speed_comparison);
        Boolean calibrationMode = bundle.getBoolean("calibration_mode");

        if (taskType != null) {
            if (taskType.equals("inspection")) {
                if (calibrationMode) {
                    //ensures user does a calibration and can't exit early and start a new game
                    // without doing one
                    SharedPreferencesWrapper.saveToPrefs(getActivity(), "first_time_inspection_task", false);
                    calibrationTextView.setVisibility(View.VISIBLE);
                }
                loadInspectionTaskFinish(bundle, calibrationMode);
            } else if (taskType.equals("sequence")) {
                if (calibrationMode) {
                    //ensures user does a calibration and can't exit early and start a new game
                    // without doing one
                    SharedPreferencesWrapper.saveToPrefs(getActivity(), "first_time_sequence_task", false);
                    calibrationTextView.setVisibility(View.VISIBLE);
                }
                loadSequenceTaskFinish(bundle, calibrationMode);
            }
        }

        if (SharedPreferencesWrapper.getFromPrefs(getActivity(), "new_user", false)) {
            if (!SharedPreferencesWrapper.getFromPrefs(getActivity(), "first_time_sequence_task", false) && !SharedPreferencesWrapper.getFromPrefs(getActivity(), "first_time_inspection_task", false)) {
                SharedPreferencesWrapper.saveToPrefs(getActivity(), "new_user", false);
            }
        }

        return rootView;
    }

    private void loadInspectionTaskFinish(Bundle bundle, Boolean calibrationMode) {
        int speed = bundle.getInt("speed");
        speedTextView.setText(getString(R.string.fragment_speed_string_int, speed));

        int lastSpeed = bundle.getInt("last_speed");
        if (lastSpeed == 0) {
            lastSpeedTextView.setVisibility(View.INVISIBLE);
        } else {
            lastSpeedTextView.setText(getString(R.string.fragment_last_speed_string_int, lastSpeed));
        }

        int baseline = SharedPreferencesWrapper.getFromPrefs(getActivity(), "calibration_time_inspection_task", 0);
        if (baseline != 0 && !calibrationMode) {
            baselineTextView.setText(getString(R.string.fragment_task_finish_baseline_message_int, baseline));
        } else {
            baselineTextView.setVisibility(View.INVISIBLE);
        }

        int difference = baseline - speed;
        if (!calibrationMode) {
            if (difference < 0) {
                // if it's less than trim the "-" to give them how much quicker they were
                int diffString = Math.abs(difference);
                baselineComparisonTextView.setText(getString(R.string.fragment_task_finish_slower_than_baseline, String.valueOf(diffString), "milliseconds"));
            } else if (difference > 0) {
                //otherwise they were slower so just use the difference result
                baselineComparisonTextView.setText(getString(R.string.fragment_task_finish_faster_than_baseline, String.valueOf(difference), "milliseconds"));
            } else {
                baselineComparisonTextView.setText(getString(R.string.fragment_task_finish_same_as_baseline));
            }
        } else {
            baselineComparisonTextView.setVisibility(View.INVISIBLE);
        }

        //save this for the home page update. Saved here as at this point we know the game has been
        //completed and we have figures for everything
        SharedPreferencesWrapper.saveToPrefs(getActivity(), "last_task_played", "Inspection Task");
    }

    private void loadSequenceTaskFinish(Bundle bundle, Boolean calibrationMode) {
        String speed = String.valueOf(bundle.getFloat("time_taken"));
        speedTextView.setText(getString(R.string.fragment_speed_string_string, speed));

        String lastSpeed = String.valueOf(bundle.getFloat("last_time"));
        if (lastSpeed.equals("0.0")) {
            lastSpeedTextView.setVisibility(View.INVISIBLE);
        } else {
            lastSpeedTextView.setText(getString(R.string.fragment_last_speed_string_string, lastSpeed));
        }

        //sequence task calibration is saved as a string not float
        String baseline = SharedPreferencesWrapper.getFromPrefs(getActivity(), "calibration_time_sequence_task", "0");
        if (!baseline.equals("0") && !calibrationMode) {
            baselineTextView.setText(getString(R.string.fragment_task_finish_baseline_message_string, baseline));
        } else {
            baselineTextView.setVisibility(View.INVISIBLE);
        }

        float difference = Float.valueOf(baseline) - Float.valueOf(speed);
        if (!calibrationMode) {
            if (difference < 0) {
                Float diffString = Math.abs(difference);
                baselineComparisonTextView.setText(getString(R.string.fragment_task_finish_slower_than_baseline, String.valueOf(diffString), "seconds"));
            } else if (difference > 0) {
                baselineComparisonTextView.setText(getString(R.string.fragment_task_finish_faster_than_baseline, String.valueOf(difference), "seconds"));
            } else {
                baselineComparisonTextView.setText(getString(R.string.fragment_task_finish_same_as_baseline));
            }
        } else {
            baselineComparisonTextView.setVisibility(View.INVISIBLE);
        }
        SharedPreferencesWrapper.saveToPrefs(getActivity(), "last_task_played", "Sequence Task");
    }

}
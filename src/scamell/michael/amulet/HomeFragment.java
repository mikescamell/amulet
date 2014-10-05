package scamell.michael.amulet;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;


/**
 * -setting custom font
 * https://www.youtube.com/watch?v=2wBjpn7VmsU
 * -using colours for different letters
 * http://stackoverflow.com/questions/7221930/change-text-color-of-one-word-in-a-textview
 */
public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        Bundle args = getArguments();
        Boolean playAnimation = false;
        if (args != null) {
            playAnimation = args.getBoolean("play_animation");
        }
        if (playAnimation) {
            Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
            rootView.setAnimation(fadeIn);
        }

        TextView amuletTextView = (TextView) rootView.findViewById(R.id.fragment_home_amulet_textView);
        //set the a in amulet to blue
        String mulet = "mulet";
        String a = "<font color='#3F7CFF'>A</font>";
        amuletTextView.setText(Html.fromHtml(a + mulet));

        TextView lastTaskPlayedTextView = (TextView) rootView.findViewById(R.id.fragment_home_task_played_textView);
        String lastTaskPlayed = SharedPreferencesWrapper.getFromPrefs(getActivity(), "last_task_played", "No tasks played");
        if (!lastTaskPlayed.equals("NO_TASK_PLAYED")) {
            lastTaskPlayedTextView.setText(lastTaskPlayed);
        }

        TextView lastTaskTimeTextView = (TextView) rootView.findViewById(R.id.fragment_home_task_score_textView);
        if (lastTaskPlayed.equals("Inspection Task")) {
            int lastInspectionTime = SharedPreferencesWrapper.getFromPrefs(getActivity(), "last_inspection_task_time", 0);
            if (lastInspectionTime != 0) {
                lastTaskTimeTextView.setText(getString(R.string.fragment_home_score_milliseconds, lastInspectionTime));
            }
        } else if (lastTaskPlayed.equals("Sequence Task")) {
            float lastSequenceTime = SharedPreferencesWrapper.getFromPrefs(getActivity(), "last_sequence_task_time", 0.0f);
            if (lastSequenceTime != 0.0f) {
                lastTaskTimeTextView.setText(getString(R.string.fragment_home_score_seconds, String.valueOf(lastSequenceTime)));
            }
        } else {
            lastTaskTimeTextView.setText("No task time");
        }

        TextView dateTextView = (TextView) rootView.findViewById(R.id.fragment_home_date_textView);
        dateTextView.setText(DateAndTime.getDateAndTime(getActivity()));

        TextView lastDrinkTextView = (TextView) rootView.findViewById(R.id.fragment_home_login_last_drink_textView);
        String lastDrink = SharedPreferencesWrapper.getFromPrefs(getActivity(), "lastDrinkAdded", "No drink added");
        lastDrinkTextView.setText(lastDrink);

        TextView lastDrinkUnitsTextView = (TextView) rootView.findViewById(R.id.fragment_home_units_value_textView);
        String lastDrinkUnits = SharedPreferencesWrapper.getFromPrefs(getActivity(), "unitsOfLastDrinkAdded", "No");
        lastDrinkUnitsTextView.setText(getString(R.string.fragment_home_units_consumed_string_param, lastDrinkUnits));

        TextView lastTaskUnitsTextView = (TextView) rootView.findViewById(R.id.fragment_home_last_task_units_textView);
        String lastTaskUnitsString = SharedPreferencesWrapper.getFromPrefs(getActivity(), "task_session_units", "No");
        lastTaskUnitsTextView.setText(getString(R.string.fragment_home_units_consumed_string_param, lastTaskUnitsString));

        rootView.setFocusableInTouchMode(true);
        rootView.requestFocus();
        rootView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    SharedPreferencesWrapper.saveToPrefs(getActivity(), "last_date_played", DateAndTime.getDateAndTimeNowForTasks());
                    getActivity().finish();
                    return true;
                }
                return false;
            }
        });

        return rootView;
    }
}
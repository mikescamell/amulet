package scamell.michael.amulet;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AccountDetailsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_account_details, container, false);

        TextView mName = (TextView) rootView.findViewById(R.id.account_details_name_textview);
        String mFirstName = SharedPreferencesWrapper.getFromPrefs(getActivity(), "first_name", "First Name");
        String mLastName = SharedPreferencesWrapper.getFromPrefs(getActivity(), "last_name", "Last Name");
        mName.setText(mFirstName + " " + mLastName);

        TextView mEmail = (TextView) rootView.findViewById(R.id.account_details_email_textview);
        mEmail.setText(SharedPreferencesWrapper.getFromPrefs(getActivity(), "email", "Email"));

        TextView mPassword = (TextView) rootView.findViewById(R.id.account_details_password_textview);
        mPassword.setText(SharedPreferencesWrapper.getFromPrefs(getActivity(), "password", "Password"));

        TextView mInspectionTaskBaseline = (TextView) rootView.findViewById(R.id.account_details_inspection_task_baseline);
        int inspectionBaseline = SharedPreferencesWrapper.getFromPrefs(getActivity(), "calibration_time_inspection_task", 0);
        if (inspectionBaseline != 0) {
            mInspectionTaskBaseline.setText(getString(R.string.accountDetails_inspection_task_time_value, inspectionBaseline));
        }

        TextView mSequenceTaskBaseline = (TextView) rootView.findViewById(R.id.account_details_sequence_task_value_textView);
        String sequenceBaseline = SharedPreferencesWrapper.getFromPrefs(getActivity(), "calibration_time_sequence_task", "0");
        if (!sequenceBaseline.equals("0")) {
            mSequenceTaskBaseline.setText(getString(R.string.accountDetails_sequence_task_time_value, sequenceBaseline));
        }

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.account_details, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_password:
                DialogFragment changePasswordFragment = new ChangePasswordDialogFragment();
                changePasswordFragment.show(getFragmentManager(), "DIALOG_CHANGE_PASSWORD");
                return true;
            case R.id.action_about:
                DialogFragment aboutDialogFragment = new AboutDialogFragment();
                aboutDialogFragment.show(getFragmentManager(), "DIALOG_ABOUT");
//                aboutDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
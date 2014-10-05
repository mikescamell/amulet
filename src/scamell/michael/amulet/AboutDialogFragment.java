package scamell.michael.amulet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

public class AboutDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(inflater.inflate(R.layout.about_dialog, null));
        builder.setTitle(R.string.action_about);
        builder.setMessage(getString(R.string.dialog_about_developer_message) +
                getString(R.string.dialog_about_attribution_message));
        return builder.create();
    }
}
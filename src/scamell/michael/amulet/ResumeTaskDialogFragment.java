package scamell.michael.amulet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public abstract class ResumeTaskDialogFragment extends DialogFragment {

    private final String message;
    private final String posString;
    private final String negString;
    private final String neuString;

    public ResumeTaskDialogFragment(String message, String posString, String negString, String neuString) {
        this.message = message;
        this.posString = posString;
        this.negString = negString;
        this.neuString = neuString;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setTitle("Task Paused")
                .setPositiveButton(posString, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        posButtonClicked();
                    }
                })
                .setNeutralButton(neuString, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        neutralButtonClicked();
                    }

                })
                .setNegativeButton(negString, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        negButtonClicked();
                    }
                });
        return builder.create();
    }

    public abstract void posButtonClicked();

    public abstract void negButtonClicked();

    public abstract void neutralButtonClicked();
}
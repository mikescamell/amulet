package scamell.michael.amulet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

//http://developer.android.com/guide/topics/ui/dialogs.html
public class ExampleDrinksDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Typical Drinks")
                .setItems(R.array.drink_type_array, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.putExtra("drinkExample", which);
                        getTargetFragment().onActivityResult(getTargetRequestCode(), 1234, intent);
                    }
                });
        return builder.create();
    }

}
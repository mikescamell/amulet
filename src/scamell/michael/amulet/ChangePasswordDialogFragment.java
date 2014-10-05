package scamell.michael.amulet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ChangePasswordDialogFragment extends DialogFragment implements OnRetrieveHttpData {

    private final int passwordLength = 8;
    private EditText currentPassword;
    private EditText passwordOne;
    private EditText passwordTwo;
    private String newPassword;
    private Boolean correctCurrentPassword = false;
    private AlertDialog alertDialog = null;

    public ChangePasswordDialogFragment() {
        //default constructor
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final LayoutInflater inflater = getActivity().getLayoutInflater();

        alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_change_password_title)
                .setView(inflater.inflate(R.layout.dialog_change_password, null))
                .setMessage(R.string.dialog_change_password_message)
                .setPositiveButton(R.string.dialog_change_password_ok, null)// {
                .setNegativeButton(R.string.dialog_change_password_cancel, null)// {
                .create();
        alertDialog.show();

        try {
            //noinspection ConstantConditions
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currentPassword = (EditText) alertDialog.findViewById(R.id.dialog_change_password_original_password_editText);
                    passwordOne = (EditText) alertDialog.findViewById(R.id.dialog_change_password_new_password1);
                    passwordTwo = (EditText) alertDialog.findViewById(R.id.dialog_change_password_new_password2);

                    View focusView = null;
                    boolean cancel = false;

                    String currentPasswordString = currentPassword.getEditableText().toString();
                    correctCurrentPassword = checkOldPassword(currentPasswordString);

                    if (correctCurrentPassword) {
                        String p1 = passwordOne.getEditableText().toString();
                        String p2 = passwordTwo.getEditableText().toString();

                        if (!p2.equals(p1)) {
                            passwordTwo.setError("Password doesn't match");
                            focusView = passwordTwo;
                            cancel = true;
                        }

                        if (TextUtils.isEmpty(p2)) {
                            passwordTwo.setError(getString(R.string.error_field_required));
                            focusView = passwordOne;
                            cancel = true;
                        } else if (p2.length() < passwordLength) {
                            passwordTwo.setError(getString(R.string.error_invalid_password));
                            focusView = passwordTwo;
                            cancel = true;
                        }

                        if (TextUtils.isEmpty(p1)) {
                            passwordOne.setError(getString(R.string.error_field_required));
                            focusView = passwordOne;
                            cancel = true;
                        } else if (p1.length() < passwordLength) {
                            passwordOne.setError(getString(R.string.error_invalid_password));
                            focusView = passwordOne;
                            cancel = true;
                        }

                        if (cancel) {
                            // There was an error; don't attempt login and focus the first
                            // form field with an error.
                            focusView.requestFocus();
                            Toast.makeText(getActivity(), getString(R.string.dialog_change_password_reenter_password_message), Toast.LENGTH_SHORT).show();
                        } else {
                            //if passwords match commit to save to storage
                            newPassword = p1;
                            changeServerPassword();
                        }
                    } else {
                        currentPassword.setError(getString(R.string.error_reenter_password));
                        focusView = currentPassword;
                        focusView.requestFocus();
                        Toast.makeText(getActivity(), getString(R.string.dialog_change_password_current_password_wrong), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (NullPointerException e) {
            Toast.makeText(getActivity(), "Password changed failed. Please ", Toast.LENGTH_SHORT).show();
            Log.e("NULL_POINTER", "Password Changed failed");
        }
        return alertDialog;
    }

    private void changeServerPassword() {
        String mUsername = SharedPreferencesWrapper.getFromPrefs(getActivity(), "email", "NO_EMAIL");
        String mOldPassword = SharedPreferencesWrapper.getFromPrefs(getActivity(), "password", "NO_PASSWORD");
        RetrieveHTTPDataAsync changePassword = new RetrieveHTTPDataAsync(this);
        changePassword.execute("http://08309.net.dcs.hull.ac.uk/api/admin/change?" +
                "username=" + mUsername + "&oldpassword=" + mOldPassword + "&newpassword=" + newPassword);
    }

    private Boolean checkOldPassword(String password) {
        String mOldPassword = SharedPreferencesWrapper.getFromPrefs(getActivity(), "password", "NO_PASSWORD");
        return password.equals(mOldPassword);
    }

    @Override
    public void onRetrieveTaskCompleted(String httpData) {
        Log.i("Change_Password_Server Response", httpData);
        handleResponse(httpData);
    }

    private void handleResponse(String httpData) {
        if (httpData.equals("ERROR_CONTACTING_SERVER")) {
            Toast.makeText(getActivity(), "Password change failed. Please check your internet connection", Toast.LENGTH_SHORT).show();
        } else if (httpData.equals("SERVER_ERROR_RESPONSE")) {
            Toast.makeText(getActivity(), "There was an error. Please try again later", Toast.LENGTH_SHORT).show();
        } else {
            //if successful and passwords match save to storage
            alertDialog.dismiss();
            SharedPreferencesWrapper.saveToPrefs(getActivity(), "password", newPassword);
            Toast.makeText(getActivity(), getString(R.string.toast_password_change_successful), Toast.LENGTH_SHORT).show();
        }
    }

}
/**
 * Based off the Login Template offered for use, free from Google Inc under Creative Commons Licence 2.5.
 *
 * http://developer.android.com/tools/projects/templates.html
 *
 * Makes registering process better by using Async task as well as providing a good user experience.
 * e.g. the loading screen, the password and email error setters.
 */

package scamell.michael.amulet;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity which displays a register screen to the user, offering registration as
 * well.
 */
public class RegisterActivity extends Activity implements OnRetrieveHttpData {

    private final int passwordLength = 8;
    //RegEx to check names
    //      source: http://stackoverflow.com/questions/15805555/java-regex-to-validate-full-name-allow-only-spaces-and-letters
    private final String namerRegEx = "^[\\p{L}.'-]+$";
    private final String emailRegEx = "[-0-9a-zA-Z.+_]+@[-0-9a-zA-Z.+_]+\\.[A-Za-z]{2,4}";
    /**
     * Keep track of the register task to ensure we can cancel it if requested.
     */
    private UserRegisterTask mAuthTask = null;
    // Values for email and password at the time of the register attempt.
    private String mFirstName;
    private String mLastName;
    private String mEmail;
    private String mPassword;
    private String mPassword2;
    private Boolean alreadyRegistered = false;
    // UI references.
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mPassword2View;
    private View mRegisterFormView;
    private View mRegisterStatusView;
    private TextView mRegisterStatusMessageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Set up the register form.
        mFirstNameView = (EditText) findViewById(R.id.first_name);
        mLastNameView = (EditText) findViewById(R.id.last_name);

        mEmailView = (EditText) findViewById(R.id.register_email);

        mPasswordView = (EditText) findViewById(R.id.register_password);
        mPassword2View = (EditText) findViewById(R.id.register_password2);

        mRegisterFormView = findViewById(R.id.register_form);
        mRegisterStatusView = findViewById(R.id.register_status);
        mRegisterStatusMessageView = (TextView) findViewById(R.id.register_status_message);

        findViewById(R.id.sign_in_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        attemptRegister();
                    }
                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    /**
     * Attempts to sign in or register the account specified by the register form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual register attempt is made.
     */
    public void attemptRegister() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mFirstNameView.setError(null);
        mLastNameView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mPassword2View.setError(null);

        // Store values at the time of the register attempt.
        mFirstName = mFirstNameView.getText().toString();
        mLastName = mLastNameView.getText().toString();
        mEmail = mEmailView.getText().toString();
        mPassword = mPasswordView.getText().toString();
        mPassword2 = mPassword2View.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!mPassword2.equals(mPassword)) {
            mPassword2View.setError("Passwords don't match");
            focusView = mPassword2View;
            focusView.requestFocus();
            cancel = true;
        }

        // Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (mPassword.length() < passwordLength) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(mEmail)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!mEmail.matches(emailRegEx)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        // Check for a valid last name.
        if (TextUtils.isEmpty(mLastName)) {
            mLastNameView.setError(getString(R.string.error_field_required));
            focusView = mLastNameView;
            cancel = true;
        } else if (!mLastName.matches(namerRegEx)) {
            mLastNameView.setError(getString(R.string.error_invalid_last_name));
            focusView = mLastNameView;
            cancel = true;
        }

        // Check for a valid first name.
        if (TextUtils.isEmpty(mFirstName)) {
            mFirstNameView.setError(getString(R.string.error_field_required));
            focusView = mFirstNameView;
            cancel = true;
        } else if (!mFirstName.matches(namerRegEx)) {
            mFirstNameView.setError(getString(R.string.error_invalid_first_name));
            focusView = mFirstNameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt register and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user register attempt.
            mRegisterStatusMessageView.setText(R.string.register_progress_signing_in);
            showProgress(true);
            mAuthTask = new UserRegisterTask();
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the register form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade_out_and_scale_up-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            mRegisterStatusView.setVisibility(View.VISIBLE);
            mRegisterStatusView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mRegisterStatusView.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });

            mRegisterFormView.setVisibility(View.VISIBLE);
            mRegisterFormView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mRegisterFormView.setVisibility(show ? View.GONE
                                    : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mRegisterStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mRegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private String RegisterUser(String firstName, String lastName, String email, String password) {
        RetrieveHTTPData registerUser = new RetrieveHTTPData(this);
        return registerUser.getHTTPData("http://08309.net.dcs.hull.ac.uk/api/admin/register?firstname=" + firstName + "&Surname="
                + lastName + "&username=" + email + "&password=" + password);
    }

    @Override
    public void onRetrieveTaskCompleted(String httpData) {
        Log.i("Server Response", httpData);
    }

    /**
     * Represents an asynchronous register/registration task used to authenticate
     * the user.
     */
    private class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            String registerStatus = RegisterUser(mFirstName, mLastName, mEmail, mPassword);
            if (registerStatus.equals("SERVER_ERROR_RESPONSE") || registerStatus.equals("ERROR_CONTACTING_SERVER")) {
                return false;
            } else if (registerStatus.contains("User already exists")) {
                alreadyRegistered = true;
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                LoginActivity.getInstance().finish();
                Toast.makeText(getApplicationContext(), "Registered succesfully and logged in", Toast.LENGTH_LONG).show();
                SharedPreferencesWrapper.saveToPrefs(RegisterActivity.this, "first_name", mFirstName);
                SharedPreferencesWrapper.saveToPrefs(RegisterActivity.this, "last_name", mLastName);
                SharedPreferencesWrapper.saveToPrefs(RegisterActivity.this, "email", mEmail);
                SharedPreferencesWrapper.saveToPrefs(RegisterActivity.this, "password", mPassword);
                SharedPreferencesWrapper.saveToPrefs(RegisterActivity.this, "logged_in", true);
                SharedPreferencesWrapper.saveToPrefs(RegisterActivity.this, "new_login", true);
                SharedPreferencesWrapper.saveToPrefs(RegisterActivity.this, "new_user", true);
                SharedPreferencesWrapper.saveToPrefs(RegisterActivity.this, "first_time_inspection_task", true);
                SharedPreferencesWrapper.saveToPrefs(RegisterActivity.this, "first_time_sequence_task", true);
                SharedPreferencesWrapper.saveToPrefs(RegisterActivity.this, "first_home_start", true);
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                if (alreadyRegistered) {
                    Toast.makeText(getApplicationContext(), "This username has already been registered, please enter a new username or login", Toast.LENGTH_LONG).show();
                    mEmailView.setError(getString(R.string.error_registered_email));
                    mEmailView.requestFocus();
                    alreadyRegistered = false;
                    //Doesn't work currently due to exception when there's no network, need to sort this out.
                } else {
                    Toast.makeText(getApplicationContext(), "Sorry we couldn't register you, please try again later", Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

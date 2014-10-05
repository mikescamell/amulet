/**
 * Based off the Login Template offered for use, free from Google Inc under Creative Commons Licence 2.5.
 *
 * http://developer.android.com/tools/projects/templates.html
 *
 * Makes login process more better by using Async task as well as providing a good user experience.
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
import android.text.Html;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity implements OnRetrieveHttpData {

    private static LoginActivity loginActivityToKill;
    private final int passwordLength = 8;
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    // Values for email and password at the time of the login attempt.
    private String mEmail;
    private String mPassword;
    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mLoginFormView;
    private View mLoginStatusView;
    private TextView mLoginStatusMessageView;
    private Boolean userOrPasswordUnknown = false;

    //when using register need to kill login page so back button press doesn't take you back to login
    //http://stackoverflow.com/questions/14355731/killing-one-activity-from-another
    public static LoginActivity getInstance() {
        return loginActivityToKill;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //if the user hasn't signed in already logged in
        if (!SharedPreferencesWrapper.getFromPrefs(LoginActivity.this, "logged_in", false)) {
            setContentView(R.layout.activity_login);

            loginActivityToKill = this;

            setVolumeControlStream(AudioManager.STREAM_MUSIC);

            TextView amuletTextView = (TextView) findViewById(R.id.activity_login_amulet_textview);
            //set the a in amulet to blue
            String mulet = "mulet";
            String a = "<font color='#3F7CFF'>A</font>";
            amuletTextView.setText(Html.fromHtml(a + mulet));

            // Set up the login form.
            mEmailView = (EditText) findViewById(R.id.login_email);
            String previousEmail = SharedPreferencesWrapper.getFromPrefs(LoginActivity.this, "email", "null");
            if (!previousEmail.equals("null")) {
                mEmailView.setText(previousEmail);
            }

            mPasswordView = (EditText) findViewById(R.id.login_password);
            mPasswordView
                    .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView textView, int id,
                                                      KeyEvent keyEvent) {
                            if (id == R.id.login || id == EditorInfo.IME_NULL) {
                                attemptLogin();
                                return true;
                            }
                            return false;
                        }
                    });

            mLoginFormView = findViewById(R.id.login_form);
            mLoginStatusView = findViewById(R.id.login_status);
            mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

            findViewById(R.id.sign_in_button).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            attemptLogin();
                        }
                    }
            );

            //makes the word "register" in the register option text start the register activity
            SpannableString ss = new SpannableString(getString(R.string.login_screen_register_option));
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                }
            };
            ss.setSpan(clickableSpan, 16, 24, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            TextView loginScreenRegisterOption = (TextView) findViewById(R.id.login_screen_register_option_text);
            loginScreenRegisterOption.setText(ss);
            loginScreenRegisterOption.setMovementMethod(LinkMovementMethod.getInstance());
            //the user must have already logged in and not logged out so launch the HomeActivity
        } else {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        mEmail = mEmailView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

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
        } else if (!mEmail.contains("@")) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);
            mAuthTask = new UserLoginTask();
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade_out_and_scale_up-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            mLoginStatusView.setVisibility(View.VISIBLE);
            mLoginStatusView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginStatusView.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });

            mLoginFormView.setVisibility(View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mLoginFormView.setVisibility(show ? View.GONE
                                    : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private String LoginUser(String email, String password) {
        RetrieveHTTPData loginUser = new RetrieveHTTPData(this);
        return loginUser.getHTTPData("http://08309.net.dcs.hull.ac.uk/api/admin/details?username=" + email + "&password=" + password);
    }

    @Override
    public void onRetrieveTaskCompleted(String httpData) {
        Log.i("Server Response", httpData);
    }

    public void onCheckboxClicked(View view) {

        boolean checked = ((CheckBox) view).isChecked();

        switch (view.getId()) {
            case R.id.login_show_password_checkbox:
                if (checked)
                    mPasswordView.setInputType(InputType.TYPE_CLASS_TEXT);
                else
                    mPasswordView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                mPasswordView.setSelection(mPasswordView.getText().length());
                break;
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    private class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            String loginStatus = LoginUser(mEmail, mPassword);
            if (loginStatus.equals("SERVER_ERROR_RESPONSE") || loginStatus.equals("ERROR_CONTACTING_SERVER")) {
                return false;
            } else if (loginStatus.contains("User or password unknown")) {
                userOrPasswordUnknown = true;
                return false;
            }

            //otherwise user exists and password is correct
            //save their names, email and password to local storage for future use
            int spacePos = loginStatus.indexOf(":");
            String mFullName = loginStatus.substring(spacePos + 2, loginStatus.length() - 2);
            String[] nameArray = mFullName.split("\\s+");
            // TODO: what happens if it's Jean Luc Pickard???
            SharedPreferencesWrapper.saveToPrefs(LoginActivity.this, "first_name", nameArray[0].trim());
            SharedPreferencesWrapper.saveToPrefs(LoginActivity.this, "last_name", nameArray[1].trim());
            // use trim to ensure that the user is not affected by an auto added space
            SharedPreferencesWrapper.saveToPrefs(LoginActivity.this, "email", mEmail.trim());
            SharedPreferencesWrapper.saveToPrefs(LoginActivity.this, "password", mPassword);
            SharedPreferencesWrapper.saveToPrefs(LoginActivity.this, "logged_in", true);
            SharedPreferencesWrapper.saveToPrefs(LoginActivity.this, "new_login", true);
            SharedPreferencesWrapper.saveToPrefs(LoginActivity.this, "first_time_inspection_task", true);
            SharedPreferencesWrapper.saveToPrefs(LoginActivity.this, "first_time_sequence_task", true);
            SharedPreferencesWrapper.saveToPrefs(LoginActivity.this, "first_home_start", true);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                //if login was successful
                Toast.makeText(getApplicationContext(), "You're now logged in", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                if (userOrPasswordUnknown) {
                    Toast.makeText(getApplicationContext(), "Username or Password Wrong Please Try Again", Toast.LENGTH_LONG).show();
                    mEmailView.setError(getString(R.string.error_login_username));
                    mEmailView.requestFocus();
                    mPasswordView.setError(getString(R.string.error_login_password));
                    userOrPasswordUnknown = false;
                } else {
                    Toast.makeText(getApplicationContext(), "Can't Connect to Server, Try Again Later", Toast.LENGTH_LONG).show();
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

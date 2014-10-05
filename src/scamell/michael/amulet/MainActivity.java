package scamell.michael.amulet;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends FragmentActivity implements OnRetrieveHttpData, UnitCalculatorFragment.UnitCalculatorListener {

    private String[] mDrawerOptions;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String fragmentName;
    private View loadingView;
    private Handler handler;
    private MediaPlayer intro;

    public MainActivity() {
        //default constructor
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        //http://developer.android.com/training/managing-audio/volume-playback.html
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        intro = MediaPlayer.create(this, R.raw.intro);

        loadingView = findViewById(R.id.loading_status);

        mDrawerOptions = getResources().getStringArray(R.array.drawer_menu_items);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.list_item_drawer, mDrawerOptions));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerTitle = getTitle();
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        if (savedInstanceState == null) {
            setTitle("Home");
            mTitle = "Home";
        }
        if (savedInstanceState != null) {
            if (!savedInstanceState.getBoolean("isDrawerOpen")) {
                mTitle = savedInstanceState.getCharSequence("title");
                setTitle(mTitle);
            }
        }

        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        int count = getSupportFragmentManager().getBackStackEntryCount();
                        if (count != 0) {
                            FragmentManager.BackStackEntry fragment = getSupportFragmentManager().getBackStackEntryAt(count - 1);
                            String name = fragment.getName();
                            if (!String.valueOf(mTitle).toLowerCase().equals(name)) {
                                setTitle(name);
                            }
                        } else {
                            setTitle("Home");
                        }
                    }
                }
        );

        boolean firstHomeStart = SharedPreferencesWrapper.getFromPrefs(this, "first_home_start", false);
        if (!firstHomeStart) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            //load the home fragment if there are no other fragments(in case activity has been restarted)
            List<Fragment> fragmentList = fragmentManager.getFragments();
            if (fragmentList == null) {
                playIntroMusic();
                Bundle args = new Bundle();
                args.putBoolean("play_animation", true);
                HomeFragment homeFragment = new HomeFragment();
                homeFragment.setArguments(args);
                fragmentManager.beginTransaction().replace(R.id.content_frame, homeFragment, "home").commit();
            }
        } else {
            loadingScreen(true);
        }

        //if it's a new login then try to get the drink diary and task history from the web and save locally
        if (SharedPreferencesWrapper.getFromPrefs(this, "new_login", false)) {
            RetrieveTaskDataFromServer retrieveTaskDataFromServer = new RetrieveTaskDataFromServer(this);
            retrieveTaskDataFromServer.retrieveInspectionTaskEntries();
            retrieveTaskDataFromServer.retrieveSequenceTaskEntries();
            retrieveDrinkDiaryEntries();
            SharedPreferencesWrapper.saveToPrefs(this, "new_login", false);
        } else if (SharedPreferencesWrapper.getFromPrefs(this, "reUpload_tasks", false)) {
            TaskEntries taskEntries = TaskEntries.createTaskEntriesFromStorage(this, "ReUploadTasks.json");
            if (taskEntries.getNumEntries() > 0) {
                SendTaskEntriesToServer sendTaskEntriesToServer = new SendTaskEntriesToServer(this, taskEntries);
                sendTaskEntriesToServer.sendTaskEntriesToServer();
            }
        } else if (SharedPreferencesWrapper.getFromPrefs(this, "reUpload_drink_diary", false)) {
            DrinkDiaryEntries drinkDiaryEntries = DrinkDiaryEntries.createDrinkDiaryEntriesFromStorage(this, "ReUploadDrinkDiary.json");
            if (drinkDiaryEntries.getNumEntries() > 0) {
                SendDrinkDiaryEntriesToServer sendDrinkDiaryEntriesToServer = new SendDrinkDiaryEntriesToServer(this, drinkDiaryEntries);
                sendDrinkDiaryEntriesToServer.sendDrinkDiaryEntriesToServer();
            }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_logout:
                DialogFragment logoutDialogFragment = new LogoutDialogFragment();
                logoutDialogFragment.show(getSupportFragmentManager(), "DIALOG_LOGOUT");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void unitCalculationComplete(DrinkDiaryEntry drinkDiaryEntry, Boolean save) {
        DrinkDiaryEntries drinkDiaryEntries = DrinkDiaryEntries.createDrinkDiaryEntriesFromStorage(this, "DrinkDiaryEntries.json");
        SaveDrinkDiaryUtility saveDrinkDiaryUtility = new SaveDrinkDiaryUtility(this, drinkDiaryEntry, drinkDiaryEntries);
        saveDrinkDiaryUtility.saveDrinkDiaryEntryToStorage();
        getSupportFragmentManager().popBackStack();
    }

    private void menuSelection(int pos) {

        Fragment fragment = null;
        ListFragment listFragment = null;
        switch (pos) {
            case 0:
                Bundle args = new Bundle();
                args.putBoolean("play_animation", false);
                fragment = new HomeFragment();
                fragment.setArguments(args);
                fragmentName = "Home";
                break;
            case 1:
                fragment = new TasksMenuFragment();
                fragmentName = "Tasks";
                break;
            case 2:
                listFragment = new DrinkDiaryFragment();
                fragmentName = "Drink Diary";
                break;
            case 3:
                fragment = new UnitCalculatorFragment();
                fragmentName = "Unit Calculator";
                break;
            case 4:
                fragment = new AccountDetailsFragment();
                fragmentName = "Account Details";
                break;
            default:
                break;
        }
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            int count = getSupportFragmentManager().getBackStackEntryCount();
            if (count > 0) {
                FragmentManager.BackStackEntry backStackEntry = getSupportFragmentManager().getBackStackEntryAt(count - 1);
                String name = backStackEntry.getName();
                if (!name.equals(fragmentName)) {
                    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, fragmentName).addToBackStack(fragmentName).commit();
                    mDrawerList.setItemChecked(pos, true);
                    mDrawerList.setSelection(pos);
                    setTitle(mDrawerOptions[pos]);
                }
            } else {
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, fragmentName).addToBackStack(fragmentName).commit();
                mDrawerList.setItemChecked(pos, true);
                mDrawerList.setSelection(pos);
                setTitle(mDrawerOptions[pos]);
            }
            mDrawerLayout.closeDrawer(mDrawerList);
        } else if (listFragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            int count = getSupportFragmentManager().getBackStackEntryCount();
            if (count > 0) {
                FragmentManager.BackStackEntry backStackEntry = getSupportFragmentManager().getBackStackEntryAt(count - 1);
                String name = backStackEntry.getName();
                if (!name.equals(fragmentName)) {
                    fragmentManager.beginTransaction().replace(R.id.content_frame, listFragment, fragmentName).addToBackStack(fragmentName).commit();
                    mDrawerList.setItemChecked(pos, true);
                    mDrawerList.setSelection(pos);
                    setTitle(mDrawerOptions[pos]);
                }
            } else {
                fragmentManager.beginTransaction().replace(R.id.content_frame, listFragment, fragmentName).addToBackStack(fragmentName).commit();
                mDrawerList.setItemChecked(pos, true);
                mDrawerList.setSelection(pos);
                setTitle(mDrawerOptions[pos]);
            }
            mDrawerLayout.closeDrawer(mDrawerList);
        } else {
            //error creating fragment
            Log.e("NAVIGATION_DRAWER", "Error in creating fragment");
        }
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        MenuItem taskCalibration = menu.findItem(R.id.action_task_calibration);
        taskCalibration.setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_activity_menu, menu);
        return true;
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    @Override
    public void finish() {
        SharedPreferencesWrapper.saveToPrefs(this, "last_date_played", DateAndTime.getDateAndTimeNowForTasks());
        if (SharedPreferencesWrapper.getFromPrefs(this, "new_user", true)) {
            //if the user has done both task then they are not a new user anymore and don't need to see instructions
            if (!SharedPreferencesWrapper.getFromPrefs(this, "first_time_inspection_task", false) && !SharedPreferencesWrapper.getFromPrefs(this, "first_time_sequence_task", false)) {
                SharedPreferencesWrapper.saveToPrefs(this, "new_user", false);
            }
        }
        if (!SharedPreferencesWrapper.getFromPrefs(this, "reUpload_drink_diary", false)) {
            DrinkDiaryEntries drinkDiaryEntries1 = new DrinkDiaryEntries();
            drinkDiaryEntries1.saveToStorage(this, "ReUploadDrinkDiary.json");
        }
        if (!SharedPreferencesWrapper.getFromPrefs(this, "reUpload_tasks", false)) {
            TaskEntries taskEntries1 = new TaskEntries();
            taskEntries1.saveToStorage(this, "ReUploadTasks.json");
        }

        //clear the unit calculator
        SharedPreferencesWrapper.removeFromPrefs(this, "unit_calc_drinkName");
        SharedPreferencesWrapper.removeFromPrefs(this, "unit_calc_abv");
        SharedPreferencesWrapper.removeFromPrefs(this, "unit_calc_drinkVolume");
        SharedPreferencesWrapper.removeFromPrefs(this, "unit_calc_volPos");
        SharedPreferencesWrapper.removeFromPrefs(this, "unit_calc_typePos");
        SharedPreferencesWrapper.removeFromPrefs(this, "unit_calc_quantity");
        SharedPreferencesWrapper.removeFromPrefs(this, "unit_calc_units");
        super.finish();
    }

    private void retrieveDrinkDiaryEntries() {
        RetrieveHTTPDataAsync retrieveDrinkDiaryEntries = new RetrieveHTTPDataAsync(this);
        String mEmail = SharedPreferencesWrapper.getFromPrefs(this, "email", "NO_EMAIL");
        String mPassword = SharedPreferencesWrapper.getFromPrefs(this, "password", "NO_PASSWORD");
        retrieveDrinkDiaryEntries.execute("http://08309.net.dcs.hull.ac.uk/api/admin/diary?username=" + mEmail + "&password=" + mPassword);
    }

    @Override
    public void onRetrieveTaskCompleted(String httpData) {
        Log.i("RETRIEVE_DRINK_DIARY_ENTRIES_RESPONSE", httpData);
        if (!httpData.equals("null")) {
            processDrinkDiaryEntries(httpData);
        }
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingScreen(false);
                intro.start();
                intro.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        intro.release();
                    }
                });
                //load the home fragment if there are no other fragments(in case activity has been restarted)
                FragmentManager fragmentManager = getSupportFragmentManager();
                List<Fragment> fragmentList = fragmentManager.getFragments();
                if (fragmentList == null) {
                    HomeFragment homeFragment = new HomeFragment();
                    fragmentManager.beginTransaction().replace(R.id.content_frame, homeFragment, "home").commit();
                }
            }
        }, 2000);
        SharedPreferencesWrapper.saveToPrefs(this, "first_home_start", false);
    }

    private void playIntroMusic() {
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                intro.start();
                intro.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        intro.release();
                    }
                });
            }
        }, 0);
    }

    private void processDrinkDiaryEntries(String httpData) {
        DrinkDiaryEntries drinkDiaryEntries = DrinkDiaryEntries.createDDEntriesFromWebServer(httpData);
        if (drinkDiaryEntries.getNumEntries() > 0) {
            DrinkDiaryEntry drinkDiaryEntry = drinkDiaryEntries.getEntry(0);
            SharedPreferencesWrapper.saveToPrefs(this, "lastDrinkAdded", drinkDiaryEntry.drinkName);
            SharedPreferencesWrapper.saveToPrefs(this, "unitsOfLastDrinkAdded", drinkDiaryEntry.units);
            drinkDiaryEntries.saveToStorage(this, "DrinkDiaryEntries.json");
        }
    }

    /**
     * show loading screen
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void loadingScreen(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade_out_and_scale_up-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            loadingView.setVisibility(View.VISIBLE);
            loadingView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            loadingView.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence("title", mTitle);
        outState.putBoolean("isDrawerOpen",
                mDrawerLayout.isDrawerOpen(mDrawerList));
    }

    public void aboutDialog() {
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.about_dialog);
        dialog.setTitle(R.string.action_about);
        dialog.setCancelable(true);

        //make a toast for user feedback
        Toast.makeText(getBaseContext(), "How To", Toast.LENGTH_SHORT).show();

        //sets up the TextView within the dialog to display the exercise description
        TextView dialogText = (TextView) dialog.findViewById(R.id.exercise_description_dialog_text);
        dialogText.setText(getString(R.string.dialog_about_developer_message) +
                getString(R.string.dialog_about_attribution_message));

        dialog.show();
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            menuSelection(position);
        }
    }
}
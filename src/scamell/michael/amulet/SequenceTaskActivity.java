package scamell.michael.amulet;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SequenceTaskActivity extends FragmentActivity implements UnitCalculatorFragment.UnitCalculatorListener {

    private final static String TAG_TASK_START_COUNTDOWN_FRAGMENT = "TAG_TASK_START_COUNTDOWN_FRAGMENT";
    private final static String TAG_UNIT_CALCULATOR_FRAGMENT = "TAG_UNIT_CALCULATOR_FRAGMENT";
    private final int CORRECT_SOUND = 1;
    private final int WRONG_SOUND = 2;
    private final int easyTaskSize = 9;
    private final int easyGridSize = 3;
    private final int normalTaskSize = 16;
    private final int normalGridSize = 4;
    private final int hardTaskSize = 25;
    private final int hardGridSize = 5;
    private CountDownTimer timer;
    private float currentTime = 0;
    private Boolean calibrationMode;
    private Vibrator vib;
    private Map<String, String> trackGridNumbersIndexMap = new HashMap<String, String>();
    private Boolean firstStart = true;
    private Boolean taskRunning = false;
    private Boolean beingResumed = false;
    private Sound sound;
    private TaskEntries taskEntries = new TaskEntries();
    private int taskLevel = 1;
    private int nextAnswer = 1;
    private int prevAnswer = 1;
    private float lastPlayTaskTime;
    private float thisPlayTaskTime;
    private TableLayout tableLayout;
    private TableRow row4;
    private TableRow row5;

    private ArrayList<Integer> textViewNumbers;
    private ArrayList<String> gridNumbers;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sequence_task);

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        sound = new Sound(this, 2);
        sound.loadSound(this, CORRECT_SOUND, R.raw.sequence_task_correct, 1);
        sound.loadSound(this, WRONG_SOUND, R.raw.sequence_task_wrong, 1);

        //get the last speed set for the game
        lastPlayTaskTime = SharedPreferencesWrapper.getFromPrefs(this, "last_sequence_task_time", 0.0f);

        taskEntries = TaskEntries.createTaskEntriesFromStorage(this, "SequenceTaskEntries.json");

        //a timer that updates every 0.5 seconds and should provide accurate enough answers
        timer = new CountDownTimer(Long.MAX_VALUE, 500) {

            @Override
            public void onTick(long millisUntilFinished) {
                currentTime = currentTime + 0.5f;
            }

            @Override
            public void onFinish() {
                thisPlayTaskTime = currentTime;
                onTaskFinish();
            }
        };

        //lock to portrait for the duration of the task
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //create a vibrator
        vib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        //get hold of all the views needed for the task
        tableLayout = (TableLayout) findViewById(R.id.activity_sequence_task_tablelayout);
        row4 = (TableRow) findViewById(R.id.table_row_4);
        row5 = (TableRow) findViewById(R.id.table_row_5);

        Intent intent = getIntent();

        gridNumbers = new ArrayList<String>();

        //checks to make sure we're not coming back from the activity being destroyed by Android in
        //the middle of the task
        if (!taskRunning) {
            try {
                calibrationMode = intent.getExtras().getBoolean("calibration_mode", false);
            } catch (Exception e) {
                Log.e("SEQUENCE_TASK_ACTIVITY", "No calibration boolean");
                calibrationMode = false;
            }
            if (calibrationMode) {
                Toast.makeText(this, "Calibration Mode", Toast.LENGTH_SHORT).show();
                startTaskCountdown();
            } else {
                startUnitCalculator();
            }
        }
    }

    private void onTaskFinish() {

        TaskEntry taskEntry = new TaskEntry();
        taskEntry.taskType = getString(R.string.activity_sequence_task_sequence);
        taskEntry.taskValue = String.valueOf(thisPlayTaskTime);
        taskEntry.date = DateAndTime.getDateAndTimeNowForTasks();
        if (calibrationMode) {
            //if calibration mode then save the calibration score locally and make the units 0
            //to identify it as the calibration score
            SharedPreferencesWrapper.saveToPrefs(this, "calibration_time_sequence_task", String.valueOf(thisPlayTaskTime));
            taskEntry.units = "0";
        } else {
            taskEntry.units = SharedPreferencesWrapper.getFromPrefs(this, "task_session_units", "NO_UNITS");
        }
        taskEntries.addEntry(taskEntry);
        taskEntries.saveToStorage(this, "SequenceTaskEntries.json");

        SendTaskEntryToServer sendTaskEntryToServer = new SendTaskEntryToServer(this, taskEntry);
        sendTaskEntryToServer.sendTaskEntryToServer();

        //save this sessions speed as the last speed for next reload
        SharedPreferencesWrapper.saveToPrefs(this, "last_sequence_task_time", thisPlayTaskTime);
        loadTaskFinishFragment();
    }

    private void startEasyTask() {
        firstStart = false;
        tableLayout.setColumnCollapsed(4, true);
        tableLayout.setColumnCollapsed(5, true);
        tableLayout.invalidate();
        if (!beingResumed) {
            setupSequenceTask(easyGridSize);
            timer.start();
        }
    }

    private void startNormalTask() {
        row4.setVisibility(View.VISIBLE);
        tableLayout.setColumnCollapsed(4, false);
        tableLayout.invalidate();
        if (!beingResumed) {
            setupSequenceTask(normalGridSize);
        }
    }

    private void startHardTask() {
        row5.setVisibility(View.VISIBLE);
        tableLayout.setColumnCollapsed(5, false);
        tableLayout.invalidate();
        if (!beingResumed) {
            setupSequenceTask(hardGridSize);
        }
    }

    private void setupSequenceTask(int gridSize) {
        //set up a random array of numbers for the size of the task that is happening
        textViewNumbers = new ArrayList<Integer>();
        //this is to make sure we go from 0-the size of the task in the second for loop. Otherwise
        //if we used i or j it would continuously overwrite the array and the array would not be the right size
        int indexCount = 0;
        int k = 1;
        for (int i = 0; i < gridSize * gridSize; i++) {
            textViewNumbers.add(k++);
        }
        trackGridNumbersIndexMap = new HashMap<String, String>();
        //used to generate a string to save to SharedPrefs so that the game can be restarted.
        gridNumbers = new ArrayList<String>();
        Random random = new Random();
        //plus one to the texViewNumbers to compensate for the 0 in the array index
        //for the size of the grid, get the child at the start which is a tablerow
        for (int i = 0; i < gridSize; i++) {
            ViewGroup view = (ViewGroup) tableLayout.getChildAt(i);
            //then create a random number based on the size of the game, get the textview to place
            // the number and set it. Then remove this index from the textViewNumbers which holds
            // values from 1-gamesize in its array so that it can't be set again. Generate a new random
            // number based on the reduced size.
            for (int j = 0; j < gridSize; j++) {
                int range = (textViewNumbers.size() - 1) + 1;
                TextView textView = (TextView) view.getChildAt(j);
                int randVal = random.nextInt(range);
                //keep a log of where the numbers are in the textviews e.g "20" at index 0, "3" at index 5
                trackGridNumbersIndexMap.put(textViewNumbers.get(randVal).toString(), String.valueOf(indexCount));
                indexCount++;
                //create an array of the numbers from 0-grid size. This to blank out when an answer is
                //correct so it can be written as string to sharedpreferences
                gridNumbers.add(textViewNumbers.get(randVal).toString());
                //set the text view to the random number
                textView.setText(textViewNumbers.get(randVal).toString());
                textViewNumbers.remove(randVal);
            }
        }
    }

    private void resumeTask() {
        int gridSize = 0;
        int count = 0;
        switch (taskLevel) {
            case 1:
                gridSize = easyGridSize;
                startEasyTask();
                break;
            case 2:
                gridSize = normalGridSize;
                startNormalTask();
                break;
            case 3:
                gridSize = hardGridSize;
                startHardTask();
                break;
        }
        for (int i = 0; i < gridSize; i++) {
            ViewGroup view = (ViewGroup) tableLayout.getChildAt(i);
            for (int j = 0; j < gridSize; j++) {
                TextView textView = (TextView) view.getChildAt(j);
                textView.setText(gridNumbers.get(count));
                count++;
            }
        }
        timer.start();
        beingResumed = false;
    }

    private void startUnitCalculator() {
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        final UnitCalculatorFragment unitCalculatorFragment = new UnitCalculatorFragment();
        Bundle args = new Bundle();
        args.putBoolean("task_unit_calculator", true);
        unitCalculatorFragment.setArguments(args);
        transaction.replace(R.id.activity_sequence_task_tablelayout, unitCalculatorFragment, TAG_UNIT_CALCULATOR_FRAGMENT);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * View Animations:
     * http://developer.android.com/guide/topics/graphics/view-animation.html
     */
    public void checkAnswer(View view) {
        int numberselected;
        final TextView textView = (TextView) view;
        numberselected = Integer.valueOf(textView.getText().toString());
        //if answer is correct do a fade out and scale animation
        if (nextAnswer == numberselected) {
            sound.playSound(CORRECT_SOUND);
            nextAnswer++;
            //for each one that's right make it blank in the numbersToarray in case game is paused. Used with gridNumbers to reset
            String indexToWipe = trackGridNumbersIndexMap.get(String.valueOf(numberselected));
            //set the index position of the number to blank, this way theres no need to remove the text view or hide it
            //and also makes generating a string to save later in onPause easier
            gridNumbers.set(Integer.valueOf(indexToWipe), " ");
            //each number must have it's own animation otherwise numbers get left on screen and never cleared
            Animation correctAnswerAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out_and_scale_up);
            correctAnswerAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    textView.setEnabled(false);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //if an animation has run then we know it's been the correct answer
                    //also prevents animations starting on the next level as they started too soon
                    textView.setText(" ");
                    checkLevelEnd();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            //play the animation
            textView.startAnimation(correctAnswerAnimation);
            //and make sure the TextView cannot be clicked again
            textView.setEnabled(false);
        }
        //if the answers wrong do shake animation
        else {
            sound.playSound(WRONG_SOUND);
            //each number must have it's own animation otherwise numbers get left on screen and never cleared
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
            textView.startAnimation(shake);
            vib.vibrate(50);
        }
        Log.i("SEQUENCE_TASK_ACTIVITY", "Number tapped");
    }

    private void checkLevelEnd() {
        switch (taskLevel) {
            case 1:
                if (prevAnswer == easyTaskSize) {
                    nextAnswer = 1;
                    prevAnswer = 1;
                    taskLevel = 2;
                    resetTextViewVisibility(easyGridSize);
                    startNormalTask();
                } else {
                    prevAnswer++;
                }
                break;
            case 2:
                if (prevAnswer == normalTaskSize) {
                    nextAnswer = 1;
                    prevAnswer = 1;
                    taskLevel = 3;
                    resetTextViewVisibility(normalGridSize);
                    startHardTask();
                } else {
                    prevAnswer++;
                }
                break;
            case 3:
                if (prevAnswer == hardTaskSize) {
                    timer.onFinish();
                } else {
                    prevAnswer++;
                }
                break;
        }
    }

    /**
     * makes sure that all the TextViews are currently on screen before the new game
     * are enabled (so they can be clicked) on a new game
     *
     * @param gridSize how many views to re-enable
     */
    private void resetTextViewVisibility(int gridSize) {
        for (int i = 0; i < gridSize; i++) {
            ViewGroup view = (ViewGroup) tableLayout.getChildAt(i);
            for (int j = 0; j < gridSize; j++) {
                TextView textView = (TextView) view.getChildAt(j);
                textView.setEnabled(true);
            }
        }
    }

    private void loadTaskFinishFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        TaskFinishFragment taskFinishFragment = new TaskFinishFragment();
        Bundle args = new Bundle();
        //Bundle all the times for the task finish fragment
        args.putString("task_type", getString(R.string.activity_sequence_task_sequence));
        args.putFloat("time_taken", thisPlayTaskTime);
        args.putFloat("last_time", lastPlayTaskTime);
        args.putBoolean("calibration_mode", calibrationMode);
        taskFinishFragment.setArguments(args);
        transaction.replace(R.id.activity_sequence_task_tablelayout, taskFinishFragment);
        transaction.commitAllowingStateLoss();
    }

    private void startTaskCountdown() {
        taskRunning = true;
        final Handler handler = new Handler();
        final FragmentTransaction secondTransaction = getSupportFragmentManager().beginTransaction();
        TaskStartCountdownFragment taskStartCountdownFragment = new TaskStartCountdownFragment() {
            @Override
            protected void onStartCountDownFinish() {
                //pauses so the "go!" is not removed too quickly
                handler.postDelayed(new Runnable() {
                    public void run() {
                        //in case user exits during countdown make sure the activity is not null.
                        // If it isn't start the game
                        if (getActivity() != null) {
                            FragmentManager fm = getSupportFragmentManager();
                            fm.popBackStack();
                            startEasyTask();
                        }
                    }
                }, 500);
            }
        };
        secondTransaction.replace(R.id.activity_sequence_task_tablelayout, taskStartCountdownFragment, TAG_TASK_START_COUNTDOWN_FRAGMENT);
        secondTransaction.addToBackStack(null);
        //delays committing the fragment so as not to start the timer too quickly for the user
        // through the transition from selecting the task
        handler.postDelayed(new Runnable() {
            public void run() {
                secondTransaction.commit();
            }
        }, 1000);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //remove the taskStartCountDownfragment and unitCalculatorFragment if the back button is pressed
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        TaskStartCountdownFragment taskStartCountdownfragment = (TaskStartCountdownFragment) getSupportFragmentManager().findFragmentByTag(TAG_TASK_START_COUNTDOWN_FRAGMENT);
        UnitCalculatorFragment unitCalculatorFragment = (UnitCalculatorFragment) getSupportFragmentManager().findFragmentByTag(TAG_UNIT_CALCULATOR_FRAGMENT);
        if (taskStartCountdownfragment != null) {
            transaction.remove(taskStartCountdownfragment);
        }
        if (unitCalculatorFragment != null) {
            transaction.remove(unitCalculatorFragment);
        }
        taskRunning = false;
        finish();
    }

    @Override
    public void onPause() {
        if (taskRunning) {
            sound.shutDownSoundPool();
            SharedPreferencesWrapper.saveToPrefs(this, "currentTime", currentTime);
            SharedPreferencesWrapper.saveToPrefs(this, "taskRunning", taskRunning);
            SharedPreferencesWrapper.saveToPrefs(this, "nextAnswer", nextAnswer);
            SharedPreferencesWrapper.saveToPrefs(this, "taskLevel", taskLevel);
            StringBuilder builder = new StringBuilder();
            for (String number : gridNumbers) {
                builder.append(number);
                builder.append("-");
            }
            SharedPreferencesWrapper.saveToPrefs(this, "gridNumbers", builder.toString());
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        taskRunning = SharedPreferencesWrapper.getFromPrefs(this, "taskRunning", false);
        if (!firstStart && taskRunning) {
            sound = new Sound(this, 2);
            sound.loadSound(this, CORRECT_SOUND, R.raw.sequence_task_correct, 1);
            sound.loadSound(this, WRONG_SOUND, R.raw.sequence_task_wrong, 1);
            currentTime = SharedPreferencesWrapper.getFromPrefs(this, "currentTime", 0.0f);
            nextAnswer = SharedPreferencesWrapper.getFromPrefs(this, "nextAnswer", 1);
            taskLevel = SharedPreferencesWrapper.getFromPrefs(this, "taskLevel", 1);
            //gets the grid numbers string from Shared Prefs, splits it and puts it back into the gridNumbers
            //array so the game can be re-setup
            String gridNumbersString = SharedPreferencesWrapper.getFromPrefs(this, "gridNumbers", "NULL");
            String[] gridNumberStringArray = gridNumbersString.split("-");
            gridNumbers = new ArrayList<String>();
            Collections.addAll(gridNumbers, gridNumberStringArray);
            beingResumed = true;
            firstStart = false;
            ResumeTaskDialogFragment resumeTaskDialogFragment = new ResumeTaskDialogFragment(
                    getString(R.string.activity_sequence_task_message_dialog),
                    getString(R.string.activity_sequence_task_pos_dialog),
                    getString(R.string.activity_sequence_task_neg_dialog),
                    getString(R.string.activity_sequence_task_neu_dialog)) {
                @Override
                public void posButtonClicked() {
                    resumeTask();
                }

                @Override
                public void negButtonClicked() {
                    //if they don't want to play any more then close the task
                    finish();
                }

                @Override
                public void neutralButtonClicked() {
                    //if they want to restart, close the task and start a new game
                    finish();
                    Intent intent = new Intent(SequenceTaskActivity.this, SequenceTaskActivity.class);
                    intent.putExtra("calibration_mode", calibrationMode);
                    startActivity(intent);
                }
            };
            resumeTaskDialogFragment.show(getSupportFragmentManager(), "DIALOG_RESUME_TASK");
        }
        super.onResume();
    }

    @Override
    public void unitCalculationComplete(DrinkDiaryEntry drinkDiaryEntry, Boolean save) {
        if (save) {
            DrinkDiaryEntries drinkDiaryEntries = DrinkDiaryEntries.createDrinkDiaryEntriesFromStorage(this, "DrinkDiaryEntries.json");
            SaveDrinkDiaryUtility saveDrinkDiaryUtility = new SaveDrinkDiaryUtility(this, drinkDiaryEntry, drinkDiaryEntries);
            saveDrinkDiaryUtility.saveDrinkDiaryEntryToStorage();
        }
        getSupportFragmentManager().popBackStack();
        startTaskCountdown();
    }
}
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
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Random;

public class InspectionTaskActivity extends FragmentActivity implements UnitCalculatorFragment.UnitCalculatorListener {

    private final static String TAG_TASK_START_COUNTDOWN_FRAGMENT = "TAG_TASK_START_COUNTDOWN_FRAGMENT";

    private final static String TAG_UNIT_CALCULATOR_FRAGMENT = "TAG_UNIT_CALCULATOR_FRAGMENT";

    private final int CORRECT_SOUND = 1;
    private final int WRONG_SOUND = 2;
    private final int defaultStartPresentationTime = 1000;
    private final int defaultLowEndStartPresentationTime = 56;
    private final double decrementalValue = 0.75;
    private final double incrementalValue = 1.33;
    private final int maxImages = 4;
    private final int minImages = 1;
    private final int maxDisplayTime = 3000;
    private final int minDisplayTime = 32;
    private Boolean firstStart = true;
    private Boolean taskRunning = false;
    private TaskEntries taskEntries = new TaskEntries();
    private ImageButton topLeftButton;
    private ImageButton topRightButton;
    private ImageButton bottomLeftButton;
    private ImageButton bottomRightButton;
    private Sound sound;
    private Handler handler;
    private Boolean calibrationMode;
    private int turnCounter = 15;
    private int correctInRow = 0;
    private int presentationTime;
    private int lastCorrectAnswerTime;
    private int altBeerLocation;
    private Vibrator vib;

    public void onCreate(Bundle savedInstanceState) {

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        vib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        sound = new Sound(this, 2);
        sound.loadSound(this, CORRECT_SOUND, R.raw.inspection_task_correct2, 1);
        sound.loadSound(this, WRONG_SOUND, R.raw.inspection_task_wrong2, 1);

        //lock to portrait for the duration of the task
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection_task);

        taskEntries = TaskEntries.createTaskEntriesFromStorage(this, "InspectionTaskEntries.json");

        //checks to make sure we're not coming back from the activity being destroyed by Android in
        //the middle of the task
        if (!taskRunning) {
            Intent intent = getIntent();
            try {
                calibrationMode = intent.getExtras().getBoolean("calibration_mode");
            } catch (Exception e) {
                Log.e("INSPECTION_TASK_ACTIVITY", "No calibration boolean");
                calibrationMode = false;
            }
            //if it's calibration mode then start the user off with a default reasonable time
            if (calibrationMode) {
                presentationTime = defaultStartPresentationTime;
                lastCorrectAnswerTime = presentationTime;
                Toast.makeText(this, "Calibration Mode", Toast.LENGTH_SHORT).show();
                startTaskCountdown();
            } else {
                presentationTime = SharedPreferencesWrapper.getFromPrefs(this, "last_inspection_task_time", 0);
                //if no calibration data is found set the presentation time to default along with the
                //lastCorrectAnswerTime
                if (presentationTime == 0) {
                    presentationTime = defaultStartPresentationTime;
                    lastCorrectAnswerTime = presentationTime;
                    //if the last time was quicker then the default low end time then set it to the default
                    // low end to not overwhelm the user on first go
                } else if (presentationTime > 0 && presentationTime < defaultLowEndStartPresentationTime) {
                    presentationTime = defaultLowEndStartPresentationTime;
                    lastCorrectAnswerTime = presentationTime;
                } else {
                    lastCorrectAnswerTime = presentationTime;
                }
                startUnitCalculator();
            }
        }
    }

    private void setupImageButtons() {
        topLeftButton = (ImageButton) findViewById(R.id.top_left_box);
        topLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(1);
            }
        });
        topRightButton = (ImageButton) findViewById(R.id.top_right_box);
        topRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(2);
            }
        });
        bottomLeftButton = (ImageButton) findViewById(R.id.bottom_left_box);
        bottomLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(3);
            }
        });
        bottomRightButton = (ImageButton) findViewById(R.id.bottom_right_box);
        bottomLeftButton.setSoundEffectsEnabled(false);
        bottomRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(4);
            }
        });
    }

    private void startUnitCalculator() {
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        final UnitCalculatorFragment unitCalculatorFragment = new UnitCalculatorFragment() {
            @Override
            protected void startTaskButtonClicked() {
                FragmentManager fm = getSupportFragmentManager();
                fm.popBackStack();
                startTaskCountdown();
            }
        };
        Bundle args = new Bundle();
        args.putBoolean("task_unit_calculator", true);
        unitCalculatorFragment.setArguments(args);
        transaction.replace(R.id.inspection_task_container, unitCalculatorFragment, TAG_UNIT_CALCULATOR_FRAGMENT);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void runTask() {
        firstStart = false;
        taskRunning = true;
        setupImageButtons();
        imageChangeTimer(presentationTime);
    }

    //countdown to start when changing image positions
    private void imageChangeTimer(int time) {
        disableButtons();
        setAlternateImage();
        new CountDownTimer(time, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                hideImages();
                enableButtons();
            }
        }.start();
    }

    private void hideImages() {
        topLeftButton.setImageDrawable(null);
        topRightButton.setImageDrawable(null);
        bottomLeftButton.setImageDrawable(null);
        bottomRightButton.setImageDrawable(null);
    }

    private void disableButtons() {
        topLeftButton.setEnabled(false);
        topRightButton.setEnabled(false);
        bottomLeftButton.setEnabled(false);
        bottomRightButton.setEnabled(false);
    }

    private void enableButtons() {
        topLeftButton.setEnabled(true);
        topRightButton.setEnabled(true);
        bottomLeftButton.setEnabled(true);
        bottomRightButton.setEnabled(true);
    }

    private void setAlternateImage() {
        Random random = new Random();
        int range = maxImages - minImages + 1;
        altBeerLocation = random.nextInt(range) + minImages;
        switch (altBeerLocation) {
            case (1):
                topLeftButton.setImageDrawable(getResources().getDrawable(R.drawable.alt_beer));
                topRightButton.setImageDrawable(getResources().getDrawable(R.drawable.normal_beer));
                bottomLeftButton.setImageDrawable(getResources().getDrawable(R.drawable.normal_beer));
                bottomRightButton.setImageDrawable(getResources().getDrawable(R.drawable.normal_beer));
                break;
            case (2):
                topLeftButton.setImageDrawable(getResources().getDrawable(R.drawable.normal_beer));
                topRightButton.setImageDrawable(getResources().getDrawable(R.drawable.alt_beer));
                bottomLeftButton.setImageDrawable(getResources().getDrawable(R.drawable.normal_beer));
                bottomRightButton.setImageDrawable(getResources().getDrawable(R.drawable.normal_beer));
                break;
            case (3):
                topLeftButton.setImageDrawable(getResources().getDrawable(R.drawable.normal_beer));
                topRightButton.setImageDrawable(getResources().getDrawable(R.drawable.normal_beer));
                bottomLeftButton.setImageDrawable(getResources().getDrawable(R.drawable.alt_beer));
                bottomRightButton.setImageDrawable(getResources().getDrawable(R.drawable.normal_beer));
                break;
            case (4):
                topLeftButton.setImageDrawable(getResources().getDrawable(R.drawable.normal_beer));
                topRightButton.setImageDrawable(getResources().getDrawable(R.drawable.normal_beer));
                bottomLeftButton.setImageDrawable(getResources().getDrawable(R.drawable.normal_beer));
                bottomRightButton.setImageDrawable(getResources().getDrawable(R.drawable.alt_beer));
                break;
        }
    }

    private void checkAnswer(int buttonPressed) {
        //if correct
        if (buttonPressed == altBeerLocation) {
            sound.playSound(CORRECT_SOUND);
            confirmRightAnswer();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (presentationTime != minDisplayTime) {
                        lastCorrectAnswerTime = presentationTime;
                        double d = presentationTime * decrementalValue;
                        presentationTime = (int) d;
                        if (presentationTime < minDisplayTime) {
                            presentationTime = minDisplayTime;
                        }
                    } else {
                        correctInRow++;
                    }
                    checkTaskEnd();
                }
            }, 2250);
        }
        //if wrong and on a previously presentation time that was correctly answered or new game
        // then decrement
        else if (lastCorrectAnswerTime == presentationTime) {
            sound.playSound(WRONG_SOUND);
            vib.vibrate(100);
            showCorrectAnswer();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    double d = presentationTime * incrementalValue;
                    presentationTime = (int) d;
                    lastCorrectAnswerTime = presentationTime;
                    //if the value is beyond the max limit for displaying the images then keep presentation
                    // and lastCorrectAnswer at this limit
                    if (presentationTime > maxDisplayTime) {
                        presentationTime = maxDisplayTime;
                        lastCorrectAnswerTime = maxDisplayTime;
                    }
                    checkTaskEnd();
                }
            }, 2250);
        }
        //otherwise it's first time wrong since a successful answer or new start so go back to last
        // known correct answer time
        else {
            sound.playSound(WRONG_SOUND);
            showCorrectAnswer();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    presentationTime = lastCorrectAnswerTime;
                    checkTaskEnd();
                }
            }, 2250);
        }
        turnCounter--;
    }

    private void confirmRightAnswer() {
        switch (altBeerLocation) {
            case 1:
                topLeftButton.setImageDrawable(getResources().getDrawable(R.drawable.tick));
                break;
            case 2:
                topRightButton.setImageDrawable(getResources().getDrawable(R.drawable.tick));
                break;
            case 3:
                bottomLeftButton.setImageDrawable(getResources().getDrawable(R.drawable.tick));
                break;
            case 4:
                bottomRightButton.setImageDrawable(getResources().getDrawable(R.drawable.tick));
                break;
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideImages();
            }
        }, 1500);
    }

    private void showCorrectAnswer() {
        switch (altBeerLocation) {
            case 1:
                topLeftButton.setImageResource(R.drawable.alt_beer);
                break;
            case 2:
                topRightButton.setImageResource(R.drawable.alt_beer);
                break;
            case 3:
                bottomLeftButton.setImageResource(R.drawable.alt_beer);
                break;
            case 4:
                bottomRightButton.setImageResource(R.drawable.alt_beer);
                break;
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideImages();
            }
        }, 1500);
    }

    private void checkTaskEnd() {
        if (turnCounter > 0 && correctInRow < 3) {
            imageChangeTimer(presentationTime);
        } else {
            onTaskFinish();
        }
    }

    /**
     * Sending data to a fragment:
     * http://developer.android.com/training/basics/fragments/communicating.html
     */
    private void onTaskFinish() {
        taskRunning = false;
        TaskEntry taskEntry = new TaskEntry();
        taskEntry.taskType = getString(R.string.activity_inspection_task_inspection);
        taskEntry.taskValue = String.valueOf(presentationTime);
        taskEntry.date = DateAndTime.getDateAndTimeNowForTasks();
        if (calibrationMode) {
            //if calibration mode then save the calibration score locally and make the units 0
            //to identify it as the calibration score
            SharedPreferencesWrapper.saveToPrefs(this, "calibration_time_inspection_task", presentationTime);
            taskEntry.units = "0";
        } else {
            taskEntry.units = SharedPreferencesWrapper.getFromPrefs(this, "task_session_units", "NO_UNITS");
        }
        taskEntries.addEntry(taskEntry);
        taskEntries.saveToStorage(this, "InspectionTaskEntries.json");

        SendTaskEntryToServer sendTaskEntryToServer = new SendTaskEntryToServer(this, taskEntry);
        sendTaskEntryToServer.sendTaskEntryToServer();

        //get the last speed recorded for the task, if not send 0 and don't display the message
        int lastInspectionSpeed = SharedPreferencesWrapper.getFromPrefs(this, "last_inspection_task_time", 0);
        //save the new speed as the last speed
        SharedPreferencesWrapper.saveToPrefs(this, "last_inspection_task_time", presentationTime);
        //load the task finish fragment
        loadTaskFinishFragment(lastInspectionSpeed);
    }

    private void loadTaskFinishFragment(int lastInspectionSpeed) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        TaskFinishFragment taskFinishFragment = new TaskFinishFragment();
        Bundle args = new Bundle();
        //bundle the speeds to send to the fragment
        args.putString("task_type", getString(R.string.activity_inspection_task_inspection));
        args.putInt("speed", presentationTime);
        args.putInt("last_speed", lastInspectionSpeed);
        args.putBoolean("calibration_mode", calibrationMode);
        taskFinishFragment.setArguments(args);
        transaction.replace(R.id.inspection_task_container, taskFinishFragment);
        transaction.commitAllowingStateLoss();
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

    private void startTaskCountdown() {
        handler = new Handler();
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
                            runTask();
                        }
                    }
                }, 500);
            }
        };
        secondTransaction.replace(R.id.inspection_task_container, taskStartCountdownFragment, TAG_TASK_START_COUNTDOWN_FRAGMENT);
        secondTransaction.addToBackStack(null);
        //delays committing the fragment so as not to start the timer too quickly for the user
        // through the transition from selecting the game
        handler.postDelayed(new Runnable() {
            public void run() {
                secondTransaction.commit();
            }
        }, 1000);
    }

    @Override
    public void onPause() {
        if (taskRunning) {
            SharedPreferencesWrapper.saveToPrefs(this, "turnCounter", turnCounter);
            SharedPreferencesWrapper.saveToPrefs(this, "presentationTime", presentationTime);
            SharedPreferencesWrapper.saveToPrefs(this, "taskRunning", taskRunning);
            sound.shutDownSoundPool();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        taskRunning = SharedPreferencesWrapper.getFromPrefs(this, "taskRunning", false);
        if (!firstStart && taskRunning) {
            //reinitialise the sounds
            sound = new Sound(this, 2);
            sound.loadSound(this, CORRECT_SOUND, R.raw.inspection_task_correct2, 1);
            sound.loadSound(this, WRONG_SOUND, R.raw.inspection_task_wrong2, 1);
            turnCounter = SharedPreferencesWrapper.getFromPrefs(this, "turnCounter", 0);
            presentationTime = SharedPreferencesWrapper.getFromPrefs(this, "presentationTime", presentationTime);
            firstStart = false;
            ResumeTaskDialogFragment resumeTaskDialogFragment = new ResumeTaskDialogFragment(
                    getString(R.string.activity_inspection_task_message_dialog),
                    getString(R.string.activity_inspection_task_pos_dialog),
                    getString(R.string.activity_inspection_task_neg_dialog),
                    getString(R.string.activity_inspection_task_neu_dialog)) {
                @Override
                public void posButtonClicked() {
                    runTask();
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
                    Intent intent = new Intent(InspectionTaskActivity.this, InspectionTaskActivity.class);
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
package scamell.michael.amulet;

/*
* Screen Slider Source : http://developer.android.com/training/animation/screen-slide.html
* */

import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.List;


public class TaskInstructionsActivity extends FragmentActivity {

    /**
     * The number of pages for each task instruction
     */
    private static final int INSPECTION_TASK_NUM_PAGES = 4;
    private static final int SEQUENCE_TASK_NUM_PAGES = 5;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private static ViewPager mPager;
    private final List<Fragment> fragmentList = new ArrayList<Fragment>();
    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    public static void setNull() {
        mPager.setAdapter(null);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_instructions);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        //lock to portrait for the duration of the instructions
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        String taskType = getIntent().getExtras().getString("task_type");

        //if this is their first time doing the task after logging in then show them instructions
        if (taskType.equals("inspection")) {
            fragmentList.add(0, new TasksFirstStartWelcomeFragment());
            fragmentList.add(1, new InspectionTaskInstructions1Fragment());
            fragmentList.add(2, new InspectionTaskInstructions2Fragment());

            Bundle args = new Bundle();
            args.putString("task_type", "inspection");
            Fragment lastTaskInstructionFragment = new LastTaskInstructionFragment() {
                @Override
                void onTaskStart() {
                    //closes the activity once started so user returns to main menu if back button
                    //is presses
                    finish();
                }
            };
            lastTaskInstructionFragment.setArguments(args);
            fragmentList.add(3, lastTaskInstructionFragment);
            mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), INSPECTION_TASK_NUM_PAGES);
        } else if (taskType.equals("sequence")) {
            fragmentList.add(0, new TasksFirstStartWelcomeFragment());
            fragmentList.add(1, new SequenceTaskInstruction1Fragment());
            fragmentList.add(2, new SequenceTaskInstruction2Fragment());
            fragmentList.add(3, new SequenceTaskInstruction3Fragment());

            //create bundle with which task is being started and send to the last taskInstructionFragment
            // to know which task to start
            Bundle args = new Bundle();
            args.putString("task_type", "sequence");
            Fragment lastTaskInstructionFragment = new LastTaskInstructionFragment() {
                @Override
                void onTaskStart() {
                    //closes the activity once started so user returns to main menu if back button
                    //is presses
                    finish();
                }
            };
            lastTaskInstructionFragment.setArguments(args);
            fragmentList.add(4, lastTaskInstructionFragment);

            mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager(), SEQUENCE_TASK_NUM_PAGES);
        }
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setOffscreenPageLimit(3);
        mPager.setAdapter(mPagerAdapter);
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    /**
     * A simple pager adapter that represents 4 ScreenSlidePageFragment objects, in
     * sequence.
     */
    public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        private final int numberOfPages;

        public ScreenSlidePagerAdapter(FragmentManager fm, int numberOfPages) {
            super(fm);
            this.numberOfPages = numberOfPages;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return numberOfPages;
        }

    }
}
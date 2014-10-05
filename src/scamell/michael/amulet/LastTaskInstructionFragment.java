package scamell.michael.amulet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public abstract class LastTaskInstructionFragment extends Fragment implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private static final String DEBUG_TAG = "Gestures";
    private GestureDetectorCompat mDetector;
    private String taskType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task_start, container, false);

        mDetector = new GestureDetectorCompat(getActivity(), this);
        mDetector.setOnDoubleTapListener(this);

        Bundle bundle = getArguments();
        taskType = bundle.getString("task_type");

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mDetector.onTouchEvent(event);
            }
        });

        return rootView;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.d(DEBUG_TAG, "onSingleTapUp: " + e.toString());
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (taskType.equals("inspection")) {
            startInspectionTask();
        } else if (taskType.equals("sequence")) {
            startSequenceTask();
        }
        return true;
    }

    private void startInspectionTask() {
        Intent intent = new Intent(getActivity(), InspectionTaskActivity.class);
        //set calibration to true, user doesn't have a choice as need to calibration if first start
        intent.putExtra("calibration_mode", true);
        startActivity(intent);
        onTaskStart();
    }

    private void startSequenceTask() {
        Intent intent = new Intent(getActivity(), SequenceTaskActivity.class);
        //set calibration to true, user doesn't have a choice as need to calibration if first start
        intent.putExtra("calibration_mode", true);
        startActivity(intent);
        onTaskStart();
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    abstract void onTaskStart();
}
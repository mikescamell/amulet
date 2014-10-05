package scamell.michael.amulet;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public abstract class TaskStartCountdownFragment extends Fragment {

    private TextView countDownTextView = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task_start_countdown, container, false);

        countDownTextView = (TextView) rootView.findViewById(R.id.countdown_textView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        new CountDownTimer(4000, 1000) {

            public void onTick(long millisUntilFinished) {
                countDownTextView.setText("" + millisUntilFinished / 1000);
            }

            public void onFinish() {
                countDownTextView.setText("Go!");
                onStartCountDownFinish();
            }
        }.start();

    }

    abstract protected void onStartCountDownFinish();

}

package com.sample.microsoft.stt.poc.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sample.microsoft.stt.R;
import com.sample.microsoft.stt.poc.BaseFragment;
import com.sample.microsoft.stt.poc.CognitiveServicesHelper;
import com.sample.microsoft.stt.poc.MicrosoftLandingActivity;
import com.sample.microsoft.stt.poc.data.POCApplication;
import com.sample.microsoft.stt.poc.ui.custom.EqualizerView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by sgarimella on 18/09/17.
 */

public class DictationFragment extends BaseFragment implements CognitiveServicesHelper.RecorderListener, View.OnClickListener {
    private final String TAG = "DictationFragment";

    private TextView mRecordedView;
    private StringBuilder mRecordedData;
    private TextView mTimerView;
    private ImageView mPauseView;
    private EqualizerView mEqualiser;
    private ImageView mDone;


    public int mRecordTimeInSecs = 59;
    public int mRecordTimeInMins = 0;
    private Timer mTimer = new Timer();

    private boolean mPaused = false;

    private final int MIN_BAR = 15;
    private final int MAX_BAR = 25;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dictation, null);
        initialiseViews(v);
        return v;
    }

    private void initialiseViews(View view) {
        mRecordedView = view.findViewById(R.id.recordeddata);
        mRecordedData = new StringBuilder();

        mRecordTimeInMins = (((POCApplication) getActivity().getApplication()).getRecordTime() - 1);
        mTimerView = view.findViewById(R.id.timer_text);

        String text = String.format(getResources().getString(R.string.mode_text), getResources().getString(R.string.dictation));
        ((TextView) view.findViewById(R.id.mode_text_title)).setText(text);

        String t = ((POCApplication) getActivity().getApplication()).getTitle();
        t = TextUtils.isEmpty(t) ? "Untitled" : t;
        String title = String.format(getResources().getString(R.string.title_text), t);
        ((TextView) view.findViewById(R.id.title_title_title)).setText(title);

        mPauseView = view.findViewById(R.id.pauserecord);
        mPauseView.setOnClickListener(this);
        view.findViewById(R.id.reset).setOnClickListener(this);
        mDone = view.findViewById(R.id.done);
        mDone.setOnClickListener(this);

        mEqualiser = view.findViewById(R.id.equalizer);
        mEqualiser.stopBars();
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MicrosoftLandingActivity) getActivity()).setActionBarTitle(getString(R.string.dictation_title));
        ((MicrosoftLandingActivity) getActivity()).enableBackButton();
    }

    @Override
    public void onResume() {
        super.onResume();
        resume();
    }

    @Override
    public void onPause() {
        pause();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void partial(byte state, String data) {
        mEqualiser.stopBars();
        mEqualiser.setBarCount(data.length());
        mEqualiser.animateBars();
        Log.v(TAG, "partial : " + data);
        mDone.setEnabled(false);
        mRecordedView.setText(mRecordedData.toString() + data);
    }

    @Override
    public void complete(byte state, String data) {
        if (TextUtils.isEmpty(data) || "null".equalsIgnoreCase(data)) {
            resetAudioListener();
            return;
        }
        mDone.setEnabled(true);
        mRecordedData.append(data);
        mRecordedData.append('\n');
        mRecordedView.setText(mRecordedData.toString());
        mRecordedView.setMovementMethod(new ScrollingMovementMethod());
        final int scrollAmount = mRecordedView.getLayout().getLineTop(mRecordedView.getLineCount()) - mRecordedView.getHeight();
        if (scrollAmount > 0) {
            mRecordedView.scrollTo(0, scrollAmount);
        } else {
            mRecordedView.scrollTo(0, 0);
        }
        mEqualiser.stopBars();
    }

    @Override
    public void error(byte state, String data) {
        Log.v(TAG, "error : " + data);
        mEqualiser.stopBars();
    }

    private void recordTimer() {
        //Set the schedule function and rate
        mTimer = new Timer();
        Clock clock = new Clock();
        mTimer.schedule(clock, 0, 1000);
    }

    private class Clock extends TimerTask {
        @Override
        public void run() {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String time = String.valueOf(mRecordTimeInMins) + ":" + String.valueOf(mRecordTimeInSecs);
                        String text = String.format(getResources().getString(R.string.time_remaining), time);
                        mTimerView.setText(text);
                        mRecordTimeInSecs -= 1;
                        if (mRecordTimeInSecs == 0) {
                            time = String.valueOf(mRecordTimeInMins) + ":" + String.valueOf(mRecordTimeInSecs);
                            text = String.format(getResources().getString(R.string.time_remaining), time);
                            mTimerView.setText(text);

                            mRecordTimeInSecs = 59;
                            mRecordTimeInMins = mRecordTimeInMins - 1;
                            if (mRecordTimeInMins < 0) {
                                done();
                            }
                        }
                    }
                });
            }
        }
    }

    public void reset() {
        mPaused = false;
        mPauseView.setImageResource(R.mipmap.ic_pause);

        mRecordedData = new StringBuilder();
        mRecordTimeInMins = (((POCApplication) getActivity().getApplication()).getRecordTime() - 1);
        mRecordTimeInSecs = 59;
        mRecordedView.setText(mRecordedData.toString());

        //pause and stop timer
        mTimer.cancel();
        mTimer.purge();

        resetAudioListener();
        // restart timer
        recordTimer();
    }

    private void resetAudioListener() {
        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().unRegisterRecorderListener();
        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().stopRecording();


        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().registerRecorderListener(this);
        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().startRecording();
    }

    public void pause() {
        mPaused = true;
        mPauseView.setImageResource(R.mipmap.ic_play);
        mTimer.cancel();
        mTimer.purge();
        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().unRegisterRecorderListener();
        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().stopRecording();

        mEqualiser.stopBars();
    }

    public void resume() {
        mPaused = false;
        mPauseView.setImageResource(R.mipmap.ic_pause);
        recordTimer();
        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().registerRecorderListener(this);
        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().startRecording();

        mEqualiser.stopBars();
    }

    public void done() {
        if (!TextUtils.isEmpty(mRecordedData.toString())) {
            ((POCApplication) getActivity().getApplication()).setRecordedText(mRecordedData.toString());
            ((MicrosoftLandingActivity) getActivity()).setFragment(new SemanticsValidationFragment());
        } else {
            ((MicrosoftLandingActivity) getActivity()).setFragment(new DocumentsListFragment());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.pauserecord:
                if (mPaused) {
                    resume();
                } else {
                    pause();
                }
                break;
            case R.id.reset:
                reset();
                break;
            case R.id.done:
                done();
                break;
        }
    }
}

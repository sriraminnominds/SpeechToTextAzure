package com.sample.microsoft.stt.poc.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sample.microsoft.stt.R;
import com.sample.microsoft.stt.poc.BaseFragment;
import com.sample.microsoft.stt.poc.CognitiveServicesHelper;
import com.sample.microsoft.stt.poc.MicrosoftLandingActivity;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by sgarimella on 18/09/17.
 */

public class DictationFragment extends BaseFragment implements CognitiveServicesHelper.RecorderListener {
    private final String TAG = "DictationFragment";

    private TextView mRecordedView;
    private StringBuilder mRecordedData;

    public int seconds = 60;
    public int minutes = 9;

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

        recordTimer(view);

        String text = String.format(getResources().getString(R.string.mode_text), getResources().getString(R.string.dictation));
        ((TextView) view.findViewById(R.id.mode_text_title)).setText(text);

        String title = String.format(getResources().getString(R.string.title_text), "Sample Title for Interview text wonder how it is done");
        ((TextView) view.findViewById(R.id.title_title_title)).setText(title);

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
        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().registerRecorderListener(this);
        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().startRecording();
    }

    @Override
    public void onPause() {
        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().unRegisterRecorderListener();
        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().stopRecording();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void partial(byte state, String data) {
        Log.v(TAG, "partial : " + data);
    }

    @Override
    public void complete(byte state, String data) {
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
        Log.v(TAG, "complete : " + data);
    }

    @Override
    public void error(byte state, String data) {
        Log.v(TAG, "error : " + data);
    }

    private void recordTimer(final View view) {
        Timer t = new Timer();
        //Set the schedule function and rate
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView tv = (TextView) view.findViewById(R.id.timer_text);
                        String time = String.valueOf(minutes) + ":" + String.valueOf(seconds);
                        String text = String.format(getResources().getString(R.string.time_remaining), time);
                        tv.setText(text);
                        seconds -= 1;
                        if (seconds == 0) {
                            time = String.valueOf(minutes) + ":" + String.valueOf(seconds);
                            text = String.format(getResources().getString(R.string.time_remaining), time);
                            tv.setText(text);

                            seconds = 60;
                            minutes = minutes - 1;
                        }
                    }
                });
            }
        }, 0, 1000);
    }
}

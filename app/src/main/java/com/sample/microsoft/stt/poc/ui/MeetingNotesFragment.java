package com.sample.microsoft.stt.poc.ui;

import android.support.v4.app.Fragment;

import com.sample.microsoft.stt.poc.CognitiveServicesHelper;
import com.sample.microsoft.stt.poc.MicrosoftLandingActivity;

/**
 * Created by sgarimella on 18/09/17.
 */

public class MeetingNotesFragment extends Fragment implements CognitiveServicesHelper.RecorderListener {

    @Override
    public void onResume() {
        super.onResume();
        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().registerRecorderListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().unRegisterRecorderListener();
    }

    @Override
    public void record(byte state, String data) {

    }
}

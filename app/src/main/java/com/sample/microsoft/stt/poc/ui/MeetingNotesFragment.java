package com.sample.microsoft.stt.poc.ui;

import android.support.v4.app.Fragment;

import com.sample.microsoft.stt.poc.BaseFragment;
import com.sample.microsoft.stt.poc.CognitiveServicesHelper;
import com.sample.microsoft.stt.poc.MicrosoftLandingActivity;

/**
 * Created by sgarimella on 18/09/17.
 */

public class MeetingNotesFragment extends BaseFragment implements CognitiveServicesHelper.RecorderListener {

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
    public void partial(byte state, String data) {

    }

    @Override
    public void complete(byte state, String data) {

    }

    @Override
    public void error(byte state, String data) {

    }
}

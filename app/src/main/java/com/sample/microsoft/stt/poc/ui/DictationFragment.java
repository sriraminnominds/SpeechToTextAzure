package com.sample.microsoft.stt.poc.ui;

import android.support.v4.app.Fragment;

import com.sample.microsoft.stt.poc.CognitiveServicesHelper;

/**
 * Created by sgarimella on 18/09/17.
 */

public class DictationFragment extends Fragment implements CognitiveServicesHelper.RecorderListener {

    @Override
    public void record(byte state, String data) {

    }
}

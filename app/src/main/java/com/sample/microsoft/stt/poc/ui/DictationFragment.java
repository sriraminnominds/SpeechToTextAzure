package com.sample.microsoft.stt.poc.ui;

import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sample.microsoft.stt.R;
import com.sample.microsoft.stt.poc.CognitiveServicesHelper;
import com.sample.microsoft.stt.poc.MicrosoftLandingActivity;
import com.sample.microsoft.stt.poc.ui.visualizer.MusicWave;

/**
 * Created by sgarimella on 18/09/17.
 */

public class DictationFragment extends Fragment implements CognitiveServicesHelper.RecorderListener {
    private final String TAG = "DictationFragment";
    private Visualizer mVisualizer;
    private MusicWave mMusicWave;
    private MediaPlayer mMediaPlayer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dictation, null);
        initialiseViews(v);
        return v;
    }

    private void initialiseViews(View view) {
        mMusicWave = view.findViewById(R.id.musicWave);
        prepareVisualizer();
    }

    private void prepareVisualizer() {
        mMediaPlayer = MediaPlayer.create(getActivity(), R.raw.unrelenting);
        mVisualizer = new Visualizer(mMediaPlayer.getAudioSessionId());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        mVisualizer.setDataCaptureListener(
                new Visualizer.OnDataCaptureListener() {
                    public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                        mMusicWave.updateVisualizer(bytes);
                    }

                    public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, false);
        mVisualizer.setEnabled(true);
        mMediaPlayer.start();
    }


    @Override
    public void onResume() {
        super.onResume();
        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().registerRecorderListener(this);
    }

    @Override
    public void onPause() {
        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().unRegisterRecorderListener();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mVisualizer.release();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    @Override
    public void record(byte state, String data) {

    }
}

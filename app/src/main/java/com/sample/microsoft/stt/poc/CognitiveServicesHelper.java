package com.sample.microsoft.stt.poc;

import android.app.Activity;
import android.util.Log;

import com.microsoft.cognitiveservices.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.cognitiveservices.speechrecognition.MicrophoneRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionResult;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionMode;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionServiceFactory;

/**
 * Created by sgarimella on 18/09/17.
 */

public class CognitiveServicesHelper implements ISpeechRecognitionServerEvents {
    private final String TAG = "CognitiveServicesHelper";
    private MicrophoneRecognitionClient mMicClient = null;

    public final byte RECORDER_STATE_ERROR = 0;
    public final byte RECORDER_STATE_PARTIAL = 1;
    public final byte RECORDER_STATE_FINAL = 2;

    private RecorderListener mRecListener;

    private String mReceivedData = null;

    public void initializeRecoClient(Activity context, String language, String subscriptionKey, SpeechRecognitionMode mode) {
        if (null == mMicClient) {
            mMicClient = SpeechRecognitionServiceFactory.createMicrophoneClient(context,
                    mode,
                    language,
                    this,
                    subscriptionKey);
        }

    }

    public void registerRecorderListener(RecorderListener listener) {
        this.mRecListener = listener;
    }

    public void unRegisterRecorderListener() {
        this.mRecListener = null;
    }

    @Override
    public void onPartialResponseReceived(String s) {
        Log.v(TAG, "onPartialResponseReceived :::::: " + s);
        mReceivedData = s;
        if (mRecListener != null) {
            mRecListener.record(RECORDER_STATE_PARTIAL, s);
        }
    }

    @Override
    public void onFinalResponseReceived(RecognitionResult recognitionResult) {
        Log.v(TAG, "onFinalResponseReceived :::::: " + recognitionResult.RecognitionStatus);
        if (mRecListener != null) {
            mRecListener.record(RECORDER_STATE_FINAL, mReceivedData);
        }
    }

    @Override
    public void onIntentReceived(String s) {
        Log.v(TAG, "onIntentReceived :::::: " + s);
    }

    @Override
    public void onError(int i, String s) {
        Log.v(TAG, "onError :::::: " + s+ " :: code::: "+i);
        if (mRecListener != null) {
            mRecListener.record(RECORDER_STATE_ERROR, s);
        }
    }

    @Override
    public void onAudioEvent(boolean b) {
        Log.v(TAG, "onAudioEvent :::::: " + b);
    }

    public void startRecording() {
        this.mMicClient.startMicAndRecognition();
    }

    public void stopRecording() {
        this.mMicClient.endMicAndRecognition();
    }

    public interface RecorderListener {
        void record(byte state, String data);
    }
}

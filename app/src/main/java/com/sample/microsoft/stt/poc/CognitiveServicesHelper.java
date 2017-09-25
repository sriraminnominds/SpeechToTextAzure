package com.sample.microsoft.stt.poc;

import android.app.Activity;
import android.text.TextUtils;
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

    public static final byte STATE_IDLE = 0;
    public static final byte STATE_LISTENING = 1;
    public static final byte STATE_ACTIVE_LISTENING = 2;
    public static final byte STATE_SYSTEM_ERROR = 3;
    public static final byte STATE_MICRO_PHONE_OFF = 4;
    private byte mCognitiveState = STATE_IDLE;

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
        mCognitiveState = STATE_IDLE;
    }

    public void registerRecorderListener(RecorderListener listener) {
        this.mRecListener = listener;
    }

    public void unRegisterRecorderListener() {
        this.mRecListener = null;
    }

    @Override
    public void onPartialResponseReceived(String s) {
        if (TextUtils.isEmpty(mReceivedData)) {
            mCognitiveState = STATE_LISTENING;
        } else {
            mCognitiveState = STATE_ACTIVE_LISTENING;
        }
        Log.v(TAG, "onPartialResponseReceived :::::: " + s + " mCognitiveState : " + mCognitiveState);
        mReceivedData = s;
        if (mRecListener != null) {
            mRecListener.partial(mCognitiveState, s);
        }
    }

    @Override
    public void onFinalResponseReceived(RecognitionResult recognitionResult) {
        Log.v(TAG, "onFinalResponseReceived :::::: " + recognitionResult.RecognitionStatus);
        if (mRecListener != null) {
            mRecListener.complete(mCognitiveState, mReceivedData);
        }
        mCognitiveState = STATE_IDLE;
        mReceivedData = null;
    }

    @Override
    public void onIntentReceived(String s) {
        Log.v(TAG, "onIntentReceived :::::: " + s);
    }

    @Override
    public void onError(int i, String s) {
        Log.v(TAG, "onError :::::: " + s + " :: code::: " + i);
        mCognitiveState = STATE_SYSTEM_ERROR;
        if (mRecListener != null) {
            mRecListener.error(mCognitiveState, s);
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
        void partial(byte state, String data);

        void complete(byte state, String data);

        void error(byte state, String data);
    }

    public byte getCognitiveState() {
        return mCognitiveState;
    }

    public void setCognitiveState(byte mCognitiveState) {
        this.mCognitiveState = mCognitiveState;
    }
}

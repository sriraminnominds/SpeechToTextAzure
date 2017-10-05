package com.sample.microsoft.stt.poc.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sample.microsoft.stt.R;
import com.sample.microsoft.stt.poc.BaseFragment;
import com.sample.microsoft.stt.poc.CognitiveServicesHelper;
import com.sample.microsoft.stt.poc.MicrosoftLandingActivity;
import com.sample.microsoft.stt.poc.socket.SocketHelper;
import com.sample.microsoft.stt.poc.socket.SocketRequestContract;
import com.sample.microsoft.stt.poc.socket.SocketResponseContract;
import com.sample.microsoft.stt.poc.ui.custom.EqualizerView;
import com.sample.microsoft.stt.poc.utils.AppUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by sgarimella on 18/09/17.
 */

public class MeetingNotesFragment extends BaseFragment implements CognitiveServicesHelper.RecorderListener {
    private final String TAG = "MeetingNotesFragment";

    private SocketRequestContract mSocketRequest;
    private EqualizerView mEqualiser;

    private String mMeetingId = "11538";
    public boolean mIsOrganiser = false;
    public String mAttendeeName = "Rahul";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dictation, null);
        initialiseViews(v);
        return v;
    }

    private void initialiseViews(View view) {
        mEqualiser = view.findViewById(R.id.equalizer);
        mEqualiser.stopBars();
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MicrosoftLandingActivity) getActivity()).setActionBarTitle("Meeting Notes");
        ((MicrosoftLandingActivity) getActivity()).enableBackButton();

        ((MicrosoftLandingActivity) getActivity()).initialiseCognitiveServices();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSocketRequest = new SocketHelper();
        mSocketRequest.initialise("http://192.168.21.126:3000/", mSocketResp);

        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().registerRecorderListener(this);
        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().startRecording();
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().unRegisterRecorderListener();
        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().stopRecording();

        sendMeetingMsg(false);
        //mSocketRequest.closeAndDisconnectSocket();
    }

    @Override
    public void partial(byte state, String data) {
        //Log.v(TAG, "Azure : partial Message : " + data);
        if (TextUtils.isEmpty(data) || "null".equalsIgnoreCase(data)) {
            resetAudioListener();
            return;
        }
        mEqualiser.stopBars();
        mEqualiser.setBarCount(data.length());
        mEqualiser.animateBars();
    }

    @Override
    public void complete(byte state, String data) {
        sendMessage(data);
        mEqualiser.stopBars();
    }

    @Override
    public void error(byte state, String data) {
        //Log.v(TAG, "Azure : error Message : " + data);
        mEqualiser.stopBars();
    }

    private void resetAudioListener() {
        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().unRegisterRecorderListener();
        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().stopRecording();


        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().registerRecorderListener(this);
        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().startRecording();
    }

    private void sendMeetingMsg(boolean isStart) {
        try {
            if (mSocketRequest.isSocketConnected()) {
                JSONObject jMsg = new JSONObject();
                jMsg.put("meetingId", !mIsOrganiser ? mMeetingId : "");
                jMsg.put("userId", AppUtils.getDeviceUniqueId(getActivity()));
                jMsg.put("userName", mAttendeeName);
                jMsg.put("isOrganizer", mIsOrganiser);
                jMsg.put("timestamp", new Date().getTime());
                if (isStart) {
                    mSocketRequest.emitMessage("startmeeting", jMsg);
                } else {
                    mSocketRequest.emitMessage("endmeeting", jMsg);
                }
            } else {
                Log.v(TAG, "Socket Not Connected");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String msg) {
        try {
            if (mSocketRequest.isSocketConnected()) {
                JSONObject jMsg = new JSONObject();
                jMsg.put("meetingId", mMeetingId);
                jMsg.put("userId", AppUtils.getDeviceUniqueId(getActivity()));
                jMsg.put("userName", mAttendeeName);
                jMsg.put("message", msg);
                jMsg.put("timestamp", new Date().getTime());
                mSocketRequest.emitMessage("addnotes", jMsg);
            } else {
                Log.v(TAG, "Socket Not Connected");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    SocketResponseContract mSocketResp = new SocketResponseContract() {
        @Override
        public void onConnect() {
            Log.v(TAG, "Socket : onConnect ");
            sendMeetingMsg(true);
        }

        @Override
        public void onSocketFailed() {
            Log.v(TAG, "Socket : onSocketFailed ");
        }

        @Override
        public void onLoginWithSocket() {
            Log.v(TAG, "Socket : onLoginWithSocket ");
        }

        @Override
        public void onUserLeft(String user) {
            Log.v(TAG, "Socket : onUserLeft " + user);
        }

        @Override
        public void onMessageReceived(String message) {
            Log.v(TAG, "Socket : onMessageReceived " + message);
        }

        @Override
        public void onNewUser(Object... args) {
            Log.v(TAG, "Socket : onNewUser ");
        }

        @Override
        public void onSocketError(int code) {
            Log.v(TAG, "Socket : onSocketError " + code);
        }

        @Override
        public void onMeetingStarted(String message) {
            Log.v(TAG, "Socket : onMeetingStarted " + message);
            try {
                JSONObject data = new JSONObject(message);
                mMeetingId = data.getString("meetingId");
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onMeetingEnd(String message) {
            Log.v(TAG, "Socket : onMeetingEnd " + message);
            mSocketRequest.closeAndDisconnectSocket();
        }
    };
}

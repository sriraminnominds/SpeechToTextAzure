package com.sample.microsoft.stt.poc.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
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

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sgarimella on 18/09/17.
 */

public class MeetingNotesFragment extends BaseFragment implements CognitiveServicesHelper.RecorderListener {
    private final String TAG = "MeetingNotesFragment";

    private SocketRequestContract mSocketRequest;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dictation, null);
        initialiseViews(v);
        return v;
    }

    private void initialiseViews(View v) {

    }

    @Override
    public void onStart() {
        super.onStart();
        ((MicrosoftLandingActivity) getActivity()).setActionBarTitle("Meeting Notes");
        ((MicrosoftLandingActivity) getActivity()).enableBackButton();

        ((MicrosoftLandingActivity) getActivity()).initialiseCognitiveServices();
        mSocketRequest = new SocketHelper();
        mSocketRequest.initialise("http://192.168.21.126:3000/", mSocketResp);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().registerRecorderListener(this);
        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().startRecording();
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().unRegisterRecorderListener();
        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().stopRecording();
    }

    @Override
    public void partial(byte state, String data) {
        Log.v(TAG, "Azure : partial Message : " + data);
    }

    @Override
    public void complete(byte state, String data) {
        try {
            Log.v(TAG, "Azure : complete Message : " + data);
            JSONObject msg = new JSONObject();
            msg.put("message", data);
            mSocketRequest.emitMessage("chat message", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void error(byte state, String data) {
        Log.v(TAG, "Azure : error Message : " + data);
    }


    SocketResponseContract mSocketResp = new SocketResponseContract() {
        @Override
        public void onConnect() {
            Log.v(TAG, "Socket : onConnect ");
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
        public void onTyping(String typing) {
            Log.v(TAG, "Socket : onTyping " + typing);
        }

        @Override
        public void onMessageReceived(String message) {
            Log.v(TAG, "Socket : onMessageReceived " + message);
        }

        @Override
        public void onMessagesUpdated(String messages) {
            Log.v(TAG, "Socket : onMessagesUpdated " + messages);
        }

        @Override
        public void onNewUser(Object... args) {
            Log.v(TAG, "Socket : onNewUser ");
        }

        @Override
        public void onSocketError(int code) {
            Log.v(TAG, "Socket : onSocketError " + code);
        }
    };
}

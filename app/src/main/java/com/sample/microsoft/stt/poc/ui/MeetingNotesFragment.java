package com.sample.microsoft.stt.poc.ui;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sample.microsoft.stt.R;
import com.sample.microsoft.stt.poc.BaseFragment;
import com.sample.microsoft.stt.poc.CognitiveServicesHelper;
import com.sample.microsoft.stt.poc.MicrosoftLandingActivity;
import com.sample.microsoft.stt.poc.data.MeetingNotes;
import com.sample.microsoft.stt.poc.socket.SocketHelper;
import com.sample.microsoft.stt.poc.socket.SocketRequestContract;
import com.sample.microsoft.stt.poc.socket.SocketResponseContract;
import com.sample.microsoft.stt.poc.ui.custom.EqualizerView;
import com.sample.microsoft.stt.poc.utils.AppUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by sgarimella on 18/09/17.
 */

public class MeetingNotesFragment extends BaseFragment implements CognitiveServicesHelper.RecorderListener, View.OnClickListener {
    private final String TAG = "MeetingNotesFragment";

    private SocketRequestContract mSocketRequest;
    private EqualizerView mEqualiser;

    private Dialog mDialog;

    private String mMeetingId = "";
    public boolean mIsOrganiser = false;
    public String mAttendeeName = "";

    private TextView mMeetingIdTV;
    private TextView mAttendeeTV;
    private LinearLayout mDone;
    private TextView mRecordedView;
    private StringBuilder mRecordedData;

    private static final String JSON_MEETING_ID = "meetingId";
    private static final String JSON_USER_ID = "userId";
    private static final String JSON_USER_NAME = "userName";
    private static final String JSON_IS_ORGANISER = "isOrganizer";
    private static final String JSON_TIMESTAMP = "timestamp";
    private static final String JSON_MESSAGE = "message";

    private static final String API_END_POINT = "http://meeting-dictate.azurewebsites.net/";

    private final int REQUEST_MICROPHONE = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_meeting_notes, null);
        initialiseViews(v);
        return v;
    }

    private void initialiseViews(View view) {
        mEqualiser = view.findViewById(R.id.equalizer);
        mEqualiser.stopBars();

        String text = String.format(getResources().getString(R.string.mode_meeting), mMeetingId);
        mMeetingIdTV = (TextView) view.findViewById(R.id.mode_text_title);
        mMeetingIdTV.setText(text);

        String userName = String.format(getResources().getString(R.string.mode_meeting_name), mAttendeeName);
        mAttendeeTV = (TextView) view.findViewById(R.id.title_title_title);
        mAttendeeTV.setText(userName);

        view.findViewById(R.id.timer_text).setVisibility(View.GONE);

        mDone = view.findViewById(R.id.donerecord);
        mDone.setOnClickListener(this);

        mRecordedView = view.findViewById(R.id.recordeddata);
        mRecordedData = new StringBuilder();

        ((MicrosoftLandingActivity) getActivity()).initialiseCognitiveServices();
        requestForPermissions();
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MicrosoftLandingActivity) getActivity()).setActionBarTitle(getString(R.string.mode_meeting_notes));
        ((MicrosoftLandingActivity) getActivity()).enableBackButton();

        mSocketRequest = new SocketHelper();
        mSocketRequest.initialise(API_END_POINT, mSocketResp);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        sendMeetingMsg(false);
        mMeetingId = null;
        mAttendeeName = null;
    }

    @Override
    public void partial(byte state, String data) {
        if (TextUtils.isEmpty(data) || "null".equalsIgnoreCase(data)) {
            resetAudioListener();
            return;
        }
        mEqualiser.stopBars();
        mEqualiser.setBarCount(data.length());
        mEqualiser.animateBars();

        mDone.setEnabled(false);
        mRecordedView.setText(mRecordedData.toString() + data);
    }

    @Override
    public void complete(byte state, String data) {
        if (TextUtils.isEmpty(data) || "null".equalsIgnoreCase(data)) {
            resetAudioListener();
            return;
        }
        sendMessage(data);

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
        Log.v(TAG, "Azure : error Message : " + data);
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
                jMsg.put(JSON_MEETING_ID, mMeetingId);
                jMsg.put(JSON_USER_ID, AppUtils.getDeviceUniqueId(getActivity()));
                jMsg.put(JSON_USER_NAME, mAttendeeName);
                jMsg.put(JSON_IS_ORGANISER, mIsOrganiser);
                jMsg.put(JSON_TIMESTAMP, new Date().getTime());
                if (isStart) {
                    mSocketRequest.emitMessage("startmeeting", jMsg);

                    ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().registerRecorderListener(this);
                    ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().startRecording();
                } else {
                    mSocketRequest.emitMessage("endmeeting", jMsg);

                    ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().unRegisterRecorderListener();
                    ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().stopRecording();
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
            if (mSocketRequest.isSocketConnected() && !TextUtils.isEmpty(msg)) {
                JSONObject jMsg = new JSONObject();
                jMsg.put(JSON_MEETING_ID, mMeetingId);
                jMsg.put(JSON_USER_ID, AppUtils.getDeviceUniqueId(getActivity()));
                jMsg.put(JSON_USER_NAME, mAttendeeName);
                jMsg.put(JSON_MESSAGE, msg);
                jMsg.put(JSON_TIMESTAMP, new Date().getTime());
                mSocketRequest.emitMessage("addnotes", jMsg);
            } else {
                Log.v(TAG, "Socket Not Connected");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showMeetingNotesDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            return;
        }
        mDialog = new Dialog(getActivity());
        mDialog.setContentView(R.layout.view_meeting_notes_dialog);
        mDialog.setCancelable(false);

        final EditText userNameET = (EditText) mDialog.findViewById(R.id.input_et_userName);
        final EditText meetingIdET = (EditText) mDialog.findViewById(R.id.input_et_meeting_id);
        final TextInputLayout meetingIdLayout = (TextInputLayout) mDialog.findViewById(R.id.input_layout_meeting_id);
        final CheckBox isOrganiserCB = mDialog.findViewById(R.id.input_cb_isorganiser);
        isOrganiserCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    meetingIdLayout.setVisibility(View.GONE);
                } else {
                    meetingIdLayout.setVisibility(View.VISIBLE);
                }
            }
        });
        TextView acceptButton = (TextView) mDialog.findViewById(R.id.dialog_ok);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();

                mMeetingId = meetingIdET.getText().toString();
                mIsOrganiser = isOrganiserCB.isChecked();
                mAttendeeName = userNameET.getText().toString();

                ((MicrosoftLandingActivity) getActivity()).getData().setMeetingId(mMeetingId);
                ((MicrosoftLandingActivity) getActivity()).getData().setAttendeeName(mAttendeeName);
                ((MicrosoftLandingActivity) getActivity()).getData().setOrganiser(mIsOrganiser);

                sendMeetingMsg(true);
            }
        });
        mDialog.show();
    }

    private void showConfirmationDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            return;
        }
        mDialog = new Dialog(getActivity());
        mDialog.setContentView(R.layout.view_share_meeting_id_dialog);

        final TextView dataTv = (TextView) mDialog.findViewById(R.id.dialog_data);
        dataTv.setText(getString(R.string.meeting_notes_problem));
        mDialog.setCancelable(false);
        mDialog.show();
        TextView acceptButton = (TextView) mDialog.findViewById(R.id.next);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                getActivity().onBackPressed();
            }
        });
    }

    SocketResponseContract mSocketResp = new SocketResponseContract() {
        @Override
        public void onConnect() {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (TextUtils.isEmpty(mMeetingId)) {
                            if (mDialog == null || !mDialog.isShowing()) {
                                showMeetingNotesDialog();
                            }
                        }
                    }
                });
            }
        }

        @Override
        public void onSocketFailed() {
            if (getActivity() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showConfirmationDialog();
                    }
                });
            }
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
                mMeetingId = data.optString(JSON_MEETING_ID);
                ((MicrosoftLandingActivity) getActivity()).getData().setMeetingId(mMeetingId);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String text = String.format(getResources().getString(R.string.mode_meeting), mMeetingId);
                            mMeetingIdTV.setText(text);

                            String userName = String.format(getResources().getString(R.string.mode_meeting_name), mAttendeeName);
                            mAttendeeTV.setText(userName);
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onMeetingEnd(String message) {
            Log.v(TAG, "Socket : onMeetingEnd " + message);
            createTranscripts(message);
            mSocketRequest.closeAndDisconnectSocket();
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.donerecord:
                done();
                break;
        }
    }

    public void done() {
        sendMeetingMsg(false);
        mMeetingId = null;
        mAttendeeName = null;

        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().unRegisterRecorderListener();
        ((MicrosoftLandingActivity) this.getActivity()).getSpeechHelper().stopRecording();
    }

    private void createTranscripts(String message) {
        try {
            List<MeetingNotes> list = new ArrayList<>();
            JSONArray jTranscripts = new JSONArray(message);
            for (int i = 0; i < jTranscripts.length(); i++) {
                JSONObject jObj = jTranscripts.getJSONObject(i);
                String mId = jObj.optString(JSON_MEETING_ID);
                String userId = jObj.optString(JSON_USER_ID);
                String userName = jObj.optString(JSON_USER_NAME);
                String msg = jObj.optString(JSON_MESSAGE);
                long timeStamp = jObj.optLong(JSON_TIMESTAMP);
                boolean isOrg = jObj.optBoolean(JSON_IS_ORGANISER);

                MeetingNotes m = new MeetingNotes(mId, userId, userName, msg, isOrg, new Date(timeStamp));
                list.add(m);
            }
            if (getActivity() != null && !isHidden()) {
                ((MicrosoftLandingActivity) getActivity()).getData().setNotes(list);
                ((MicrosoftLandingActivity) getActivity()).setFragment(new MeetingTranscriptsFragment());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void requestForPermissions() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.RECORD_AUDIO)) {
                ((MicrosoftLandingActivity) getActivity()).initialiseCognitiveServices();
                resetAudioListener();
            } else {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MICROPHONE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_MICROPHONE:
                ((MicrosoftLandingActivity) getActivity()).initialiseCognitiveServices();
                resetAudioListener();
                break;
        }
    }
}

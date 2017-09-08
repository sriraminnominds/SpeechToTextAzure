package com.sample.microsoft.stt;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.cognitiveservices.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.cognitiveservices.speechrecognition.MicrophoneRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionResult;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionMode;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionServiceFactory;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by sgarimella on 07/09/17.
 */

public class ScrumMeetingActivity extends Activity implements ISpeechRecognitionServerEvents, View.OnClickListener {
    private final String TAG = "ScrumMeetingActivity";
    private MicrophoneRecognitionClient m_micClient = null;
    private TextView m_meetingNotes;
    private StringBuilder m_meetingNotesData;
    private String m_receivedData;
    String[] participants = {
            "Scrum Master",
            "Developer 1",
            "Developer 2",
            "Tester 1",
            "Tester 2",
            "Designer"
    };
    private boolean isMeetingStarted = false;
    private GridView m_grid;
    private final int REQUEST_MICROPHONE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrum_meeting);

        initializeViews();
        requestForPermissions();
    }

    @Override
    public void onPartialResponseReceived(String s) {
        Log.v(TAG, "onPartialResponseReceived :::::: " + s);
        m_receivedData = s;
    }

    @Override
    public void onFinalResponseReceived(RecognitionResult recognitionResult) {
        setNotes(m_receivedData);
    }

    @Override
    public void onIntentReceived(String s) {
        Log.v(TAG, "onIntentReceived :::::: " + s);
    }

    @Override
    public void onError(int i, String s) {
        Log.v(TAG, "onError :::::: " + s);
        setNotes("***** Error in recording data *****");
    }

    @Override
    public void onAudioEvent(boolean b) {
        Log.v(TAG, "onAudioEvent :::::: " + b);
    }

    private void initializeViews() {
        CustomGrid adapter = new CustomGrid(ScrumMeetingActivity.this, participants);
        m_grid = (GridView) findViewById(R.id.participants_grid);
        m_grid.setAdapter(adapter);
        m_grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setNotes(participants[position] + " at : " + getDate());
                setSelectedParticipant(position);
            }
        });

        findViewById(R.id.meeting_controls_start).setOnClickListener(this);
        findViewById(R.id.meeting_controls_end).setOnClickListener(this);

        findViewById(R.id.meeting_controls_start).setEnabled(true);
        findViewById(R.id.meeting_controls_end).setEnabled(false);

        m_meetingNotes = (TextView) findViewById(R.id.meeting_notes);
    }

    private void requestForPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_MICROPHONE);
        }
    }

    private void setSelectedParticipant(int position) {
        if (!isMeetingStarted) {
            return;
        }
        for (int i = 0; i < participants.length; i++) {
            View view = m_grid.getChildAt(i);
            if (position == i) {
                view.setBackgroundColor(getColorWrapper(this, R.color.selected));
            } else {
                view.setBackgroundColor(getColorWrapper(this, R.color.white));
            }
        }
    }

    public static int getColorWrapper(Context context, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getColor(id);
        } else {
            //noinspection deprecation
            return context.getResources().getColor(id);
        }
    }


    void initializeRecoClient() {
        String language = "en-us";
        String subscriptionKey = this.getString(R.string.primaryKey);
        if (null == m_micClient) {
            m_micClient = SpeechRecognitionServiceFactory.createMicrophoneClient(this,
                    SpeechRecognitionMode.LongDictation,
                    language,
                    this,
                    subscriptionKey);

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.meeting_controls_start:
                isMeetingStarted = true;
                findViewById(R.id.meeting_controls_start).setEnabled(false);
                findViewById(R.id.meeting_controls_end).setEnabled(true);
                setSelectedParticipant(0);

                setNotes("*********************");
                setNotes("Meeting Started at : " + getDate());
                setNotes(participants[0] + " at : " + getDate());

                initializeRecoClient();
                this.m_micClient.startMicAndRecognition();
                break;
            case R.id.meeting_controls_end:
                isMeetingStarted = false;
                findViewById(R.id.meeting_controls_start).setEnabled(true);
                findViewById(R.id.meeting_controls_end).setEnabled(false);
                setSelectedParticipant(-1);

                setNotes("Meeting Ended at : " + getDate());
                setNotes("*********************");
                this.m_micClient.endMicAndRecognition();
                shareRecordedData(m_meetingNotesData.toString());
                break;
        }
    }

    private void setNotes(String notes) {
        if (!isMeetingStarted) {
            return;
        }
        if (m_meetingNotesData == null) {
            m_meetingNotesData = new StringBuilder();
        }
        m_meetingNotesData.append(notes);
        m_meetingNotesData.append("\n");
        m_meetingNotes.setMovementMethod(new ScrollingMovementMethod());
        m_meetingNotes.setText(m_meetingNotesData.toString());
    }

    private String getDate() {
        Date date = new Date();
        return DateFormat.getDateTimeInstance().format(date);
    }

    private void shareRecordedData(String data) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Meeting Notes");
        shareIntent.setType("*/*");
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.putExtra(Intent.EXTRA_TEXT, data);
        startActivity(Intent.createChooser(shareIntent, "Select App to Share Text and Image"));
    }

    public static class CustomGrid extends BaseAdapter {
        private Context mContext;
        private final String[] participants;

        public CustomGrid(Context c, String[] participants) {
            mContext = c;
            this.participants = participants;
        }

        @Override
        public int getCount() {
            return participants.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View grid;
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null) {
                grid = new View(mContext);
                grid = inflater.inflate(R.layout.layout_grid_item, null);
                TextView textView = (TextView) grid.findViewById(R.id.grid_item);
                textView.setText(participants[position]);
            } else {
                grid = (View) convertView;
            }
            grid.setBackgroundColor(Color.WHITE);
            return grid;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MICROPHONE && resultCode == RESULT_OK) {

        } else {
            Toast.makeText(this, "Please enable Microphone permissions.", Toast.LENGTH_SHORT).show();
        }
    }
}

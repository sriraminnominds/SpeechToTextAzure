package com.sample.microsoft.stt;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
            "Developer",
            "Tester",
            "Designer"
    };
    private boolean isMeetingStarted = false;
    private GridView m_grid;
    private final int REQUEST_MICROPHONE = 1;
    private final String m_fileName = "Meeting Notes";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrum_meeting);

        initializeViews();
        requestForPermissions();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onPartialResponseReceived(String s) {
        Log.v(TAG, "onPartialResponseReceived :::::: " + s);
        ((TextView) findViewById(R.id.meeting_notes_analyser)).setText(s);
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

                m_meetingNotesData = new StringBuilder();
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
                isStoragePermissionGranted();
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
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"email@example.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Meeting notes " + getDate());
        intent.putExtra(Intent.EXTRA_TEXT, m_meetingNotesData.toString());
        String fpath = Environment.getExternalStorageDirectory() + File.separator + m_fileName + ".txt";
        Log.v(TAG, fpath);
        File file = new File(fpath);
        if (!file.exists() || !file.canRead()) {
            Toast.makeText(this, "Attachment Error", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri uri = Uri.fromFile(file);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(intent, "Send email..."));
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

    public Boolean write(String fname, String fcontent) {
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {

            String fpath = Environment.getExternalStorageDirectory() + File.separator + fname + ".txt";
            Log.v(TAG, fpath);
            File file = new File(fpath);
            // If file does not exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            } else {
                file.delete();
            }
            fw = new FileWriter(file.getAbsoluteFile());
            bw = new BufferedWriter(fw);
            bw.write(fcontent);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                    bw.close();
                } catch (IOException e) {

                }
            }
        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                writeAndShare();
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            writeAndShare();
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            writeAndShare();
        }
    }

    private void writeAndShare(){
        String data = m_meetingNotesData.toString();
        write(m_fileName, data);
        shareRecordedData(data);
    }
}

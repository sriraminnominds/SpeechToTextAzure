package com.sample.microsoft.stt.poc.data;

import android.app.Application;

/**
 * Created by sgarimella on 26/09/17.
 */

public class POCApplication extends Application {
    private int mRecordTime = 0;
    private String mTitle;
    private String mRecordedText;
    private String mAttendeeName;
    private boolean isOrganiser;
    private String mMeetingId;

    public int getRecordTime() {
        return mRecordTime;
    }

    public void setRecordTime(int mRecordTime) {
        this.mRecordTime = mRecordTime;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getRecordedText() {
        return mRecordedText;
    }

    public void setRecordedText(String mRecordedText) {
        this.mRecordedText = mRecordedText;
    }

    public String getAttendeeName() {
        return mAttendeeName;
    }

    public void setAttendeeName(String name) {
        this.mAttendeeName = name;
    }

    public boolean isOrganiser() {
        return isOrganiser;
    }

    public void setOrganiser(boolean organiser) {
        isOrganiser = organiser;
    }

    public String getMeetingId() {
        return mMeetingId;
    }

    public void setMeetingId(String meetingId) {
        this.mMeetingId = meetingId;
    }

    public void clearDictation() {
        mRecordTime = 0;
        mTitle = "";
    }
}

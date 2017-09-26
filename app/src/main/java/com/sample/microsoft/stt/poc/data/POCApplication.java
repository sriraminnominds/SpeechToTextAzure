package com.sample.microsoft.stt.poc.data;

import android.app.Application;

/**
 * Created by sgarimella on 26/09/17.
 */

public class POCApplication extends Application {
    private int mRecordTime = 0;
    private String mTitle;

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

    public void clear() {
        mRecordTime = 0;
        mTitle = "";
    }
}

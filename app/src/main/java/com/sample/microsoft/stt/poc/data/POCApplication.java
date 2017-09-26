package com.sample.microsoft.stt.poc.data;

import android.app.Application;

/**
 * Created by sgarimella on 26/09/17.
 */

public class POCApplication extends Application {
    private int mRecordTime;

    public int getRecordTime() {
        return mRecordTime;
    }

    public void setRecordTime(int mRecordTime) {
        this.mRecordTime = mRecordTime;
    }
}

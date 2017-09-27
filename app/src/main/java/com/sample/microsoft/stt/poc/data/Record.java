package com.sample.microsoft.stt.poc.data;

import android.support.annotation.NonNull;

import java.util.Date;

/**
 * Created by sgarimella on 27/09/17.
 */

public class Record implements Comparable<Record> {
    private String recordName;
    private Date lastModifiedDate;
    private String path;

    public Record(String recordName, String path, Date lastModifiedDate) {
        this.recordName = recordName;
        this.lastModifiedDate = lastModifiedDate;
        this.path = path;
    }

    public String getRecordName() {
        return recordName;
    }

    public void setRecordName(String recordName) {
        this.recordName = recordName;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "Record{" +
                "recordName='" + recordName + '\'' +
                ", lastModifiedDate=" + lastModifiedDate +
                ", path='" + path + '\'' +
                '}';
    }


    @Override
    public int compareTo(@NonNull Record record) {
        return getLastModifiedDate().compareTo(record.getLastModifiedDate());
    }
}

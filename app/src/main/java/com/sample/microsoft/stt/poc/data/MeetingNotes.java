package com.sample.microsoft.stt.poc.data;

import java.util.Date;

/**
 * Created by sgarimella on 05/10/17.
 */

public class MeetingNotes {
    private String meetingId;
    private String userId;
    private String userName;
    private boolean isOrganiser;
    private Date date;
    private String msg;

    public MeetingNotes(String meetingId, String userId, String userName, String msg, boolean isOrganiser, Date date) {
        this.meetingId = meetingId;
        this.userId = userId;
        this.userName = userName;
        this.msg = msg;
        this.isOrganiser = isOrganiser;
        this.date = date;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isOrganiser() {
        return isOrganiser;
    }

    public void setOrganiser(boolean organiser) {
        isOrganiser = organiser;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "MeetingNotes{" +
                "meetingId='" + meetingId + '\'' +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", isOrganiser=" + isOrganiser +
                ", date=" + date +
                ", msg='" + msg + '\'' +
                '}';
    }
}

package com.exo.scomm.model;

import androidx.annotation.NonNull;

import java.util.Date;

public class Notification {
    private  String type;
    private String fromUser;
    private String toUser;
    private long date;
    private String  task_id;

    public Notification() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getTask_id() {
        return task_id;
    }

    public void setTask_id(String task_id) {
        this.task_id = task_id;
    }

    public String getFromUser() {
        return fromUser;
    }

    public void setFromUser(String fromUser) {
        this.fromUser = fromUser;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }
}

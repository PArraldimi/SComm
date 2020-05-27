package com.exo.scomm.model;

import java.util.Date;

public class Task {
    private String title;
    private String description;
    private String type;
    private String date;
    private  String taskOwner;

    public Task() {
    }

    public Task(String title, String description, String type, String date, String taskOwner) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.date = date;
        this.taskOwner = taskOwner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getDate() {
        return date;
    }

    public void getDate(String remind_me_at) {
        this.date = date;
    }

    public String getTaskOwner() {
        return taskOwner;
    }

    public void setTaskOwner(String taskOwner) {
        this.taskOwner = taskOwner;
    }
}
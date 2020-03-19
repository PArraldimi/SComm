package com.exo.scomm;

import java.util.Date;

public class TasksModel {
    public String date, description, task_owner, title, type;

    public TasksModel(){
    }

    public TasksModel(String date, String description, String task_owner, String title, String type) {
        this.date = date;
        this.description = description;
        this.task_owner = task_owner;
        this.title = title;
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTask_owner() {
        return task_owner;
    }

    public void setTask_owner(String task_owner) {
        this.task_owner = task_owner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}


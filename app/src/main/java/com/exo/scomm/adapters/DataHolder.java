package com.exo.scomm.adapters;

import com.exo.scomm.model.TasksModel;
import com.exo.scomm.model.User;

import java.util.List;

public class DataHolder {
    private static String UID;
    private static String taskId;
    private static TasksModel task;
    private static String phone;
    private static String currentUID;
    private static List<TasksModel> todayTasks;
    private static List<User>  userList;

    public static String getTaskId() {
        return taskId;
    }

    public static void setTaskId(String taskId) {
        DataHolder.taskId = taskId;
    }

    public static String getPhone() {
        return phone;
    }

    public static void setPhone(String phone) {
        DataHolder.phone = phone;
    }

    public static String getCurrentUID() {
        return currentUID;
    }

    public static void setCurrentUID(String currentUID) {
        DataHolder.currentUID = currentUID;
    }

    public static List<TasksModel> getTodayTasks() {
        return todayTasks;
    }

    public static void setTodayTasks(List<TasksModel> todayTasks) {
        DataHolder.todayTasks = todayTasks;
    }

    public static List<User> getUserList() {
        return userList;
    }

    public static void setUserList(List<User> userList) {
        DataHolder.userList = userList;
    }

    public static TasksModel getTask() {
        return task;
    }

    public static void setTask(TasksModel task) {
        DataHolder.task = task;
    }

    public static String getUID() {
        return UID;
    }

    public static void setUID(String UID) {
        DataHolder.UID = UID;
    }
}

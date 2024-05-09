package com.example.taskify.Model;

import java.io.Serializable;

public class TaskModel implements Serializable {
    String taskId, taskName, taskDeadline, taskColor, userID;
    int taskOrder;

    public TaskModel(){

    }

    public TaskModel(String taskId, String taskName, String taskDeadline, String taskColor, String userID) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.taskDeadline = taskDeadline;
        this.taskColor = taskColor;
        this.userID = userID;
    }


    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDeadline() {
        return taskDeadline;
    }

    public void setTaskDeadline(String taskDeadline) {
        this.taskDeadline = taskDeadline;
    }

    public int getTaskOrder() {
        return taskOrder;
    }

    public void setTaskOrder(int taskOrder) {
        this.taskOrder = taskOrder;
    }

    public String getTaskColor() {
        return taskColor;
    }

    public void setTaskColor(String taskColor) {
        this.taskColor = taskColor;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}

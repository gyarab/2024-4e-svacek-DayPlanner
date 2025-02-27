package com.example.dayplanner.main.tasks;

public class Task {
    private String taskId;
    private String taskTitle;
    private String taskDescription;
    private String taskDate;
    private String taskStartTime;
    private int taskLength;
    private boolean isCompleted;

    // Constructor
    public Task(String taskId, String taskTitle, String taskDescription, String taskDate, String taskStartTime, int taskLength, boolean isCompleted) {
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.taskDescription = taskDescription;
        this.taskDate = taskDate;
        this.taskStartTime = taskStartTime;
        this.taskLength = taskLength;
        this.isCompleted = isCompleted;
    }

    // Getter and Setter methods
    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getTaskDate() {
        return taskDate;
    }

    public void setTaskDate(String taskDate) {
        this.taskDate = taskDate;
    }

    public String getTaskStartTime() {
        return taskStartTime;
    }

    public void setTaskStartTime(String taskStartTime) {
        this.taskStartTime = taskStartTime;
    }

    public int getTaskLength() {
        return taskLength;
    }

    public void setTaskLength(int taskLength) {
        this.taskLength = taskLength;
    }

    public boolean isTaskCompleted() {
        return isCompleted;
    }

    public void setTaskCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    // Utility methods
    @Override
    public String toString() {
        return "Task{" +
                "taskId='" + taskId + '\'' +
                ", taskTitle='" + taskTitle + '\'' +
                ", taskDescription='" + taskDescription + '\'' +
                ", taskDate='" + taskDate + '\'' +
                ", taskStartTime='" + taskStartTime + '\'' +
                ", taskLength=" + taskLength + '\'' +
                ", isCompleted=" + isCompleted +
                '}';
    }

    public boolean isValid() {
        return taskTitle != null && !taskTitle.isEmpty() && taskDate != null && !taskDate.isEmpty() && taskStartTime != null;
    }
}

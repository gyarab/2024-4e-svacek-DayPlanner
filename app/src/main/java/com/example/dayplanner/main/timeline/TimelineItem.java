package com.example.dayplanner.main.timeline;

import com.example.dayplanner.main.habits.Habit;

public class TimelineItem {

    private boolean isTask;
    private String taskId, taskTitle, taskStartTime;
    private Habit habit;

    // Constructor for tasks
    public TimelineItem(String taskId, String taskTitle, String taskStartTime) {
        this.isTask = true;
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.taskStartTime = taskStartTime;
    }

    // Constructor for habits
    public TimelineItem(Habit habit) {
        this.isTask = false;
        this.habit = habit;
    }

    public boolean isTask() {
        return isTask;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public String getTaskStartTime() {
        return taskStartTime;
    }

    public String getHabitName() {
        return habit.getName();
    }

    public String getHabitFrequency() {
        return habit.getFrequency();
    }
}

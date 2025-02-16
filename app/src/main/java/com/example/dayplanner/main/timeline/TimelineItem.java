package com.example.dayplanner.main.timeline;

import com.example.dayplanner.main.habits.Habit;

public class TimelineItem {

    private boolean isTask;
    private String taskId, taskTitle, taskStartTime;
    private int durationMinutes;
    private Habit habit;

    // Constructor for tasks
    public TimelineItem(String taskId, String taskTitle, String taskStartTime, int durationMinutes) {
        this.isTask = true;
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.taskStartTime = taskStartTime;
        this.durationMinutes = durationMinutes;
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

    // Converts start time (e.g., "14:30") to total minutes from midnight
    public int getStartTimeInMinutes() {
        if (taskStartTime == null || !taskStartTime.contains(":")) return 0;
        String[] parts = taskStartTime.split(":");
        try {
            return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return 0;  // Default to 0 if the format is incorrect
        }
    }


    // Returns duration in minutes
    public int getDurationInMinutes() {
        return durationMinutes;
    }
}

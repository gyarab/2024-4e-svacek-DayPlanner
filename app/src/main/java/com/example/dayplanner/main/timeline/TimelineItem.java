package com.example.dayplanner.main.timeline;

import com.example.dayplanner.main.habits.Habit;
import com.example.dayplanner.main.tasks.Task;

public class TimelineItem {
    private boolean isTask;
    private Task task;
    private Habit habit;

    // Constructor for tasks
    public TimelineItem(Task task) {
        this.isTask = true;
        this.task = task;
    }

    // Constructor for habits
    public TimelineItem(Habit habit) {
        this.isTask = false;
        this.habit = habit;
    }

    public boolean isTask() {
        return isTask;
    }

    public Task getTask() {
        return task;
    }

    public Habit getHabit() {
        return habit;
    }

    // Retrieves task properties safely
    public String getTaskId() {
        return isTask && task != null ? task.getTaskId() : null;
    }

    public String getTaskTitle() {
        return isTask && task != null ? task.getTaskTitle() : null;
    }

    public String getTaskStartTime() {
        return isTask && task != null ? task.getTaskStartTime() : null;
    }

    public int getTaskDuration() {
        return isTask && task != null ? task.getTaskLength() : 0;
    }

    public String getTaskDescription() {
        return isTask && task != null ? task.getTaskDescription() : null;
    }

    // Retrieves habit properties safely
    public String getHabitName() {
        return !isTask && habit != null ? habit.getName() : null;
    }

    public String getHabitFrequency() {
        return !isTask && habit != null ? habit.getFrequency() : null;
    }

    public String toString() {
        if (isTask) {
            return "Task: " + task.getTaskId() + " | " + task.getTaskTitle() + " | Description: " + task.getTaskDescription() +
                    " | Date: " + task.getTaskDate() +
                    " | Start: " + task.getTaskStartTime() + " | Duration: " + task.getTaskLength() + " min";
        } else if (habit != null) {
            return "Habit: " + habit.getId() + " | " + habit.getName() +
                    " | Frequency: " + habit.getFrequency() + " | Start: " + habit.getStartTime();
        }
        return "Unknown Timeline Item";
    }

    // Converts start time (e.g., "14:30") to total minutes from midnight
    public int getStartTimeInMinutes() {
        String time = isTask ? getTaskStartTime() : (habit != null ? habit.getStartTime() : null);
        if (time == null || !time.contains(":")) return 0;
        String[] parts = time.split(":");
        try {
            return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return 0;  // Default to 0 if the format is incorrect
        }
    }

    // Returns duration in minutes
    public int getDurationInMinutes() {
        return isTask ? getTaskDuration() : (habit != null ? habit.getLength() : 0);
    }
}

package com.example.dayplanner.statistics;

public class HabitProgressEntry {
    private String date;
    private int progress;
    private int goalValue;

    public HabitProgressEntry(String date, int progress, int goalValue) {
        this.date = date;
        this.progress = progress;
        this.goalValue = goalValue;
    }

    public String getDate() {
        return date;
    }

    public int getProgress() {
        return progress;
    }

    public int getGoalValue() {
        return goalValue;
    }

    public String toString() {
        return "Date: " + date + ", Progress: " + progress + ", Goal Value: " + goalValue;
    }
}


package com.example.dayplanner.statistics;

// Model class for daily progress
public class DailyProgress {
    private int day;
    private float completionPercentage;

    public DailyProgress(int day, float completionPercentage) {
        this.day = day;
        this.completionPercentage = completionPercentage;
    }

    public int getDay() {
        return day;
    }

    public float getCompletionPercentage() {
        return completionPercentage;
    }
}
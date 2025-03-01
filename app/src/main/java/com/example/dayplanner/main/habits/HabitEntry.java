package com.example.dayplanner.main.habits;

import android.util.Log;

import com.google.firebase.database.PropertyName;

public class HabitEntry {
    private String date; // The date the habit was performed (could be in the format "YYYY-MM-DD")
    private boolean completed; // Whether the habit was completed on that day
    private int progress; // How much was completed (e.g., 5km out of 10km)
    private int entryGoalValue; // The goal for that habit (e.g., 10km)

    // ✅ Required **no-argument** constructor for Firebase
    public HabitEntry() { }

    public HabitEntry(String date, boolean completed, int progress, int entryGoalValue) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        this.date = date;
        this.completed = completed;
        this.progress = progress;
        this.entryGoalValue = entryGoalValue;
        Log.d("HabitEntry", "Constructor called - Goal set to: " + entryGoalValue);
    }

    // ✅ Getters and Setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public int getEntryGoalValue() { return entryGoalValue; }

    public void setEntryGoalValue(int goal) { this.entryGoalValue = entryGoalValue; }

    @Override
    public String toString() {
        return "HabitEntry{" +
                "date='" + date + '\'' +
                ", completed=" + completed +
                ", progress=" + progress +
                ", goalValue=" + entryGoalValue +
                '}';
    }

}

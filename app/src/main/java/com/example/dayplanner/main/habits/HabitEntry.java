package com.example.dayplanner.main.habits;

public class HabitEntry {
    private String date;
    private int entryGoalValue;
    private int progress;
    private boolean completed;

    // Required no-argument constructor for Firebase
    public HabitEntry() {}

    public HabitEntry(String date, int entryGoalValue, int progress, boolean completed) {
        this.date = date;
        this.entryGoalValue = entryGoalValue;
        this.progress = progress;
        this.completed = completed;
    }

    /** Geters and Setters **/
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getEntryGoalValue() { return entryGoalValue; }
    public void setEntryGoalValue(int entryGoalValue) { this.entryGoalValue = entryGoalValue; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    /** Method to update progress and check completion **/
    public void updateProgress(int amount) {
        this.progress += amount;
        if (this.progress >= this.entryGoalValue) {
            this.completed = true;
            this.progress = this.entryGoalValue; // Prevent overflow
        }
    }

    public String toString() {
        return "Date: " + date + ", Goal: " + entryGoalValue + ", Progress: " + progress + ", Completed: " + completed;
    }
}

package com.example.dayplanner.main.habits;

public class GoalHistoryRecord {
    private int goalValue;
    private String date;

    public GoalHistoryRecord(int goalValue, String date) {
        this.goalValue = goalValue;
        this.date = date;
    }

    public int getGoalValue() {
        return goalValue;
    }

    public void setGoalValue(int goalValue) {
        this.goalValue = goalValue;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

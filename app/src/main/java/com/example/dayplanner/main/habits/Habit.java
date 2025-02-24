package com.example.dayplanner.main.habits;

import com.google.firebase.database.IgnoreExtraProperties;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class Habit {
    private String id;
    private String name;
    private String description;
    private String frequency;
    private String startTime;
    private int length;
    private String metric;
    private int goalValue;
    private int currentStreak;
    private int longestStreak;
    // Use a Map where the key is the date (e.g. "2025-02-28") and the value is the HabitEntry
    private Map<String, HabitEntry> entries;

    // Required no-argument constructor for Firebase
    public Habit() {
        this.entries = new HashMap<>();
    }

    public Habit(String id, String name, String description, String frequency, String startTime, int length, String metric, int goalValue) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.frequency = frequency;
        this.startTime = startTime;
        this.length = length;
        this.metric = metric;
        this.goalValue = goalValue;
        this.currentStreak = 0;
        this.longestStreak = 0;
        this.entries = new HashMap<>();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public int getLength() { return length; }
    public void setLength(int length) { this.length = length; }

    public String getMetric() { return metric; }
    public void setMetric(String metric) { this.metric = metric; }

    public int getGoalValue() { return goalValue; }
    public void setGoalValue(int goalValue) { this.goalValue = goalValue; }

    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }

    public int getLongestStreak() { return longestStreak; }
    public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }

    public Map<String, HabitEntry> getEntries() { return entries; }
    public void setEntries(Map<String, HabitEntry> entries) { this.entries = entries; }

    /**
     * Update or create a habit entry for a given date.
     */
    public void setProgressForDate(String date, int progress) {
        if (entries == null) {
            entries = new HashMap<>();
        }
        HabitEntry entry = entries.get(date);
        if (entry != null) {
            entry.setProgress(progress);
        } else {
            entry = new HabitEntry(date, false, progress, goalValue);
            entries.put(date, entry);
        }
    }

    public String toString() {
        return "Habit{" + "id='" + id + '\'' + ", name='" + name + '\'' +
                ", description='" + description + '\'' + ", frequency='" + frequency + '\'' +
                ", startTime='" + startTime + '\'' + ", length=" + length + '}';
    }
}

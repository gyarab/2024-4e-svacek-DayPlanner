package com.example.dayplanner.main.habits;

import android.util.Log;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

@IgnoreExtraProperties
public class Habit {
    private String id;
    private String name;
    private String description;
    private String frequency;
    private String startDate;
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

    public Habit(String id, String name, String description, String frequency, String startDate, String startTime, int length, String metric, int goalValue) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.frequency = frequency;
        this.startDate = startDate;
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

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

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
            /** check false or true not done**/
            entry = new HabitEntry(date, false, progress, goalValue);
            entries.put(date, entry);
        }
    }

    /**
     * Checks if the habit should be visible on the given date.
     * @param dateToCheck The date to check (format: ddMMyyyy)
     * @return true if the habit should be shown, false otherwise
     */
    public boolean isHabitVisible(String dateToCheck) {
        Log.d("Habit", "Checking visibility");
        // Date format to match your ddMMyyyy pattern
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());

        try {
            // Convert startDate and dateToCheck to Calendar instances
            Calendar start = Calendar.getInstance();
            start.setTime(dateFormat.parse(startDate));

            Calendar checkDate = Calendar.getInstance();
            checkDate.setTime(dateFormat.parse(dateToCheck));

            // Debug logs to see parsed dates and comparison result
            Log.d("Habit", "Start Date: " + start.getTime() + " Check Date: " + checkDate.getTime());

            // If check date is before the habit start date, return false
            if (checkDate.compareTo(start) < 0) {
                Log.d("Habit", "Habit start date is after the check date: " + dateToCheck + " > " + start.getTime());
                return false;
            }

            switch (frequency.toLowerCase()) {
                case "daily":
                    Log.d("Habit", "Daily habit, visible on " + dateToCheck);
                    return true; // Always visible for daily habits

                case "weekly":
                    // Show habit on the same weekday as startDate
                    boolean isVisibleWeekly = start.get(Calendar.DAY_OF_WEEK) == checkDate.get(Calendar.DAY_OF_WEEK);
                    Log.d("TimelineAdapter", "Weekly habit, is visible on " + dateToCheck + ": " + isVisibleWeekly);
                    return isVisibleWeekly;

                case "custom":
                    // Example: User-defined custom days (modify this logic to fit actual user input)
                    List<String> customDays = Arrays.asList("SUNDAY", "TUESDAY", "THURSDAY");

                    // Convert Calendar day to a string (e.g., "MONDAY")
                    String dayOfWeek = checkDate.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()).toUpperCase();
                    boolean isVisibleCustom = customDays.contains(dayOfWeek);
                    Log.d("TimelineAdapter", "Custom habit, is visible on " + dateToCheck + ": " + isVisibleCustom);
                    return isVisibleCustom;

                default:
                    Log.d("Habit", "Unknown frequency, habit not visible");
                    return false;
            }

        } catch (ParseException e) {
            Log.e("TimelineAdapter", "Error parsing date", e);
            return false; // Handle parsing errors gracefully
        }
    }


    public String toString() {
        return "Habit{" + "id='" + id + '\'' + ", name='" + name + '\'' +
                ", description='" + description + '\'' + ", frequency='" + frequency + '\'' +
                ", startTime='" + startTime + '\'' + ", length=" + length + '}';
    }
}

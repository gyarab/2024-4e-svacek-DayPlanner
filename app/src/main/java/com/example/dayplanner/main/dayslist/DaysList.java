package com.example.dayplanner.main.dayslist;

import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class DaysList {
    private final ArrayList<ArrayList<DayModel>> weeksList;
    private static final int START_YEAR = 2024;
    private static final int END_YEAR = 2100;

    public DaysList() {
        weeksList = generateWeeks();
    }

    private ArrayList<ArrayList<DayModel>> generateWeeks() {
        ArrayList<ArrayList<DayModel>> weeksList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        calendar.set(START_YEAR, Calendar.JANUARY, 1);

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int offset = dayOfWeek - Calendar.SUNDAY;
        calendar.add(Calendar.DAY_OF_YEAR, -offset);

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM", Locale.getDefault());

        // Generate weeks until END_YEAR December 31st
        while (calendar.get(Calendar.YEAR) <= END_YEAR) {
            ArrayList<DayModel> week = new ArrayList<>();

            // Create a 7-day week
            for (int i = 0; i < 7; i++) {
                String day = dayFormat.format(calendar.getTime());
                String date = dateFormat.format(calendar.getTime());
                String month = monthFormat.format(calendar.getTime());
                String year = String.valueOf(calendar.get(Calendar.YEAR));

                DayModel dayModel = new DayModel(day, date, month, year);
                week.add(dayModel);

                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            weeksList.add(week);

            if (calendar.get(Calendar.YEAR) > END_YEAR) {
                break;
            }
        }

        Log.d("DaysList", "Generated " + weeksList.size() + " weeks from " + START_YEAR + " to " + END_YEAR);
        return weeksList;
    }

    public ArrayList<DayModel> getWeek(int index) {
        if (index >= 0 && index < weeksList.size()) {
            ArrayList<DayModel> week = weeksList.get(index);
            Log.d("DaysList", "Returning week " + index + " with " + week.size() + " days");
            return week;
        }
        Log.w("DaysList", "Week index " + index + " out of range (0-" + (weeksList.size()-1) + ")");
        return new ArrayList<>();
    }

    public int getCurrentWeekIndex() {
        Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH) + 1;
        int day = today.get(Calendar.DAY_OF_MONTH);

        return findWeekIndex(year, month, day);
    }

    public int getTotalWeeks() {
        return weeksList.size();
    }

    public int findWeekIndex(int year, int month, int day) {
        if (year < START_YEAR || year > END_YEAR) {
            return -1;
        }

        String targetDateId = String.format(Locale.US, "%02d%02d%d", day, month, year);

        for (int i = 0; i < weeksList.size(); i++) {
            ArrayList<DayModel> week = weeksList.get(i);
            for (DayModel dayModel : week) {
                String dateId = dayModel.getDate() + dayModel.getMonth() + dayModel.getYear();
                if (dateId.equals(targetDateId)) {
                    return i;
                }
            }
        }

        return -1; // Not found
    }
}
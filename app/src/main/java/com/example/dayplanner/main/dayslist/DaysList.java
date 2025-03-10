package com.example.dayplanner.main.dayslist;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class DaysList extends ArrayList<DayModel> {

    public DaysList() {
        super(); // Initializes the ArrayList
        this.addAll(getCurrentWeekDays());
    }

    private ArrayList<DayModel> getCurrentWeekDays() {
        ArrayList<DayModel> daysList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd");
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM");

        for (int i = 0; i < 7; i++) {
            String day = dayFormat.format(calendar.getTime());
            String date = dateFormat.format(calendar.getTime());
            String month = monthFormat.format(calendar.getTime());
            String year = String.valueOf(calendar.get(Calendar.YEAR));

            DayModel dayModel = new DayModel(day, date, month, year);
            daysList.add(dayModel);

            calendar.add(Calendar.DAY_OF_WEEK, 1);

            Log.d("WEEK", dayModel.getDayName() + " " + dayModel.getDate());
            Log.d("FormattedDate", "DayName: " + day + ", Day: " + date + ", Month: " + month);
        }

        return daysList;
    }


    @Override
    public String toString() {
        return "DaysList{" +
                "days=" + super.toString() +
                '}';
    }
}

package com.example.dayplanner.main.dayslist;

public class DayModel {
    private String dayName;
    private String date;
    private String month;
    private String year;

    public DayModel(String dayName, String date, String month, String year) {
        this.dayName = dayName;
        this.date = date;
        this.month = month;
        this.year = year;
    }

    public String getDayName() {
        return dayName;
    }

    public String getDate() {
        return  date;
    }

    public String getMonth() {
        return month;
    }

    public String getYear() {
        return year;
    }

    @Override
    public String toString() {
        return "DayModel{" +
                "dayName='" + dayName + '\'' +
                ", date='" + date + '\'' +
                ", month='" + month + '\'' +
                ", year='" + year + '\'' +
                '}';
    }
}

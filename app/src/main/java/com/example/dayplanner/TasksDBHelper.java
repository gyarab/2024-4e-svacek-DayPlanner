package com.example.dayplanner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class TasksDBHelper extends SQLiteOpenHelper {

    private Context context;
    //DB Variables - constants
    private static final String DATABASE_NAME = "DayPlannerDB.db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "Tasks";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_TITLE = "task_title";
    private static final String COLUMN_DESCRIPTION = "task_description";
    private static final String COLUMN_LENGTH = "task_length";
    private static final String COLUMN_DATE = "task_date";
    private static final String COLUMN_START_TIME = "task_start_time";

    public TasksDBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query =
                "CREATE TABLE " + TABLE_NAME +
                        " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_TITLE + " TEXT, " +
                        COLUMN_DESCRIPTION + " TEXT, " +
                        COLUMN_DATE + " STRING, " +
                        COLUMN_START_TIME + " STRING, " +
                        COLUMN_LENGTH + " INTEGER);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Drop old table if it exists
            String query = "DROP TABLE IF EXISTS " + TABLE_NAME;
            db.execSQL(query);

            // Create new table with updated schema
            onCreate(db);
        }
    }

    void addTask(String title, String description, String date, String startTime, int length) {
        SQLiteDatabase db = this.getWritableDatabase(); //this = SQLiteOpenHelper which has the method that allows me to write in the table
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_TITLE, title);
        contentValues.put(COLUMN_DESCRIPTION, description);
        contentValues.put(COLUMN_DATE, date);
        contentValues.put(COLUMN_START_TIME, startTime);
        contentValues.put(COLUMN_LENGTH, length);

        Log.d("Add Task", "Title: " + title + ", Description: " + description + ", Date: " + date + ", Start Time: " + startTime + ", Length: " + length);

        long result = db.insert(TABLE_NAME, null, contentValues);
        if (result == -1) {
            //failed to insert data
            Log.d("DB", "not succesfull");
            Toast.makeText(context, "Failed to add", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("DB", "Succesfull");
            Toast.makeText(context, "Added successfully", Toast.LENGTH_SHORT).show();
        }
    }

    Cursor readAllData() {
        String query = "SELECT * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    Cursor readAllDataWithDate(String date) {
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_DATE + " = '" + date + "'";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }
}

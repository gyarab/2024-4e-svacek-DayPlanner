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
    private static final String DATABASE_NAME = "DayPlanner.db";
    private static final int DATABASE_VERSION = 3;
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
                        COLUMN_DATE + " TEXT, " +
                        COLUMN_START_TIME + " STRING, " +
                        COLUMN_LENGTH + " INTEGER);";
        db.execSQL(query);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            // Step 1: Create a new table with the updated schema (COLUMN_DATE is TEXT here)
            String query = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "_new (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_DESCRIPTION + " TEXT, " +
                    COLUMN_DATE + " TEXT, " + // COLUMN_DATE is now TEXT type
                    COLUMN_START_TIME + " STRING, " +
                    COLUMN_LENGTH + " INTEGER);";
            db.execSQL(query);

            // Step 2: Copy the data from the old table to the new table
            String copyDataQuery = "INSERT INTO " + TABLE_NAME + "_new (" +
                    COLUMN_ID + ", " + COLUMN_TITLE + ", " + COLUMN_DESCRIPTION + ", " +
                    COLUMN_DATE + ", " + COLUMN_START_TIME + ", " + COLUMN_LENGTH + ") " +
                    "SELECT " + COLUMN_ID + ", " + COLUMN_TITLE + ", " + COLUMN_DESCRIPTION + ", " +
                    COLUMN_DATE + ", " + COLUMN_START_TIME + ", " + COLUMN_LENGTH + " FROM " + TABLE_NAME;
            db.execSQL(copyDataQuery);

            // Step 3: Drop the old table
            String dropOldTableQuery = "DROP TABLE IF EXISTS " + TABLE_NAME;
            db.execSQL(dropOldTableQuery);

            // Step 4: Rename the new table to the original table name
            String renameNewTableQuery = "ALTER TABLE " + TABLE_NAME + "_new RENAME TO " + TABLE_NAME;
            db.execSQL(renameNewTableQuery);
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

    void editTask(String taskID, String newTitle, String newDescription, String newDate, String newTime, int newLength) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        // Add the new values to be updated
        contentValues.put(COLUMN_TITLE, newTitle);
        contentValues.put(COLUMN_DESCRIPTION, newDescription);
        contentValues.put(COLUMN_DATE, newDate);
        contentValues.put(COLUMN_START_TIME, newTime);
        contentValues.put(COLUMN_LENGTH, newLength);

        // Define the WHERE clause and arguments
        String whereClause = COLUMN_ID + " = ?";
        String[] whereArgs = {taskID};

        // Attempt to update the row
        int rowsAffected = db.update(TABLE_NAME, contentValues, whereClause, whereArgs);

        if (rowsAffected > 0) {
            Log.d("DB", "Task updated successfully: ID " + taskID);
            Toast.makeText(context, "Task updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Log.d("DB", "Task update failed: ID " + taskID);
            Toast.makeText(context, "Failed to update task", Toast.LENGTH_SHORT).show();
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
        SQLiteDatabase db = this.getReadableDatabase();
        Log.d("readAllDataWithDate", date);
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_DATE + " = ?";
        Log.d("Database Query", "Query: " + query + " with date: " + date);

        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, new String[]{date});
        }
        return cursor;
    }
}

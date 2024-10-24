package com.example.dayplanner;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class TimelineDbHelper extends SQLiteOpenHelper {

    private Context context;
    //DB Variables - constants
    private static final String DATABASE_NAME = "TimeLine.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "time_line";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_TITLE = "task_title";
    private static final String COLUMN_DESCRIPTION = "task_description";
    private static final String COLUMN_LENGTH = "task_length";

    public TimelineDbHelper(@Nullable Context context) {
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
                        COLUMN_LENGTH + " INTEGER);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        String query = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(query);
        onCreate(db);
    }

    void addTask(String title, String description, int length) {
        SQLiteDatabase db = this.getWritableDatabase(); //this = SQLiteOpenHelper which has the method that allows me to write in the table
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_TITLE, title);
        contentValues.put(COLUMN_DESCRIPTION, description);
        contentValues.put(COLUMN_LENGTH, length);

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
}

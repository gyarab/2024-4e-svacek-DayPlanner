package com.example.dayplanner.main.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class TasksDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tasks.db";
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_TASKS = "tasks";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_START_TIME = "start_time";
    private static final String COLUMN_LENGTH = "length";
    private static final String COLUMN_COMPLETED = "is_completed";

    public TasksDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_TASKS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLE + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_START_TIME + " TEXT, " +
                COLUMN_LENGTH + " INTEGER, " +
                COLUMN_COMPLETED + "INTEGER DEFAULT 0)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            Log.d("ON UPGRADE", "done");
            // Add the new 'is_completed' column in version 2
            db.execSQL("ALTER TABLE " + TABLE_TASKS + " ADD COLUMN " + COLUMN_COMPLETED + " INTEGER DEFAULT 0");
        }
    }


    // **Add a new task**
    public void addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, task.getTaskTitle());
        values.put(COLUMN_DESCRIPTION, task.getTaskDescription());
        values.put(COLUMN_DATE, task.getTaskDate());
        values.put(COLUMN_START_TIME, task.getTaskStartTime());
        values.put(COLUMN_LENGTH, task.getTaskLength());
        values.put(COLUMN_COMPLETED, task.isTaskCompleted() ? 1 : 0);

        db.insert(TABLE_TASKS, null, values);
        db.close();
    }

    // **Edit an existing task**
    public void editTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, task.getTaskTitle());
        values.put(COLUMN_DESCRIPTION, task.getTaskDescription());
        values.put(COLUMN_DATE, task.getTaskDate());
        values.put(COLUMN_START_TIME, task.getTaskStartTime());
        values.put(COLUMN_LENGTH, task.getTaskLength());
        values.put(COLUMN_COMPLETED, task.isTaskCompleted() ? 1 : 0);

        db.update(TABLE_TASKS, values, COLUMN_ID + " = ?", new String[]{task.getTaskId()});
        db.close();
    }

    // **Delete a task**
    public void deleteTask(String taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, COLUMN_ID + " = ?", new String[]{taskId});
        db.close();
    }

    // **Retrieve a single task by ID**
    public Task getTaskById(String taskId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, null, COLUMN_ID + " = ?", new String[]{taskId}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Task task = new Task(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_TIME)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LENGTH)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPLETED)) > 0
            );
            cursor.close();
            return task;
        }
        return null;
    }

    // **Retrieve all tasks**
    public List<Task> getAllTasks() {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, null, null, null, null, null, COLUMN_DATE + ", " + COLUMN_START_TIME);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Task task = new Task(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_TIME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LENGTH)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPLETED)) > 0
                );
                taskList.add(task);
            }
            cursor.close();
        }

        return taskList;
    }

    // **Retrieve tasks by specific date**
    public List<Task> getTasksByDate(String date) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to fetch tasks where the date column matches the given date
        Cursor cursor = db.query(TABLE_TASKS, null, COLUMN_DATE + " = ?", new String[]{date}, null, null, COLUMN_START_TIME);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Task task = new Task(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_START_TIME)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LENGTH)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPLETED)) > 0
                );
                taskList.add(task);
            }
            cursor.close();
        }

        return taskList;
    }

}

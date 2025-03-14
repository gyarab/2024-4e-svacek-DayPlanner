package com.example.dayplanner.settings;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ThemePreferencesHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "app_settings.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "preferences";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_THEME = "theme"; // "light" or "dark"

    public ThemePreferencesHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY, "
                + COLUMN_THEME + " TEXT)";
        db.execSQL(CREATE_TABLE);

        // Insert default theme (Light Mode)
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + COLUMN_ID + ", " + COLUMN_THEME + ") VALUES (1, 'light')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void setThemePreference(String theme) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_NAME + " SET " + COLUMN_THEME + " = ? WHERE " + COLUMN_ID + " = 1", new String[]{theme});
        db.close();
    }

    public String getThemePreference() {
        SQLiteDatabase db = this.getReadableDatabase();
        String theme = "light"; // Default
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_THEME + " FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = 1", null);
        if (cursor.moveToFirst()) {
            theme = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return theme;
    }
}

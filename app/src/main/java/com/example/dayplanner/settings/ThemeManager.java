package com.example.dayplanner.settings;

import android.content.Context;
import android.content.res.Configuration;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.dayplanner.settings.ThemePreferencesHelper;

public class ThemeManager {
    private final ThemePreferencesHelper preferencesHelper;

    public ThemeManager(Context context) {
        preferencesHelper = new ThemePreferencesHelper(context);
    }

    public void applySavedTheme() {
        String theme = preferencesHelper.getThemePreference();
        if (theme.equals("dark")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public void toggleTheme() {
        boolean isDark = (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);
        String newTheme = isDark ? "light" : "dark";

        // Save new preference
        preferencesHelper.setThemePreference(newTheme);

        // Apply new theme
        AppCompatDelegate.setDefaultNightMode(isDark ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES);
    }
}

package com.example.dayplanner.settings;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import com.example.dayplanner.R;

public class SettingsActivity extends AppCompatActivity {
    SwitchCompat switchMode;
    ThemePreferencesHelper dbHelper;
    boolean nightMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        /** THEMES **/
        switchMode = findViewById(R.id.switchMode);
        dbHelper = new ThemePreferencesHelper(this);

        // Load saved theme
        nightMode = dbHelper.getThemePreference().equals("dark");

        if (nightMode) {
            switchMode.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        switchMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nightMode) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    dbHelper.setThemePreference("light");
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    dbHelper.setThemePreference("dark");
                }
                nightMode = !nightMode;
            }
        });
    }
}

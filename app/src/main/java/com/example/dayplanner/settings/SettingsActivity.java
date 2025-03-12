package com.example.dayplanner.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import com.example.dayplanner.R;
import com.example.dayplanner.auth.AuthenticationActivity;
import com.example.dayplanner.auth.signin.EmailSignInActivity;
import com.example.dayplanner.main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {
    SwitchCompat switchMode, switchNotifications;
    ThemePreferencesHelper dbHelper;
    boolean nightMode;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Button logOutButton;

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

        /** NOTIFICATIONS **/
        switchNotifications = findViewById(R.id.switchNotifications);
        preferences = getSharedPreferences("settings", MODE_PRIVATE);
        editor = preferences.edit();

        // Load saved preference
        boolean notificationsEnabled = preferences.getBoolean("notifications_enabled", true);
        switchNotifications.setChecked(notificationsEnabled);

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            editor.putBoolean("notifications_enabled", isChecked);
            editor.apply();
        });

        logOutButton = findViewById(R.id.btnLogout);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(SettingsActivity.this, AuthenticationActivity.class);
        startActivity(intent);
        finish();
    }
}

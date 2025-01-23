package com.example.dayplanner.settings;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.example.dayplanner.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey);

        // Dark Mode Toggle
        SwitchPreferenceCompat darkModePref = findPreference("dark_mode");
        if (darkModePref != null) {
            darkModePref.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean isDarkMode = (boolean) newValue;
                // Apply dark mode changes
                AppCompatDelegate.setDefaultNightMode(isDarkMode
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO);
                return true; // Save the preference
            });
        }

        // Username Preference
        EditTextPreference usernamePref = findPreference("username");
        if (usernamePref != null) {
            usernamePref.setOnPreferenceChangeListener((preference, newValue) -> {
                String username = (String) newValue;
                // Handle username change, e.g., validate or save it elsewhere
                if (username.trim().isEmpty()) {
                    Toast.makeText(getContext(), "Username cannot be empty!", Toast.LENGTH_SHORT).show();
                    return false; // Reject invalid input
                }
                return true; // Save the new username
            });
        }
    }
}


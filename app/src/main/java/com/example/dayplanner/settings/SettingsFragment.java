package com.example.dayplanner.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.example.dayplanner.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        /*setPreferencesFromResource(R.xml.settings_a, rootKey);

        // Handle Dark Mode Switch
        SwitchPreferenceCompat switchMode = findPreference("dark_mode");

        if (switchMode != null) {
            SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MODE", Context.MODE_PRIVATE);
            boolean nightMode = sharedPreferences.getBoolean("nightMode", false);

            switchMode.setChecked(nightMode);

            switchMode.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean isEnabled = (boolean) newValue;
                AppCompatDelegate.setDefaultNightMode(isEnabled ?
                        AppCompatDelegate.MODE_NIGHT_YES :
                        AppCompatDelegate.MODE_NIGHT_NO);

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("nightMode", isEnabled);
                editor.apply();

                return true;
            });
        }

        // Handle Username Preference
        EditTextPreference usernamePref = findPreference("username");
        if (usernamePref != null) {
            usernamePref.setOnPreferenceChangeListener((preference, newValue) -> {
                String username = (String) newValue;
                if (username.trim().isEmpty()) {
                    Toast.makeText(getContext(), "Username cannot be empty!", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;
            });
        }*/
    }
}

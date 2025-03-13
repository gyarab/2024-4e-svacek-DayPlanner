package com.example.dayplanner.settings;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;

import com.example.dayplanner.R;
import com.example.dayplanner.auth.AuthenticationActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

public class SettingsActivity extends AppCompatActivity {
    SwitchCompat switchMode, switchNotifications;
    ThemePreferencesHelper dbHelper;
    boolean nightMode;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    Button logOutButton, changePasswordButton;
    FirebaseAuth auth;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        /** Firebase **/
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

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

        changePasswordButton = findViewById(R.id.btnChangePassword);

        if(isUserAuthByGoogle()) {
            changePasswordButton.setVisibility(View.GONE);
        } else {
            changePasswordButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showChangePasswordDialog();
                }
            });
        }
    }

    private boolean isUserAuthByGoogle() {
        if (user != null) {
            for (UserInfo userInfo : user.getProviderData()) {
                if (userInfo.getProviderId().equals("google.com")) {
                    return true;
                }
            }

        }
        return false;
    }
    private void logout() {
        auth.signOut();
        Intent intent = new Intent(SettingsActivity.this, AuthenticationActivity.class);
        startActivity(intent);
        finish();
    }
    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password");

        // Create a layout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        // Input fields
        EditText newPasswordInput = new EditText(this);
        newPasswordInput.setHint("Enter new password");
        newPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        EditText confirmPasswordInput = new EditText(this);
        confirmPasswordInput.setHint("Confirm new password");
        confirmPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        layout.addView(newPasswordInput);
        layout.addView(confirmPasswordInput);

        builder.setView(layout);

        // Buttons
        builder.setPositiveButton("Change", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String newPassword = newPasswordInput.getText().toString().trim();
                String confirmPassword = confirmPasswordInput.getText().toString().trim();

                if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPassword.equals(confirmPassword)) {
                    Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (user != null) {
                    user.updatePassword(newPassword)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Failed to update password", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(getApplicationContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

}

package com.example.dayplanner.main;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dayplanner.R;
import com.example.dayplanner.auth.AuthenticationActivity;
import com.example.dayplanner.auth.signin.EmailSignInActivity;
import com.example.dayplanner.main.dayslist.WeeklyHeaderFragment;
import com.example.dayplanner.main.habits.Habit;
import com.example.dayplanner.main.habits.HabitDialogFragment;
import com.example.dayplanner.main.tasks.Task;
import com.example.dayplanner.main.tasks.TaskDialogFragment;
import com.example.dayplanner.main.timeline.TimelineFragment;
import com.example.dayplanner.settings.SettingsActivity;
import com.example.dayplanner.settings.ThemePreferencesHelper;
import com.example.dayplanner.statistics.StatisticsActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements WeeklyHeaderFragment.OnDaySelectedListener, TaskDialogFragment.TaskDialogListener, HabitDialogFragment.HabitDialogListener {


    FloatingActionButton addButton, profileButton;
    Button addTaskFab, addHabitFab;
    TextView addTaskText, addHabitText;
    LinearLayoutCompat addTaskContainer, addHabitContainer;
    View blurOverlay;
    boolean isOptionsVisible = false;
    private FirebaseAuth mAuth;
    private TaskDialogFragment.TaskDialogListener listener;
    private static final int NOTIFICATION_PERMISSION_CODE = 1;
    private static final int REQUEST_CODE_PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply saved theme before setting the content view
        ThemePreferencesHelper dbHelper = new ThemePreferencesHelper(this);
        if (dbHelper.getThemePreference().equals("dark")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Request notification permission for API level 33 (Android 13) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12 or above
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivityForResult(intent, REQUEST_CODE_PERMISSION);
            }
        }

        /** For databases outside of USA I need an url as an argument for getInstance*/
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://dayplanner-18a02-default-rtdb.europe-west1.firebasedatabase.app");

        /** Check if user is signed in **/
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.d("USR", "Not signed in");
            Intent intent = new Intent(MainActivity.this, AuthenticationActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        Log.d("USR", currentUser.toString());

        if(!isConnectedToWifi(MainActivity.this)) {
            Toast.makeText(MainActivity.this, "No internet connection", Toast.LENGTH_SHORT).show();
        }

        DatabaseReference userReference = database.getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("info");
        userReference.setValue(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        if (savedInstanceState == null) {
            // Add TimelineFragment to the activity
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container_timeline, new TimelineFragment(), "TIMELINE_FRAGMENT_TAG")
                    .commit();

            // Add WeeklyHeaderFragment to the activity
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container_header, new WeeklyHeaderFragment(), "WEEKLY_HEADER_FRAGMENT_TAG")
                    .commit();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
         Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
         });

        popupMenu(); // function that sets the popup menu to work

        // Get current date and set month/year in the UI
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        String currentMonthName = new DateFormatSymbols().getMonths()[currentMonth]; // convert month number to name

        addButton = findViewById(R.id.AddTaskButton);
        addTaskFab = findViewById(R.id.addTaskFab);
        addHabitFab = findViewById(R.id.addHabitFab);
        blurOverlay = findViewById(R.id.blurOverlay);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleAddOptions();
            }
        });
        blurOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleAddOptions(); // Hides options if clicked outside
            }
        });

        addTaskFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Task newTask = new Task(null, "", "", "", "", 0, false);

                TaskDialogFragment fragment = new TaskDialogFragment(false, newTask);
                fragment.show(getSupportFragmentManager(), "AddTaskDialog");

                toggleAddOptions();
            }
        });

        addHabitFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Habit newHabit = new Habit();

                HabitDialogFragment fragment = new HabitDialogFragment(false, newHabit);
                fragment.show(getSupportFragmentManager(), "AddHabitDialog");
                toggleAddOptions();
            }
        });
    }

    /** Method for creating a blur effect and animation **/
    private void toggleAddOptions() {
        if (isOptionsVisible) {
            // Hide options
            blurOverlay.animate().alpha(0f).setDuration(300).withEndAction(() -> blurOverlay.setVisibility(View.GONE));
            profileButton.animate().alpha(1f).setDuration(300); // Restore profile button visibility
            addTaskFab.animate().translationY(0).alpha(0f).setDuration(300).withEndAction(() -> addTaskFab.setVisibility(View.GONE));
            addHabitFab.animate().translationY(0).alpha(0f).setDuration(300).withEndAction(() -> addHabitFab.setVisibility(View.GONE));
        } else {
            // Show options
            blurOverlay.setVisibility(View.VISIBLE);
            blurOverlay.animate().alpha(1f).setDuration(300);
            profileButton.animate().alpha(0.3f).setDuration(300); // Blur the profile button
            addTaskFab.setVisibility(View.VISIBLE);
            addTaskFab.animate().translationY(-100).alpha(1f).setDuration(300);
            addHabitFab.setVisibility(View.VISIBLE);
            addHabitFab.animate().translationY(-160).alpha(1f).setDuration(300);
        }
        isOptionsVisible = !isOptionsVisible;
    }


    @Override
    public void onDaySelected(String dateId) {
        // Update timeline fragment
        TimelineFragment timelineFragment = (TimelineFragment) getSupportFragmentManager().findFragmentByTag("TIMELINE_FRAGMENT_TAG");
        if (timelineFragment != null) {
            timelineFragment.onDaySelected(dateId);
        } else {
            Log.e("MainActivity", "TimelineFragment not found");
        }

        // Update weekly header fragment dot
        WeeklyHeaderFragment weeklyFragment = (WeeklyHeaderFragment) getSupportFragmentManager().findFragmentByTag("WEEKLY_HEADER_FRAGMENT_TAG");
        if (weeklyFragment != null) {
            weeklyFragment.dayAdapter.setActiveDotByDateId(dateId);
        }
    }

    public void navigateToDate(int year, int month, int day) {
        String dateId = String.format(Locale.US, "%02d%02d%d", day, month, year);

        // Update the WeeklyHeaderFragment
        WeeklyHeaderFragment weeklyFragment = (WeeklyHeaderFragment) getSupportFragmentManager().findFragmentByTag("WEEKLY_HEADER_FRAGMENT_TAG");
        if (weeklyFragment != null) {
            weeklyFragment.navigateToDate(year, month, day);
        }
    }

    // Update onTaskDataChanged and onHabitDataChanged methods to use the fragment reference
    public void onTaskDataChanged(String dateId) {
        if (dateId != null && dateId.length() == 8) {
            int day = Integer.parseInt(dateId.substring(0, 2));
            int month = Integer.parseInt(dateId.substring(2, 4));
            int year = Integer.parseInt(dateId.substring(4));

            navigateToDate(year, month, day);
        }
    }

    public void onHabitDataChanged(String dateId) {
        if (dateId != null && dateId.length() == 8) {
            int day = Integer.parseInt(dateId.substring(0, 2));
            int month = Integer.parseInt(dateId.substring(2, 4));
            int year = Integer.parseInt(dateId.substring(4));

            navigateToDate(year, month, day);
        }
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MainActivity.this, EmailSignInActivity.class);
        startActivity(intent);
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void requestNotificationPermission() {
        // Check if the notification permission is already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

            // If not granted, request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                Log.d("MainActivity", "Notification permission granted");
            } else {
                // Permission denied
                Log.d("MainActivity", "Notification permission denied");
                Toast.makeText(this, "Notification permission is required for task reminders", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean isConnectedToWifi(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // For API 23 and above
            Network activeNetwork = connectivityManager.getActiveNetwork();
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork);

            if (networkCapabilities != null) {
                return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
            }
        } else {
            // For below API 23
            android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null && activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            }
        }

        return false;
    }

    /** Function for showing a popup menu when the profile button is clicked **/
    void popupMenu() {
        profileButton = findViewById(R.id.ProfileButton);

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
                // Force icons to be shown in the PopupMenu
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // by default the icons are hidden, idk why
                    popupMenu.setForceShowIcon(true);
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if (id == R.id.settings) {
                            Toast.makeText(MainActivity.this, "Settings clicked", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                            startActivity(intent);
                            return true;
                        } else if (id == R.id.statistics) {
                            if(isConnectedToWifi(MainActivity.this)) {
                                Toast.makeText(MainActivity.this, "Statistics clicked", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(MainActivity.this, "Connect to wifi in order to view statistics", Toast.LENGTH_SHORT).show();
                                return false;
                            }
                            return true;
                        } 
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
    }
}

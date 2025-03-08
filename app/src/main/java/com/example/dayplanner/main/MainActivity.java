package com.example.dayplanner.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dayplanner.R;
import com.example.dayplanner.auth.AuthenticationActivity;
import com.example.dayplanner.auth.signin.EmailSignInActivity;
import com.example.dayplanner.main.dayslist.DayAdapter;
import com.example.dayplanner.main.dayslist.WeeklyHeaderFragment;
import com.example.dayplanner.main.habits.Habit;
import com.example.dayplanner.main.tasks.TaskDialogFragment;
import com.example.dayplanner.main.timeline.TimelineFragment;
import com.example.dayplanner.settings.SettingsActivity;
import com.example.dayplanner.statistics.StatisticsActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.dayplanner.main.habits.HabitDialogFragment;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import com.example.dayplanner.main.tasks.Task;

public class MainActivity extends AppCompatActivity implements WeeklyHeaderFragment.OnDaySelectedListener, TaskDialogFragment.TaskDialogListener {


    FloatingActionButton addButton, profileButton;
    Button addTaskFab, addHabitFab;
    TextView addTaskText, addHabitText;
    LinearLayoutCompat addTaskContainer, addHabitContainer;
    View blurOverlay;
    boolean isOptionsVisible = false;
    Button register;
    private FirebaseAuth mAuth;
    private TaskDialogFragment.TaskDialogListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO: Logout Button in setting activity
        /** logs user out**/
        //logout();

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        /** For databases outside of USA I need an url as an argument for getInstance*/
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://dayplanner-18a02-default-rtdb.europe-west1.firebasedatabase.app");

        /** Check if user is signed in **/
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.d("USR", "Not signed in");
            /** He should be transfered to a UI where he can pick from multiple logins or click register using email or phone nuber**/
            Intent intent = new Intent(MainActivity.this, AuthenticationActivity.class);
            startActivity(intent);
            finish(); // Prevent the user from returning to MainActivity
            return;
        }
        Log.d("USR", currentUser.toString());

        DatabaseReference userReference = database.getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("info");
        userReference.setValue(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        /** Register Button Temporary Design**/
        //TODO: Move to settings activity
        register = findViewById(R.id.RegisterPage);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AuthenticationActivity.class);
                startActivity(intent);
            }
        });

        if (savedInstanceState == null) {
            // Add TimelineFragment to the activity
            FragmentTransaction timelineTransaction = getSupportFragmentManager().beginTransaction();
            timelineTransaction.replace(R.id.fragment_container_timeline, new TimelineFragment());
            timelineTransaction.commit();

            // Adding a Tag to the Timeline fragment so that I can access its methods later
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container_timeline, new TimelineFragment(), "TIMELINE_FRAGMENT_TAG")
                    .commit();

            // Add WeeklyHeaderFragment to the activity
            FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
            transaction2.replace(R.id.fragment_container_header, new WeeklyHeaderFragment());
            transaction2.commit();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
         Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
         });

        //TODO: do a seperate fragment from it
        popupMenu(); // function that sets the popup menu to work

        // Get current date and set month/year in the UI
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        String currentMonthName = new DateFormatSymbols().getMonths()[currentMonth]; // convert month number to name
        ((TextView) findViewById(R.id.monthYearTextView)).setText(currentMonthName + " " + currentYear);

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


    public void onDaySelected(String dateId) {
        TimelineFragment timelineFragment = (TimelineFragment) getSupportFragmentManager().findFragmentByTag("TIMELINE_FRAGMENT_TAG");
        if (timelineFragment != null) {
            timelineFragment.onDaySelected(dateId);
        } else {
            Log.e("MainActivity", "TimelineFragment not found");
        }
    }

    public void onTaskDataChanged(String dateId) {
        TimelineFragment timelineFragment = (TimelineFragment) getSupportFragmentManager().findFragmentByTag("TIMELINE_FRAGMENT_TAG");
        if (timelineFragment != null) {
            timelineFragment.fetchTasksAndHabits(dateId); // Refresh timeline with tasks and habits

            RecyclerView weeklyRecyclerView = findViewById(R.id.weeklyRecyclerView);
            DayAdapter adapter = (DayAdapter) weeklyRecyclerView.getAdapter();
            if (adapter != null) {
                adapter.setActiveDotByDateId(dateId); // Update the dot in the weekly view
            }
        } else {
            Log.e("MainActivity", "TimelineFragment not found");
        }
    }

    public void onHabitDataChanged(String dateId) {
        //TODO: needs to be used

        TimelineFragment timelineFragment = (TimelineFragment) getSupportFragmentManager().findFragmentByTag("TIMELINE_FRAGMENT_TAG");
        if (timelineFragment != null) {
            timelineFragment.fetchTasksAndHabits(dateId); // Refresh timeline with both tasks and habits

            RecyclerView weeklyRecyclerView = findViewById(R.id.weeklyRecyclerView);
            DayAdapter adapter = (DayAdapter) weeklyRecyclerView.getAdapter();
            if (adapter != null) {
                adapter.setActiveDotByDateId(dateId);
            }
        } else {
            Log.e("MainActivity", "TimelineFragment not found");
        }
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(MainActivity.this, EmailSignInActivity.class);
        startActivity(intent);
        finish();
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
                            Toast.makeText(MainActivity.this, "Statistics clicked", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
                            startActivity(intent);
                            return true;
                        } else if (id == R.id.archive) {
                            Toast.makeText(MainActivity.this, "Archive clicked", Toast.LENGTH_SHORT).show();
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

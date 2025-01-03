package com.example.dayplanner;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormatSymbols;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements WeeklyHeaderFragment.OnDaySelectedListener {


    FloatingActionButton addTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

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

        // Set up system UI insets
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
        ((TextView) findViewById(R.id.monthYearTextView)).setText(currentMonthName + " " + currentYear); // set the month year view to current month and year as default

        addTask = findViewById(R.id.AddTaskButton);
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // User wants to add a new task -> new activity starts
                TaskDialogFragment fragment = new TaskDialogFragment(false, null, "", "", "", "", "");
                fragment.show(getSupportFragmentManager(), "AddTaskDialog");
            }
        });
    }

    public void onDaySelected(String dateId) {
        // Pass the dateId to TimelineFragment
        TimelineFragment timelineFragment = (TimelineFragment) getSupportFragmentManager().findFragmentByTag("TIMELINE_FRAGMENT_TAG");
        if (timelineFragment != null) {
            timelineFragment.fetchTaskData(dateId); // Call the method in the fragment
        } else {
            Log.e("MainActivity", "TimelineFragment not found");
        }
    }
    void popupMenu() {
        // Function for showing a popup menu when the profile button is clicked
        FloatingActionButton profileButton = findViewById(R.id.ProfileButton);

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

                // Force icons to be shown in the PopupMenu
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // by default the icons are hidden, but force them to be shown
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

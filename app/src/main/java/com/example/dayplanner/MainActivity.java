package com.example.dayplanner;

import android.content.Intent;
import android.database.Cursor;
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
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    RecyclerView timeLine;
    FloatingActionButton addTask;
    RecyclerView weeklyRecyclerView;
    TasksDBHelper timelineDbHelper;
    ArrayList<String> task_start_time, task_title, task_description, task_length;
    TimelineAdapter timelineAdapter;
    DayAdapter dayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        popupMenu(); //function that sets the popupmenu to work

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        String currentMonthName = new DateFormatSymbols().getMonths()[currentMonth]; //convert month number to name
        ((TextView)findViewById(R.id.monthYearTextView)).setText(currentMonthName + " " + currentYear); //set the month year view to current month and year as default

        //Creating timeline recyclerview
        timeLine = findViewById(R.id.timeLine);
        addTask = findViewById(R.id.AddTaskButton);
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //User wants too add new task -> new activity starts
                Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
                startActivity(intent);
            }
        });

        timelineDbHelper = new TasksDBHelper(MainActivity.this);
        task_start_time = new ArrayList<>();
        task_title = new ArrayList<>();
        task_description = new ArrayList<>();
        task_length = new ArrayList<>();

        timelineAdapter = new TimelineAdapter(MainActivity.this, task_start_time, task_title, task_description, task_length);
        timeLine.setAdapter(timelineAdapter);
        //This line assigns your custom RecyclerView.Adapter (customAdapter) to the RecyclerView (timeLine).
        //The adapter (customAdapter) is responsible for creating and binding the individual list items that the RecyclerView will display.
        //In this case, customAdapter contains task data, which it will provide to each RecyclerView item through methods like onBindViewHolder
        timeLine.setLayoutManager(new LinearLayoutManager(MainActivity.this)); //organizes the items in a vertical or horizontal scrolling list (vertical by default)

        fetchTaskData("13122024");

        //Creating dayRecyclerView - week displayed as a header
        DaysList daysList = new DaysList();

        dayAdapter = new DayAdapter(MainActivity.this, daysList, new DayAdapter.OnDayClickListener() {
            @Override
            public void onDayClick(String dateId) {
                fetchTaskData(dateId);
            }
        });
        weeklyRecyclerView = findViewById(R.id.weeklyRecyclerView);
        weeklyRecyclerView.setAdapter(dayAdapter);
        weeklyRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
    }

    void fetchTaskData(String dateId) {
        Cursor cursor = timelineDbHelper.readAllDataWithDate(dateId); //Object used to retrieve data from db
        Log.d("FETCH", "fetchTaskData: " + dateId);

        //clear the arrays
        task_start_time.clear();
        task_title.clear();
        task_description.clear();
        task_length.clear();

        if (cursor.getCount() == 0) {
            //get count gets the number of rows
            //if no rows then display a message
            Toast.makeText(this, "Add Tasks and tackle your day!", Toast.LENGTH_SHORT).show();
        } else {
            //User has saved some tasks in database
            while(cursor.moveToNext()) {
                //moveToNext() takes next row of the retrieved db data
                task_start_time.add(cursor.getString(4)); //adds to array the first column of the row
                task_title.add(cursor.getString(1)); //adds to array the second column of the row and so on
                task_description.add(cursor.getString(2));
                task_length.add(cursor.getString(5));
            }
            Log.d("FETCH", task_title.toString());
        }
        cursor.close();

        //notify the timelineAdapter that the data has changed
        timelineAdapter.notifyDataSetChanged();
    }

    void popupMenu() {
        FloatingActionButton profileButton = findViewById(R.id.ProfileButton);

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

                // Force icons to be shown in the PopupMenu
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    //by default the are for some reason hidden :)
                    popupMenu.setForceShowIcon(true);
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if (id == R.id.settings) {
                            Toast.makeText(MainActivity.this, "Settings clicked", Toast.LENGTH_SHORT).show();
                            return true;
                        } else if (id == R.id.statistics) {
                            Toast.makeText(MainActivity.this, "Statistics clicked", Toast.LENGTH_SHORT).show();
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
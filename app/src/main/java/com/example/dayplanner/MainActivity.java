package com.example.dayplanner;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView timeLine;
    FloatingActionButton addTask;
    RecyclerView weeklyRecyclerView;
    TasksDBHelper timelineDbHelper;
    ArrayList<String> task_id, task_title, task_description, task_length;
    TimelineAdapter customAdapter;
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

        //Creating timeline recyclerview
        timeLine = findViewById(R.id.timeLine);
        addTask = findViewById(R.id.floatingActionButton);
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //User wants too add new task -> new activity starts
                Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
                startActivity(intent);
            }
        });

        timelineDbHelper = new TasksDBHelper(MainActivity.this);
        task_id = new ArrayList<>();
        task_title = new ArrayList<>();
        task_description = new ArrayList<>();
        task_length = new ArrayList<>();

        fetchTaskData();

        customAdapter = new TimelineAdapter(MainActivity.this, task_id, task_title, task_description, task_length);
        timeLine.setAdapter(customAdapter);
        //This line assigns your custom RecyclerView.Adapter (customAdapter) to the RecyclerView (timeLine).
        //The adapter (customAdapter) is responsible for creating and binding the individual list items that the RecyclerView will display.
        //In this case, customAdapter contains task data, which it will provide to each RecyclerView item through methods like onBindViewHolder
        timeLine.setLayoutManager(new LinearLayoutManager(MainActivity.this)); //organizes the items in a vertical or horizontal scrolling list (vertical by default)

        //Creating dayRecyclerView - week displayed as a header
        DaysList daysList = new DaysList();

        dayAdapter = new DayAdapter(MainActivity.this, daysList);
        weeklyRecyclerView = findViewById(R.id.weeklyRecyclerView);
        weeklyRecyclerView.setAdapter(dayAdapter);
        weeklyRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
    }

    void fetchTaskData() {
        Cursor cursor = timelineDbHelper.readAllDataWithDate("13122024"); //Object used to retrieve data from db
        if (cursor.getCount() == 0) {
            //get count gets the number of rows
            //if no rows then display a message
            Toast.makeText(this, "Add Tasks and tackle your day!", Toast.LENGTH_SHORT).show();
        } else {
            //User has saved some tasks in database
            while(cursor.moveToNext()) {
                //moveToNext() takes next row of the retrieved db data
                task_id.add(cursor.getString(0)); //adds to array the first column of the row
                task_title.add(cursor.getString(1)); //adds to array the second column of the row and so on
                task_description.add(cursor.getString(2));
                task_length.add(cursor.getString(5));
            }
        }
        cursor.close();
    }
}
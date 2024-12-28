package com.example.dayplanner;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

// Fragment responsible for displaying tasks for a specific day
public class TimelineFragment extends Fragment {

    RecyclerView timeLine; // RecyclerView to display the task timeline
    TasksDBHelper timelineDbHelper; // Helper class to interact with the database
    ArrayList<String> task_start_time, task_title, task_description, task_length; // Lists to hold task data
    TimelineAdapter timelineAdapter; // Adapter to bind task data to RecyclerView

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for the fragment
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);

        // Initialize the RecyclerView for timeline view
        timeLine = view.findViewById(R.id.timeLine);
        timelineDbHelper = new TasksDBHelper(getContext());
        task_start_time = new ArrayList<>();
        task_title = new ArrayList<>();
        task_description = new ArrayList<>();
        task_length = new ArrayList<>();

        // Set the adapter to the RecyclerView and layout manager
        timelineAdapter = new TimelineAdapter(getContext(), task_start_time, task_title, task_description, task_length);
        timeLine.setAdapter(timelineAdapter);
        timeLine.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize the timeline with a default date (e.g., "13122024")
        fetchTaskData("13122024");

        return view;
    }

    // Method to fetch task data from the database for a specific date
    void fetchTaskData(String dateId) {
        // Query the database to retrieve tasks for the specific date
        Cursor cursor = timelineDbHelper.readAllDataWithDate(dateId);

        // Clear existing task data
        task_start_time.clear();
        task_title.clear();
        task_description.clear();
        task_length.clear();

        if (cursor.getCount() != 0) {
            // If there are tasks for the selected date, add them to the lists
            while (cursor.moveToNext()) {
                task_start_time.add(cursor.getString(4)); // Task start time
                task_title.add(cursor.getString(1)); // Task title
                task_description.add(cursor.getString(2)); // Task description
                task_length.add(cursor.getString(5)); // Task length
            }
        }
        cursor.close();

        // Notify the adapter that the data has changed, so the RecyclerView is updated
        timelineAdapter.notifyDataSetChanged();
    }
}

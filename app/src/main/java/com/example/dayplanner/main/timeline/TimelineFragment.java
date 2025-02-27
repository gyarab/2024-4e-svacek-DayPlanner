package com.example.dayplanner.main.timeline;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dayplanner.R;
import com.example.dayplanner.main.habits.Habit;
import com.example.dayplanner.main.habits.HabitEntry;
import com.example.dayplanner.main.tasks.Task;
import com.example.dayplanner.main.tasks.TasksDBHelper;
import com.example.dayplanner.main.dayslist.WeeklyHeaderFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimelineFragment extends Fragment implements WeeklyHeaderFragment.OnDaySelectedListener {
    private RecyclerView timeLine;
    private TasksDBHelper tasksDBHelper;
    private List<TimelineItem> timelineItems;
    private TimelineAdapter timelineAdapter;
    private DatabaseReference habitsRef;
    private int pendingFetches = 0; // Tracks unfinished fetch operations
    // Store the currently selected date
    private String selectedDate = "25022025"; // default value; update as needed

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);

        timeLine = view.findViewById(R.id.timeLine);
        timeLine.setLayoutManager(new LinearLayoutManager(getContext()));

        timelineItems = new ArrayList<>();
        tasksDBHelper = new TasksDBHelper(getContext());
        timelineAdapter = new TimelineAdapter(getContext(), timelineItems);
        // Set the default date into the adapter
        timelineAdapter.setCurrentDate(selectedDate);
        timeLine.setAdapter(timelineAdapter);

        // Initially fetch tasks and habits for the default date.
        fetchTasksAndHabits(selectedDate);

        return view;
    }

    @Override
    public void onDaySelected(String dateId) {
        Log.d("HELLO", dateId);
        // Update the selected date
        selectedDate = dateId;

        // Update the adapter with the new date before fetching
        timelineAdapter.setCurrentDate(dateId);

        // Clear existing data before reloading
        timelineItems.clear();
        timelineAdapter.notifyDataSetChanged();

        // Re-fetch tasks and habits for the selected day.
        Log.d("DAY SELECTED", "Day selected: " + dateId);
        fetchTasksAndHabits(dateId);
    }

    public void fetchTasksAndHabits(String dateId) {
        Log.d("FetchTasksAndHabits", "Fetching tasks and habits for date: " + dateId);
        timelineItems.clear();
        pendingFetches = 2; // We are fetching both tasks and habits

        fetchTasks(dateId);
        fetchHabits(dateId);
    }

    private void fetchTasks(String dateId) {
        Log.d("FetchTasks", "Fetching tasks for date: " + dateId);
        List<Task> tasks = tasksDBHelper.getTasksByDate(dateId);
        if (tasks != null && !tasks.isEmpty()) {
            for (Task task : tasks) {
                timelineItems.add(new TimelineItem(task));
                Log.d("TimelineTasks", "Added task: " + task.toString());
            }
        } else {
            Log.d("FetchTasks", "No tasks found for date: " + dateId);
        }
        fetchComplete();
    }


    private void fetchHabits(String dateId) {
        Log.d("Fetching Habits", dateId);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        habitsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("habits");

        habitsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot habitSnapshot : snapshot.getChildren()) {
                    Map<String, Object> habitDataMap = (Map<String, Object>) habitSnapshot.getValue();
                    Log.d("HabitRawJSON", "Habit JSON data: " + habitDataMap);

                    Habit habit = habitSnapshot.getValue(Habit.class);
                    Log.d("habitsnapshot",  String.valueOf(habitSnapshot.getValue(Habit.class)));
                    Log.d("habit from snapshot",  habit.toString());
                    if (habit != null) {
                        // Check if the habit is visible on the given date
                        if (habit.isHabitVisible(dateId)) {
                            // Ensure the habit's entries map is initialized
                            Map<String, HabitEntry> entries = habit.getEntries();
                            if (entries == null) {
                                entries = new HashMap<>();
                                habit.setEntries(entries);
                            }
                            Log.d("defaultEntry", String.valueOf(habit.getGoalValue()));
                            // If no entry exists for the given dateId, add one with progress = 0
                            if (!entries.containsKey(dateId)) {
                                habit.setGoalValue(habit.getGoalValue());
                                HabitEntry defaultEntry = new HabitEntry(dateId, false, 0, habit.getGoalValue());
                                entries.put(dateId, defaultEntry);
                            }

                            // Add the habit (with the correct entries) to the timeline items list
                            timelineItems.add(new TimelineItem(habit));
                            Log.d("TimelineHabits", "Added habit: " + habit.toString());
                        }
                    }

                }
                fetchComplete();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to load habits", error.toException());
                fetchComplete();
            }
        });
    }




    private void fetchComplete() {
        pendingFetches--;
        if (pendingFetches == 0) {
            timelineAdapter.notifyDataSetChanged();
            Log.d("TimelineFragment", "Final timeline list: " + timelineItems);
        }
    }
}

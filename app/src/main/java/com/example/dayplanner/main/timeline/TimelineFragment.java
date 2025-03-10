package com.example.dayplanner.main.timeline;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dayplanner.R;
import com.example.dayplanner.main.habits.FirebaseHelper;
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
    private String selectedDate = "25022025"; //TODO: Dynamic

    FirebaseHelper firebaseHelper = new FirebaseHelper();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);

        timeLine = view.findViewById(R.id.timeLine);

        TimelineLayoutManager layoutManager = new TimelineLayoutManager(getContext());
        timeLine.setLayoutManager(layoutManager);

        timelineItems = new ArrayList<>();
        tasksDBHelper = new TasksDBHelper(getContext());
        timelineAdapter = new TimelineAdapter(getContext(), timelineItems);

        timelineAdapter.setCurrentDate(selectedDate);
        timeLine.setAdapter(timelineAdapter);

        /*ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new TimelineItemTouchHelperCallback(timelineAdapter, getContext()));
        itemTouchHelper.attachToRecyclerView(timeLine);*/

        fetchTasksAndHabits(selectedDate);

        return view;
    }

    @Override
    public void onDaySelected(String dateId) {
        Log.d("HELLO", dateId);
        selectedDate = dateId;

        timelineAdapter.setCurrentDate(dateId);

        timelineItems.clear();
        timelineAdapter.notifyDataSetChanged();

        Log.d("DAY SELECTED", "Day selected: " + dateId);
        fetchTasksAndHabits(dateId);
    }

    public void fetchTasksAndHabits(String dateId) {
        Log.d("FetchTasksAndHabits", "Fetching tasks and habits for date: " + dateId);
        timelineItems.clear();
        pendingFetches = 2; //ensures that habits and tasks show at the same time

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

        habitsRef = FirebaseHelper.getHabitsRef();

        habitsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot habitSnapshot : dataSnapshot.getChildren()) {
                    Habit habit = habitSnapshot.getValue(Habit.class);
                    if (habit != null && habit.isHabitVisibleOnDate(dateId)) {
                        Map<String, HabitEntry> entries = new HashMap<>();

                        if (habitSnapshot.hasChild("entries")) {
                            Log.d("FirebaseHelper", "Entries exist for habit");
                            boolean dateFound = false;

                            for (DataSnapshot entrySnapshot : habitSnapshot.child("entries").getChildren()) {
                                HabitEntry entry = entrySnapshot.getValue(HabitEntry.class);
                                if (entry != null && entry.getDate() != null) {
                                    entries.put(entry.getDate(), entry);
                                    Log.d("FirebaseHelper", "Adding entry: " + entry.toString());

                                    // Check if the desired date already exists
                                    if (entry.getDate().equals(dateId)) {
                                        dateFound = true;
                                    }
                                }
                            }

                            if (!dateFound) {
                                Log.d("FirebaseHelper", "Desired date not found, creating default entry");
                                HabitEntry newEntry = new HabitEntry(dateId, habit.getGoalValue(), 0, false);
                                entries.put(dateId, newEntry);
                                habitSnapshot.getRef().child("entries").child(dateId).setValue(newEntry)
                                        .addOnSuccessListener(aVoid -> Log.d("FirebaseHelper", "New entry uploaded: " + newEntry))
                                        .addOnFailureListener(e -> Log.e("FirebaseHelper", "Failed to upload entry: " + e.getMessage()));
                            }
                        } else {
                            Log.d("FirebaseHelper", "No entries found, creating default entry");
                            HabitEntry newEntry = new HabitEntry(dateId, habit.getGoalValue(), 0, false);
                            entries.put(dateId, newEntry);
                            habitSnapshot.getRef().child("entries").child(dateId).setValue(newEntry)
                                    .addOnSuccessListener(aVoid -> Log.d("FirebaseHelper", "New entry uploaded: " + newEntry))
                                    .addOnFailureListener(e -> Log.e("FirebaseHelper", "Failed to upload entry: " + e.getMessage()));
                        }

                        habit.setEntries(entries);
                        Log.d("FirebaseHelper", "Fetched Habit: " + habit.toString());

                        timelineItems.add(new TimelineItem(habit));
                        fetchComplete();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                fetchComplete();
                Log.e("FirebaseHelper", "Database Error: " + error.getMessage());
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

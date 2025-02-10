package com.example.dayplanner.main.timeline;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dayplanner.R;
import com.example.dayplanner.main.habits.Habit;
import com.example.dayplanner.main.tasks.TasksDBHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TimelineFragment extends Fragment {
    RecyclerView timeLine;
    TasksDBHelper tasksDBHelper;
    List<TimelineItem> timelineItems;
    TimelineAdapter timelineAdapter;
    DatabaseReference habitsRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);

        timeLine = view.findViewById(R.id.timeLine);
        timeLine.setLayoutManager(new LinearLayoutManager(getContext()));

        timelineItems = new ArrayList<>();
        tasksDBHelper = new TasksDBHelper(getContext());
        timelineAdapter = new TimelineAdapter(getContext(), timelineItems);
        timeLine.setAdapter(timelineAdapter);

        fetchTasksAndHabits("03012025");

        return view;
    }

    public void fetchTasksAndHabits(String dateId) {
        timelineItems.clear();
        fetchTasks(dateId);
        fetchHabits();
    }

    private void fetchTasks(String dateId) {
        Cursor cursor = tasksDBHelper.readAllDataWithDate(dateId);

        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                timelineItems.add(new TimelineItem(
                        cursor.getString(0),  // Task ID
                        cursor.getString(1),  // Task Title
                        cursor.getString(4)   // Task Start Time
                ));
            }
        }
        cursor.close();
        timelineAdapter.notifyDataSetChanged();
    }

    private void fetchHabits() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        habitsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("habits");

        habitsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot habitSnapshot : snapshot.getChildren()) {
                    Habit habit = habitSnapshot.getValue(Habit.class);
                    if (habit != null) {
                        timelineItems.add(new TimelineItem(habit));
                    }
                }
                timelineAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to load habits", error.toException());
            }
        });
    }
}

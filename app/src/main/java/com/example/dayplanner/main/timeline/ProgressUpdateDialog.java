package com.example.dayplanner.main.timeline;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.example.dayplanner.R;
import com.example.dayplanner.main.habits.Habit;
import com.example.dayplanner.main.habits.HabitEntry;
import com.example.dayplanner.statistics.CustomCircularProgressBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProgressUpdateDialog extends Dialog {
    private Habit habit;
    private String currentDate;
    private DatabaseReference databaseReference;
    private CustomCircularProgressBar progressBar;
    private TextView habitNameText;
    private EditText progressInput;
    private int currentProgress;
    private int goal;
    DatabaseReference habitsRef;
    FirebaseAuth auth;

    public interface OnProgressUpdatedListener {
        void onProgressUpdated();
    }

    private OnProgressUpdatedListener progressUpdatedListener;

    public void setOnProgressUpdatedListener(OnProgressUpdatedListener listener) {
        this.progressUpdatedListener = listener;
    }

    public ProgressUpdateDialog(@NonNull Context context, Habit habit, String currentDate) {
        super(context);
        this.habit = habit;
        this.currentDate = currentDate;
        databaseReference = FirebaseDatabase.getInstance().getReference("habits");
        currentProgress = habit.getEntryForDate(currentDate).getProgress();
        goal = habit.getEntryForDate(currentDate).getEntryGoalValue();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_progress_update);

        progressBar = findViewById(R.id.circularProgressBar);
        habitNameText = findViewById(R.id.habitNameText);
        progressInput = findViewById(R.id.progressInput);
        Button addButton = findViewById(R.id.addButton);
        Button setButton = findViewById(R.id.setButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        habitNameText.setText(habit.getName());
        progressBar.setMaxProgress(goal);
        progressBar.setProgress(currentProgress);
        progressBar.setText(currentProgress + "/" + goal + "  " + habit.getMetric());

        addButton.setOnClickListener(view -> updateProgress(true));
        setButton.setOnClickListener(view -> updateProgress(false));
        cancelButton.setOnClickListener(view -> dismiss());
    }

    private void updateProgress(boolean isAdding) {
        String inputText = progressInput.getText().toString();
        if (inputText.isEmpty()) {
            Toast.makeText(getContext(), "Enter a value", Toast.LENGTH_SHORT).show();
            return;
        }

        float inputProgress = Float.parseFloat(inputText);
        if (isAdding) {
            currentProgress += inputProgress;
        } else {
            currentProgress = (int) inputProgress;
        }
/*
        if (currentProgress > goal) {
            currentProgress = goal;
        }*/

        saveHabit();
        progressBar.setProgress(currentProgress, currentProgress + "/" + goal + "  " + habit.getMetric());
        saveProgressToFirebase();
    }

    private void saveHabit() {
        HabitEntry newHabitEntry = new HabitEntry(currentDate, goal, currentProgress, currentProgress >= goal);
        habit.setEntryForDate(currentDate, newHabitEntry);
    }

    private void saveProgressToFirebase() {
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            habitsRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(currentUser.getUid())
                    .child("habits");
        } else {
            habitsRef = null;
            Log.e("Firebase", "User not logged in, habitsRef is null");
        }

        habitsRef.child(habit.getId()).child("entries").child(currentDate).child("progress")
                .setValue(currentProgress)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Progress updated!", Toast.LENGTH_SHORT).show();
                    if (progressUpdatedListener != null) {
                        progressUpdatedListener.onProgressUpdated();
                    }
                    dismiss();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update", Toast.LENGTH_SHORT).show());
    }
}

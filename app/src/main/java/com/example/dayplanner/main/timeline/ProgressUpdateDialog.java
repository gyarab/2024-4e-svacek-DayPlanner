package com.example.dayplanner.main.timeline;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.example.dayplanner.R;
import com.example.dayplanner.main.habits.Habit;
import com.example.dayplanner.statistics.CustomCircularProgressBar;
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
    private float currentProgress;
    private float maxProgress;

    public ProgressUpdateDialog(@NonNull Context context, Habit habit, String currentDate) {
        super(context);
        this.habit = habit;
        this.currentDate = currentDate;
        databaseReference = FirebaseDatabase.getInstance().getReference("habits");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_progress_update);

        int goal = habit.getEntryForDate(currentDate).getEntryGoalValue();
        int progress = habit.getEntryForDate(currentDate).getProgress();

        progressBar = findViewById(R.id.circularProgressBar);
        habitNameText = findViewById(R.id.habitNameText);
        progressInput = findViewById(R.id.progressInput);
        Button addButton = findViewById(R.id.addButton);
        Button setButton = findViewById(R.id.setButton);
        Button cancelButton = findViewById(R.id.cancelButton);

        habitNameText.setText(habit.getName()); // Set habit name
        progressBar.setMaxProgress(goal);

        float percentage = (float) progress / goal;
        int percentageInt = Math.round(percentage * 100);

        progressBar.setProgress(percentageInt);
        progressBar.setText(progress + "/" + goal + "  " + habit.getMetric());

        Log.d("ProgressUpdateDialog", "percentage: " + percentage + ", Goal: " + goal + ", Progress: " + progress);

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
            currentProgress = inputProgress;
        }

        if (currentProgress > maxProgress) {
            currentProgress = maxProgress;
        }

        progressBar.setProgress(currentProgress);
        saveProgressToFirebase();
    }

    private void saveProgressToFirebase() {
        databaseReference.child(habit.getId()).child("progress").child(currentDate)
                .setValue(currentProgress)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Progress updated!", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update", Toast.LENGTH_SHORT).show());
    }
}

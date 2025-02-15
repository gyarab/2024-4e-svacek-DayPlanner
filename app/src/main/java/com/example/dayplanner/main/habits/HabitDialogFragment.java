package com.example.dayplanner.main.habits;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.dayplanner.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HabitDialogFragment extends DialogFragment {

    private EditText editHabitName, editHabitDescription, editHabitLength, editCustomMetric, editGoalValue;
    private TextView editStartTime;
    private Spinner frequencySpinner, metricSpinner;
    private Button saveButton, pickTimeButton;

    public HabitDialogFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_habit_dialog, container, false);

        editHabitName = view.findViewById(R.id.edit_habit_name);
        editHabitDescription = view.findViewById(R.id.edit_habit_description);
        editHabitLength = view.findViewById(R.id.edit_habit_length);
        editStartTime = view.findViewById(R.id.edit_habit_start_time);
        editCustomMetric = view.findViewById(R.id.edit_custom_metric);
        editGoalValue = view.findViewById(R.id.edit_goal_value);
        frequencySpinner = view.findViewById(R.id.spinner_habit_frequency);
        metricSpinner = view.findViewById(R.id.spinner_habit_metric);
        pickTimeButton = view.findViewById(R.id.pick_habit_time_button);
        saveButton = view.findViewById(R.id.save_habit_button);

        // Time Picker
        pickTimeButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view12, hourOfDay, minute) -> {
                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                editStartTime.setText(selectedTime);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        });

        // Save Habit
        saveButton.setOnClickListener(v -> saveHabitToFirebase());

        return view;
    }

    private void saveHabitToFirebase() {
        String habitName = editHabitName.getText().toString();
        String habitDescription = editHabitDescription.getText().toString();
        String habitLengthStr = editHabitLength.getText().toString();
        String startTime = editStartTime.getText().toString();
        String frequency = frequencySpinner.getSelectedItem().toString();
        String metric = metricSpinner.getSelectedItem().toString();
        String goalValueStr = editGoalValue.getText().toString();

        if (metric.equals("Custom")) {
            metric = editCustomMetric.getText().toString();
        }

        if (habitName.isEmpty() || startTime.isEmpty() || habitLengthStr.isEmpty() || goalValueStr.isEmpty()) {
            Log.e("HabitDialog", "Fields cannot be empty");
            return;
        }

        int habitLength = Integer.parseInt(habitLengthStr);
        int goalValue = Integer.parseInt(goalValueStr);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference habitsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("habits");

        String habitId = habitsRef.push().getKey();
        Habit habit = new Habit(habitId, habitName, habitDescription, frequency, startTime, habitLength, metric, goalValue);

        habitsRef.child(habitId).setValue(habit)
                .addOnSuccessListener(aVoid -> {
                    Log.d("HabitDialog", "Habit saved successfully");
                    dismiss(); // Dismiss dialog only after the habit is successfully stored
                })
                .addOnFailureListener(e -> Log.e("HabitDialog", "Failed to save habit", e));
    }



    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }
}

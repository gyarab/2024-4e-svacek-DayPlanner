package com.example.dayplanner.main.habits;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
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
import java.util.Locale;

public class HabitDialogFragment extends DialogFragment {
    private EditText editHabitName, editHabitDescription, editHabitLength, editGoalValue;
    private TextView editStartTime, editStartDate;
    private Spinner frequencySpinner, metricSpinner;
    private Button saveHabitButton, deleteHabitButton, pickTimeButton, pickDateButton;
    private DatabaseReference habitsRef;
    private FirebaseAuth auth;
    private Habit habit;
    private boolean isEditMode;
    private Calendar selectedDate = Calendar.getInstance(); // Store selected date for the date picker

    public HabitDialogFragment(boolean isEditMode, Habit habit) {
        this.isEditMode = isEditMode;
        this.habit = habit;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_habit_dialog, container, false);

        editHabitName = view.findViewById(R.id.edit_habit_name);
        editHabitDescription = view.findViewById(R.id.edit_habit_description);
        //editHabitLength = view.findViewById(R.id.edit_habit_length);
        editStartTime = view.findViewById(R.id.edit_start_time);
        editStartDate = view.findViewById(R.id.edit_start_date);
        editGoalValue = view.findViewById(R.id.edit_goal_value);
        frequencySpinner = view.findViewById(R.id.spinner_frequency);
        metricSpinner = view.findViewById(R.id.spinner_metric);
        saveHabitButton = view.findViewById(R.id.save_habit_button);
        deleteHabitButton = view.findViewById(R.id.delete_habit_button);
        pickTimeButton = view.findViewById(R.id.pick_habit_time_button);
        pickDateButton = view.findViewById(R.id.pick_habit_date_button);

        auth = FirebaseAuth.getInstance();
        habitsRef = FirebaseDatabase.getInstance().getReference("users").child(auth.getCurrentUser().getUid()).child("habits");

        if (isEditMode) {
            populateFields();
            deleteHabitButton.setVisibility(View.VISIBLE);
            deleteHabitButton.setOnClickListener(v -> showDeleteConfirmationDialog(habit.getId()));
        }

        saveHabitButton.setOnClickListener(v -> saveHabitToFirebase());

        // Date Picker
        pickDateButton.setOnClickListener(v -> {
            int year = selectedDate.get(Calendar.YEAR);
            int month = selectedDate.get(Calendar.MONTH);
            int day = selectedDate.get(Calendar.DAY_OF_MONTH);

            new DatePickerDialog(getContext(), (DatePicker view1, int selectedYear, int selectedMonth, int selectedDay) -> {
                selectedDate.set(selectedYear, selectedMonth, selectedDay);
                SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
                editStartDate.setText(dateFormat.format(selectedDate.getTime())); // Update TextView
            }, year, month, day).show();
        });

        // Time Picker
        pickTimeButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view12, hourOfDay, minute) -> {
                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                editStartTime.setText(selectedTime); // Update TextView with selected time
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        });

        return view;
    }

    private void populateFields() {
        if (habit != null) {
            editHabitName.setText(habit.getName());
            editHabitDescription.setText(habit.getDescription());
            //editHabitLength.setText(String.valueOf(habit.getLength()));
            editStartTime.setText(habit.getStartTime());
            editStartDate.setText(habit.getStartDate());
            editGoalValue.setText(String.valueOf(habit.getGoalValue()));
            setSpinnerSelection(frequencySpinner, habit.getFrequency());
            setSpinnerSelection(metricSpinner, habit.getMetric());
        }
    }

    private void saveHabitToFirebase() {
        String habitId = isEditMode ? habit.getId() : habitsRef.push().getKey();

        if (habitId == null) {
            Log.e("saveHabitToFirebase", "Error: habitId is null");
            return; // Stop execution if habitId is null
        }

        // Get input values
        String name = editHabitName.getText().toString().trim();
        String description = editHabitDescription.getText().toString().trim();
        String frequency = frequencySpinner.getSelectedItem().toString();
        String startDate = editStartDate.getText().toString().trim();
        String startTime = editStartTime.getText().toString().trim();
        String metric = metricSpinner.getSelectedItem().toString();

        // Handle potential empty number fields safely
        /*int length = editHabitLength.getText().toString().trim().isEmpty() ? 0 :
                Integer.parseInt(editHabitLength.getText().toString().trim());*/

        int goalValue = editGoalValue.getText().toString().trim().isEmpty() ? 0 :
                Integer.parseInt(editGoalValue.getText().toString().trim());

        // Create new Habit object
        Habit newHabit = new Habit(
                habitId,
                name,
                description,
                frequency,
                startDate,
                startTime,
                metric,
                goalValue
        );

        // Save to Firebase
        habitsRef.child(habitId).setValue(newHabit)
                .addOnSuccessListener(aVoid -> {
                    Log.d("saveHabitToFirebase", "Habit saved successfully");
                    dismiss(); // Dismiss the dialog after saving the habit
                })
                .addOnFailureListener(e -> Log.e("saveHabitToFirebase", "Error saving habit", e));
        dismiss();
    }

    private void showDeleteConfirmationDialog(String habitId) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Habit")
                .setMessage("Are you sure you want to delete this habit?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    habitsRef.child(habitId).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Log.d("HabitDialog", "Habit deleted successfully");
                                dismiss();
                            })
                            .addOnFailureListener(e -> Log.e("HabitDialog", "Failed to delete habit", e));
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }
}

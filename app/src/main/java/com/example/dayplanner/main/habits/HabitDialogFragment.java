package com.example.dayplanner.main.habits;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
    private GoalManager goalManager;
    private String currentDate;
    private String formattedDate;


    public HabitDialogFragment(boolean isEditMode, Habit habit) {
        this.isEditMode = isEditMode;
        this.habit = habit;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the dialog style to enable custom animations and full width
        if (getArguments() != null) {
            currentDate = getArguments().getString("currentDate");
        }

        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.BottomSheetDialogTheme);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();

            // Set the dialog to appear at the bottom
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.BOTTOM;
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);

            // Apply animation to the dialog
            window.setWindowAnimations(R.style.BottomDialogAnimation);

            // Apply animation to the view
            View view = getDialog().findViewById(R.id.habit_dialog_root);
            if (view != null) {
                Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
                view.startAnimation(slideUp);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_habit_dialog, container, false);

        //TODO: delete the habit length from xml
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
        goalManager = new GoalManager();

        auth = FirebaseAuth.getInstance();
        habitsRef = FirebaseDatabase.getInstance().getReference("users").child(auth.getCurrentUser().getUid()).child("habits");

        if (isEditMode) {
            populateFields();
            deleteHabitButton.setVisibility(View.VISIBLE);
            deleteHabitButton.setOnClickListener(v -> showDeleteConfirmationDialog(habit.getId()));
        }

        saveHabitButton.setOnClickListener(v -> {
            if (isEditMode) {
                updateHabitInFirebase();
            } else {
                saveNewHabitToFirebase();
            }
        });


        /** Date Picker **/
        pickDateButton.setOnClickListener(v -> {
            int year = selectedDate.get(Calendar.YEAR);
            int month = selectedDate.get(Calendar.MONTH);
            int day = selectedDate.get(Calendar.DAY_OF_MONTH);

            new DatePickerDialog(getContext(), (DatePicker view1, int selectedYear, int selectedMonth, int selectedDay) -> {
                selectedDate.set(selectedYear, selectedMonth, selectedDay);
                SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
                formattedDate = dateFormat.format(selectedDate.getTime());
                editStartDate.setText(formattedDate); // Update TextView
            }, year, month, day).show();
        });

        /** Time Picker **/
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

    private void saveNewHabitToFirebase() {
        String name = editHabitName.getText().toString().trim();
        String description = editHabitDescription.getText().toString().trim();
        String frequency = frequencySpinner.getSelectedItem().toString();
        String startDate = editStartDate.getText().toString().trim();
        String startTime = editStartTime.getText().toString().trim();
        String metric = metricSpinner.getSelectedItem().toString();

        int goalValue = editGoalValue.getText().toString().trim().isEmpty() ? 0 :
                Integer.parseInt(editGoalValue.getText().toString().trim());

        if (name.isEmpty() || startDate.isEmpty() || startTime.isEmpty()) {
            Log.e("saveNewHabitToFirebase", "Missing required fields");
            return;
        }

        String habitId = habitsRef.push().getKey();
        if (habitId == null) {
            Log.e("saveNewHabitToFirebase", "Failed to generate habit ID");
            return;
        }

        Log.d("savehb", "date " + formattedDate + " goal value " + goalValue);

        Habit newHabit = new Habit(habitId, name, description, frequency, startDate, startTime, metric, goalValue);
        newHabit.setGoalHistory(new HashMap<>());  // Ensure goalHistory is empty initially
        newHabit.addGoalHistory(formattedDate, goalValue);

        habitsRef.child(habitId).setValue(newHabit)
                .addOnSuccessListener(aVoid -> dismiss())
                .addOnFailureListener(e -> Log.e("saveNewHabitToFirebase", "Error saving habit", e));
    }

    private void updateHabitInFirebase() {
        if (habit == null || habit.getId() == null) {
            Log.e("updateHabitInFirebase", "Habit ID is null");
            return;
        }

        String name = editHabitName.getText().toString().trim();
        String description = editHabitDescription.getText().toString().trim();
        String frequency = frequencySpinner.getSelectedItem().toString();
        String startDate = editStartDate.getText().toString().trim();
        String startTime = editStartTime.getText().toString().trim();
        String metric = metricSpinner.getSelectedItem().toString();

        int newGoalValue = editGoalValue.getText().toString().trim().isEmpty() ? 0 :
                Integer.parseInt(editGoalValue.getText().toString().trim());

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("name", name);
        updateMap.put("description", description);
        updateMap.put("frequency", frequency);
        updateMap.put("startDate", startDate);
        updateMap.put("startTime", startTime);
        updateMap.put("metric", metric);
        updateMap.put("goalValue", newGoalValue);

        habitsRef.child(habit.getId()).updateChildren(updateMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d("updateHabitInFirebase", "Habit updated successfully");

                    // ✅ Call GoalManager to update goal values in future entries and history
                    goalManager.updateGoalValue(habit.getId(), newGoalValue, currentDate);

                    dismiss();
                })
                .addOnFailureListener(e -> Log.e("updateHabitInFirebase", "Failed to update habit", e));
    }




    private void updateFutureEntriesGoalValue(String habitId, String startDate, int newGoalValue) {
        DatabaseReference entriesRef = habitsRef.child(habitId).child("entries");

        entriesRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                for (DataSnapshot entrySnapshot : snapshot.getChildren()) {
                    String entryDate = entrySnapshot.getKey();

                    // Only update future entries
                    if (entryDate.compareTo(startDate) >= 0) {
                        entrySnapshot.getRef().child("entryGoalValue").setValue(newGoalValue);
                    }
                }
                Log.d("updateFutureEntries", "Future goal values updated successfully.");
            }
        }).addOnFailureListener(e -> Log.e("updateFutureEntries", "Error updating future entries", e));
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
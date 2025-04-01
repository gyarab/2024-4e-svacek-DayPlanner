package com.example.dayplanner.main.habits;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.dayplanner.R;
import com.example.dayplanner.main.dayslist.WeeklyHeaderFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
    private Calendar selectedDate = Calendar.getInstance();
    private GoalManager goalManager;
    private String currentDate;
    private String formattedDate;
    private WeeklyHeaderFragment weeklyHeaderFragment;
    private TextInputEditText editCustomMetric;
    private TextInputLayout customMetricLayout;

    private LinearLayout customFrequencyLayout;
    private CheckBox mondayCheckbox, tuesdayCheckbox, wednesdayCheckbox, thursdayCheckbox,
            fridayCheckbox, saturdayCheckbox, sundayCheckbox;
    private static final String CUSTOM_FREQUENCY = "Custom";
    private static final String CUSTOM_METRIC = "Custom";


    public HabitDialogFragment(boolean isEditMode, Habit habit) {
        this.isEditMode = isEditMode;
        this.habit = habit;
    }

    public interface HabitDialogListener {
        void onHabitDataChanged(String dateId);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

            // Set window to be transparent
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Full width, at the bottom
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.BOTTOM;
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);

            // Apply slide-up animation
            window.setWindowAnimations(R.style.BottomDialogAnimation);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_habit_dialog, container, false);

        editHabitName = view.findViewById(R.id.edit_habit_name);
        editHabitDescription = view.findViewById(R.id.edit_habit_description);
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
        weeklyHeaderFragment = new WeeklyHeaderFragment();

        editCustomMetric = view.findViewById(R.id.edit_custom_metric);
        customMetricLayout = (TextInputLayout) editCustomMetric.getParent().getParent();

        // Initialize custom frequency components
        customFrequencyLayout = view.findViewById(R.id.custom_frequency_layout);
        mondayCheckbox = view.findViewById(R.id.checkbox_monday);
        tuesdayCheckbox = view.findViewById(R.id.checkbox_tuesday);
        wednesdayCheckbox = view.findViewById(R.id.checkbox_wednesday);
        thursdayCheckbox = view.findViewById(R.id.checkbox_thursday);
        fridayCheckbox = view.findViewById(R.id.checkbox_friday);
        saturdayCheckbox = view.findViewById(R.id.checkbox_saturday);
        sundayCheckbox = view.findViewById(R.id.checkbox_sunday);

        // Set up frequency spinner listener
        frequencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFrequency = parent.getItemAtPosition(position).toString();
                if (selectedFrequency.equals(CUSTOM_FREQUENCY)) {
                    customFrequencyLayout.setVisibility(View.VISIBLE);
                } else {
                    customFrequencyLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                customFrequencyLayout.setVisibility(View.GONE);
            }
        });

        metricSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMetric = parent.getItemAtPosition(position).toString();
                if (selectedMetric.equals("Custom")) {
                    customMetricLayout.setVisibility(View.VISIBLE);
                } else {
                    customMetricLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                customMetricLayout.setVisibility(View.GONE);
            }
        });

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
                editStartDate.setText(formattedDate);
            }, year, month, day).show();
        });

        /** Time Picker **/
        pickTimeButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view12, hourOfDay, minute) -> {
                String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                editStartTime.setText(selectedTime);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        });

        return view;
    }

    private void populateFields() {
        if (habit != null) {
            editHabitName.setText(habit.getName());
            editHabitDescription.setText(habit.getDescription());
            editStartTime.setText(habit.getStartTime());
            editStartDate.setText(habit.getStartDate());
            editGoalValue.setText(String.valueOf(habit.getGoalValue()));
            setSpinnerSelection(frequencySpinner, habit.getFrequency());
            setSpinnerSelection(metricSpinner, habit.getMetric());

            String frequency = habit.getFrequency();
            if (frequency != null && frequency.startsWith("Custom:")) {
                setSpinnerSelection(frequencySpinner, CUSTOM_FREQUENCY);
                customFrequencyLayout.setVisibility(View.VISIBLE);

                // Parse and set checkbox states for custom frequency
                setCustomFrequencyCheckboxes(frequency);
            } else {
                setSpinnerSelection(frequencySpinner, frequency);
                customFrequencyLayout.setVisibility(View.GONE);
            }

            // Check if the habit has a custom metric
            String metric = habit.getMetric();
            boolean isCustom = true;

            for (int i = 0; i < metricSpinner.getCount(); i++) {
                if (metricSpinner.getItemAtPosition(i).toString().equalsIgnoreCase(metric)) {
                    isCustom = false;
                    break;
                }
            }

            if (isCustom) {
                // Set spinner to "Custom" and show the custom metric field
                setSpinnerSelection(metricSpinner, "Custom");
                editCustomMetric.setText(metric);
                customMetricLayout.setVisibility(View.VISIBLE);
            } else {
                setSpinnerSelection(metricSpinner, metric);
                customMetricLayout.setVisibility(View.GONE);
            }

            if (isCustom) {
                // Set spinner to "Custom" and show the custom metric field
                setSpinnerSelection(metricSpinner, CUSTOM_METRIC);
                editCustomMetric.setText(metric);
                customMetricLayout.setVisibility(View.VISIBLE);
            } else {
                setSpinnerSelection(metricSpinner, metric);
                customMetricLayout.setVisibility(View.GONE);
            }
        }
    }

    private void setCustomFrequencyCheckboxes(String customFrequency) {
        // Format is "Custom:MTWTFSS" where each letter position represents a day and is either the letter (selected) or '-' (not selected)
        if (customFrequency != null && customFrequency.startsWith("Custom:") && customFrequency.length() >= 8) {
            String daysPattern = customFrequency.substring(7); // Skip "Custom:"

            mondayCheckbox.setChecked(daysPattern.charAt(0) == 'M');
            tuesdayCheckbox.setChecked(daysPattern.charAt(1) == 'T');
            wednesdayCheckbox.setChecked(daysPattern.charAt(2) == 'W');
            thursdayCheckbox.setChecked(daysPattern.charAt(3) == 'T');
            fridayCheckbox.setChecked(daysPattern.charAt(4) == 'F');
            saturdayCheckbox.setChecked(daysPattern.charAt(5) == 'S');
            sundayCheckbox.setChecked(daysPattern.charAt(6) == 'S');
        }
    }

    private String getCustomFrequencyString() {
        StringBuilder customFrequency = new StringBuilder("Custom:");

        // Append day codes based on checkbox selection
        customFrequency.append(mondayCheckbox.isChecked() ? "M" : "-");
        customFrequency.append(tuesdayCheckbox.isChecked() ? "T" : "-");
        customFrequency.append(wednesdayCheckbox.isChecked() ? "W" : "-");
        customFrequency.append(thursdayCheckbox.isChecked() ? "T" : "-");
        customFrequency.append(fridayCheckbox.isChecked() ? "F" : "-");
        customFrequency.append(saturdayCheckbox.isChecked() ? "S" : "-");
        customFrequency.append(sundayCheckbox.isChecked() ? "S" : "-");

        return customFrequency.toString();
    }

    private boolean isAnyDaySelected() {
        return mondayCheckbox.isChecked() || tuesdayCheckbox.isChecked() ||
                wednesdayCheckbox.isChecked() || thursdayCheckbox.isChecked() ||
                fridayCheckbox.isChecked() || saturdayCheckbox.isChecked() ||
                sundayCheckbox.isChecked();
    }

    private void saveNewHabitToFirebase() {
        String name = editHabitName.getText().toString().trim();
        String description = editHabitDescription.getText().toString().trim();
        String frequency = frequencySpinner.getSelectedItem().toString();
        String startDate = editStartDate.getText().toString().trim();
        String startTime = editStartTime.getText().toString().trim();
        String metric = metricSpinner.getSelectedItem().toString();
        String goalValueString = editGoalValue.getText().toString().trim();
        int goalValue = editGoalValue.getText().toString().trim().isEmpty() ? 0 :
                Integer.parseInt(editGoalValue.getText().toString().trim());

        // Handle custom frequency
        if (frequency.equals(CUSTOM_FREQUENCY)) {
            if (!isAnyDaySelected()) {
                Toast.makeText(getContext(), "Please select at least one day of the week", Toast.LENGTH_SHORT).show();
                return;
            }
            frequency = getCustomFrequencyString();
        }

        // Handle custom metric
        if (metric.equals(CUSTOM_METRIC)) {
            String customMetric = editCustomMetric.getText().toString().trim();
            if (!customMetric.isEmpty()) {
                metric = customMetric;
            } else {
                Toast.makeText(getContext(), "Please enter a custom metric", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (name.isEmpty() || startDate.equals("Select Start Date") || startTime.equals("Select Start Time") || goalValueString.isEmpty()) {
            Log.e("saveNewHabitToFirebase", "Missing required fields");
            Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String habitId = habitsRef.push().getKey();
        if (habitId == null) {
            Log.e("saveNewHabitToFirebase", "Failed to generate habit ID");
            return;
        }

        Log.d("savehb", "date " + formattedDate + " goal value " + goalValue);

        Habit newHabit = new Habit(habitId, name, description, frequency, startDate, startTime, metric, goalValue);
        newHabit.setGoalHistory(new HashMap<>());
        newHabit.addGoalHistory(formattedDate, goalValue);

        if (getActivity() instanceof HabitDialogFragment.HabitDialogListener) {
            ((HabitDialogFragment.HabitDialogListener) getActivity()).onHabitDataChanged(startDate);
        }

        Log.d("NAVIGATE s", startDate);
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

        // Handle custom frequency
        if (frequency.equals(CUSTOM_FREQUENCY)) {
            if (!isAnyDaySelected()) {
                Toast.makeText(getContext(), "Please select at least one day of the week", Toast.LENGTH_SHORT).show();
                return;
            }
            frequency = getCustomFrequencyString();
        }

        // Handle custom metric
        if (metric.equals(CUSTOM_METRIC)) {
            String customMetric = editCustomMetric.getText().toString().trim();
            if (!customMetric.isEmpty()) {
                metric = customMetric;
            } else {
                Toast.makeText(getContext(), "Please enter a custom metric", Toast.LENGTH_SHORT).show();
                return;
            }
        }

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

                    goalManager.updateGoalValue(habit.getId(), newGoalValue, currentDate);

                    dismiss();
                })
                .addOnFailureListener(e -> Log.e("updateHabitInFirebase", "Failed to update habit", e));

        if ((currentDate != null) && (getActivity() instanceof HabitDialogFragment.HabitDialogListener)) {
            ((HabitDialogFragment.HabitDialogListener) getActivity()).onHabitDataChanged(currentDate);
        }
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
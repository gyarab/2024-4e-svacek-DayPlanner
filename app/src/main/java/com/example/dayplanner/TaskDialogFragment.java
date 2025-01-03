package com.example.dayplanner;

import static com.example.dayplanner.AddTaskActivity.formatDate;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class TaskDialogFragment extends DialogFragment {

    private boolean isEditMode;
    private String taskID, startTime, taskDate, title, description, length;

    public TaskDialogFragment(boolean isEditMode, String taskID, String startTime, String taskDate, String title, String description, String length) {
        this.isEditMode = isEditMode;
        this.taskID = taskID;
        this.startTime = startTime;
        this.taskDate = taskDate;
        this.title = title;
        this.description = description;
        this.length = length;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_edit_task, container, false);

        EditText editTaskTitle = view.findViewById(R.id.edit_task_title);
        EditText editTaskDescription = view.findViewById(R.id.edit_task_description);
        EditText editTaskLength = view.findViewById(R.id.edit_task_length);
        TextView editTaskDate = view.findViewById(R.id.edit_task_date);
        TextView editTaskTime = view.findViewById(R.id.edit_task_time);
        Button pickDateButton = view.findViewById(R.id.pick_date_button);
        Button pickTimeButton = view.findViewById(R.id.pick_time_button);
        Button saveButton = view.findViewById(R.id.save_task_button);

        // Populate fields only in edit mode
        if (isEditMode) {
            editTaskTitle.setText(title);
            editTaskDescription.setText(description);
            editTaskLength.setText(length);
            editTaskDate.setText(taskDate);
            editTaskTime.setText(startTime);
        }

        // Date Picker
        pickDateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view1, year, month, dayOfMonth) -> {
                String selectedDate = dayOfMonth + "." + (month + 1) + "." + year;
                editTaskDate.setText(selectedDate);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        // Time Picker
        pickTimeButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view12, hourOfDay, minute) -> {
                String selectedTime = String.format("%02d:%02d", hourOfDay, minute);
                editTaskTime.setText(selectedTime);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        });

        // Set up save button
        saveButton.setOnClickListener(v -> {
            // Get input values
            String newTitle = editTaskTitle.getText().toString();
            String newDescription = editTaskDescription.getText().toString();
            String newLength = editTaskLength.getText().toString();
            String newDate = editTaskDate.getText().toString();
            newDate = formatDate(newDate);
            String newTime = editTaskTime.getText().toString();

            TasksDBHelper dbHelper = new TasksDBHelper(getContext());
            if (isEditMode) {
                // Update the database
                Log.d("Task Edited", "ID: " + taskID + ", Start Time: " + newTime + ", Date: " + newDate +
                        ", Title: " + newTitle + ", Desc: " + newDescription + ", Length: " + newLength);
            } else {
                        // Insert into the database
                        dbHelper.addTask(newTitle, newDescription, newDate, newTime, Integer.parseInt(newLength));
                        Log.d("Task Added", "Start Time: " + newTime + ", Date: " + newDate +
                                ", Title: " + newTitle + ", Desc: " + newDescription + ", Length: " + newLength);
                    }

                    dismiss(); // Close the dialog
                });

                return view;
            }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        // Optional: Handle actions when the dialog is dismissed
    }

    public static String formatDate(String date) {
        String formattedDate = "";
        Log.d("formattedDateB", date);
        if (date != null && !date.isEmpty()) {
            // Split the input date by dots
            String[] parts = date.split("\\."); // Escape the dot since it's a regex special character
            if (parts.length == 3) { // Ensure the date is in the format DD.MM.YYYY
                // Parse and zero-pad day and month
                String day = parts[0].length() == 1 ? "0" + parts[0] : parts[0];   // Ensure 2-digit day
                String month = parts[1].length() == 1 ? "0" + parts[1] : parts[1]; // Ensure 2-digit month
                String year = parts[2];
                formattedDate = day + month + year; // Rearrange to YYYYMMDD
            }
        }
        Log.d("formattedDate", formattedDate);
        return formattedDate;
    }

}

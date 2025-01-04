package com.example.dayplanner;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
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
import androidx.core.content.ContextCompat;
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

    public interface TaskDialogListener {
        void onTaskDataChanged(String dateId);
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
            taskDate = formatDateForUser(taskDate);

            editTaskTitle.setText(title);
            editTaskDescription.setText(description);
            editTaskLength.setText(length);
            editTaskDate.setText(taskDate);
            editTaskTime.setText(startTime);

            Button deleteButton = view.findViewById(R.id.delete_task_button);
            Drawable icon = ContextCompat.getDrawable(this.getContext(), R.drawable.delete_icon);
            deleteButton.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
            deleteButton.setVisibility(View.VISIBLE); //visible only when edit mode
            deleteButton.setOnClickListener(v -> {
                showDeleteConfirmationDialog(taskID, taskDate);
            });
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
            String taskTitle = editTaskTitle.getText().toString();
            String taskDescription = editTaskDescription.getText().toString();
            String taskLength = editTaskLength.getText().toString();
            String taskDate = editTaskDate.getText().toString();
            taskDate = formatDateforDB(taskDate);
            String taskStartTime = editTaskTime.getText().toString();

            TasksDBHelper dbHelper = new TasksDBHelper(getContext());
            if (isEditMode) {
                // Update the database
                dbHelper.editTask(taskID, taskTitle, taskDescription, taskDate, taskStartTime, Integer.parseInt(taskLength));
                Log.d("Task Edited", "ID: " + taskID + ", Start Time: " + taskStartTime + ", Date: " + taskDate +
                        ", Title: " + taskTitle + ", Desc: " + taskDescription + ", Length: " + taskLength);
            } else {
                // Insert into the database
                dbHelper.addTask(taskTitle, taskDescription, taskDate, taskStartTime, Integer.parseInt(taskLength));
                Log.d("Task Added", "Start Time: " + taskStartTime + ", Date: " + taskDate +
                        ", Title: " + taskTitle + ", Desc: " + taskDescription + ", Length: " + taskLength);
            }

            if (getActivity() instanceof TaskDialogListener) {
                ((TaskDialogListener) getActivity()).onTaskDataChanged(taskDate);
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

    public static String formatDateforDB(String date) {
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

    public static String formatDateForUser(String date) {
        String formattedDate = "";
        Log.d("formattedDateB", date);

        if (date != null && date.length() == 8) { // Ensure the date is in the correct format (DDMMYYYY)
            // Extract day, month, and year using substring
            String day = date.substring(0, 2); // First two characters for day
            String month = date.substring(2, 4); // Next two characters for month
            String year = date.substring(4); // Remaining characters for year

            // Remove leading zeros from day and month for user-friendly format
            day = day.startsWith("0") ? day.substring(1) : day;
            month = month.startsWith("0") ? month.substring(1) : month;

            // Combine into user-friendly format
            formattedDate = day + "." + month + "." + year;
        } else {
            Log.d("formattedDateError", "Invalid date format: " + date);
        }

        Log.d("formattedDate", formattedDate);
        return formattedDate;
    }

    public void showDeleteConfirmationDialog(String taskId, String taskDate) {
        new AlertDialog.Builder(this.getContext())
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setIcon(R.drawable.delete_icon)
                .setPositiveButton("Yes", (dialog, which) -> {
                    TasksDBHelper dbHelper = new TasksDBHelper(getContext());
                    dbHelper.deleteTask(taskId);
                    Log.d("Task Deleted", "ID: " + taskId);

                    if (getActivity() instanceof TaskDialogListener) {
                        ((TaskDialogListener) getActivity()).onTaskDataChanged(taskDate);
                    }

                    dialog.dismiss(); //dismiss the alert dialog
                    dismiss(); //dismiss the whole fragment dialog
                })
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss(); // Simply dismiss the dialog
                    Log.d("Task Not Deleted", "ID: " + taskId);
                })
                .show();
    }
}

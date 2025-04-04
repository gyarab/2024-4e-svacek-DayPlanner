package com.example.dayplanner.main.tasks;

import static com.example.dayplanner.notifications.TaskNotificationHelper.cancelNotification;

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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.dayplanner.R;
import com.example.dayplanner.notifications.TaskNotificationHelper;

import java.util.Calendar;

public class TaskDialogFragment extends DialogFragment {

    private boolean isEditMode;
    private Task task;

    public TaskDialogFragment(boolean isEditMode, Task task) {
        this.isEditMode = isEditMode;
        this.task = task;
    }

    public interface TaskDialogListener {
        void onTaskDataChanged(String dateId);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

            View view = getDialog().findViewById(R.id.task_dialog_root);
            if (view != null) {
                Animation slideUp = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
                view.startAnimation(slideUp);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_dialog, container, false);

        EditText editTaskTitle = view.findViewById(R.id.edit_task_title);
        EditText editTaskDescription = view.findViewById(R.id.edit_task_description);
        EditText editTaskLength = view.findViewById(R.id.edit_task_length);
        TextView editTaskDate = view.findViewById(R.id.edit_task_date);
        TextView editTaskTime = view.findViewById(R.id.edit_task_time);
        Button pickDateButton = view.findViewById(R.id.pick_date_button);
        Button pickTimeButton = view.findViewById(R.id.pick_time_button);
        Button saveButton = view.findViewById(R.id.save_task_button);

        /** Fill in field when the user is editing the habit = edit mode is true **/
        if (isEditMode && task != null) {
            editTaskTitle.setText(task.getTaskTitle());
            editTaskDescription.setText(task.getTaskDescription());
            editTaskLength.setText(String.valueOf(task.getTaskLength()));
            editTaskDate.setText(formatTaskDateForUser(task.getTaskDate()));
            editTaskTime.setText(task.getTaskStartTime());

            Button deleteButton = view.findViewById(R.id.delete_task_button);
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog(task.getTaskId(), task.getTaskDate()));
        }

        pickDateButton.setOnClickListener(v -> showDatePicker(editTaskDate));

        pickTimeButton.setOnClickListener(v -> showTimePicker(editTaskTime));

        saveButton.setOnClickListener(v -> {
            String taskTitle = editTaskTitle.getText().toString().trim();
            String taskDescription = editTaskDescription.getText().toString().trim();
            String taskLength = editTaskLength.getText().toString().trim();
            String taskDate = formatTaskDateForDB(editTaskDate.getText().toString());
            String taskStartTime = editTaskTime.getText().toString();

            Log.d("NEW task", taskDate + " " + taskStartTime);
            if (taskTitle.isEmpty() || taskDate.isEmpty() || taskLength.isEmpty() || taskStartTime.equals("Select Time")) {
                Toast.makeText(getContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                Log.e("saveNewHabitToFirebase", "Missing required fields");
                return;
            }


            Task newTask = new Task(
                    isEditMode ? task.getTaskId() : null,
                    taskTitle,
                    taskDescription,
                    taskDate,
                    taskStartTime,
                    Integer.parseInt(taskLength),
                    task.isTaskCompleted()
            );

            TasksDBHelper dbHelper = new TasksDBHelper(getContext());

            if (isEditMode) {
                dbHelper.editTask(newTask);
            } else {
                dbHelper.addTask(newTask);
            }

            String lastTaskId = dbHelper.getLastInsertedTaskId();

            if (lastTaskId != null) {
                Task savedTask = dbHelper.getTaskById(lastTaskId);

                if (savedTask != null) {
                    // Schedule the notification using the saved task's details
                    Calendar taskCalendar = Calendar.getInstance();
                    taskCalendar.set(Calendar.YEAR, Integer.parseInt(savedTask.getTaskDate().substring(4))); // YYYY
                    taskCalendar.set(Calendar.MONTH, Integer.parseInt(savedTask.getTaskDate().substring(2, 4)) - 1); // MM (zero-based)
                    taskCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(savedTask.getTaskDate().substring(0, 2))); // DD
                    taskCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(savedTask.getTaskStartTime().substring(0, 2))); // HH
                    taskCalendar.set(Calendar.MINUTE, Integer.parseInt(savedTask.getTaskStartTime().substring(3))); // mm
                    taskCalendar.set(Calendar.SECOND, 0);

                    long taskTimeMillis = taskCalendar.getTimeInMillis();
                    TaskNotificationHelper.scheduleNotification(getContext(), savedTask.getTaskId(), savedTask.getTaskTitle(), taskTimeMillis);



                    if (getActivity() instanceof TaskDialogListener) {
                        ((TaskDialogListener) getActivity()).onTaskDataChanged(taskDate);
                    }
                }
            }

            dismiss();
        });

        return view;
    }

    private void showDatePicker(TextView editTaskDate) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            String selectedDate = dayOfMonth + "." + (month + 1) + "." + year;
            editTaskDate.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker(TextView editTaskTime) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            String selectedTime = String.format("%02d:%02d", hourOfDay, minute);
            editTaskTime.setText(selectedTime);
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        timePickerDialog.show();
    }

    public void showDeleteConfirmationDialog(String taskId, String taskDate) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    TasksDBHelper dbHelper = new TasksDBHelper(getContext());
                    dbHelper.deleteTask(taskId);

                    if (getActivity() instanceof TaskDialogListener) {
                        ((TaskDialogListener) getActivity()).onTaskDataChanged(taskDate);
                    }

                    cancelNotification(getContext(), taskId);

                    dismiss();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public static String formatTaskDateForUser(String taskDate) {
        String formattedDate = "";
        Log.d("formattedDateB", taskDate);

        if (taskDate != null && taskDate.length() == 8) { //FORMAT
            String day = taskDate.substring(0, 2);
            String month = taskDate.substring(2, 4);
            String year = taskDate.substring(4);

            /** Remove leading zeros from day and month for user-friendly format **/
            day = day.startsWith("0") ? day.substring(1) : day;
            month = month.startsWith("0") ? month.substring(1) : month;

            formattedDate = day + "." + month + "." + year;
        } else {
            Log.d("formattedDateError", "Invalid date format: " + taskDate);
        }

        Log.d("formattedDate", formattedDate);
        return formattedDate;
    }

    public static String formatTaskDateForDB(String taskDate) {
        String formattedDate = "";
        Log.d("formattedDateB", taskDate);
        if (taskDate != null && !taskDate.isEmpty()) {
            // Split the input date by dots
            String[] parts = taskDate.split("\\.");
            if (parts.length == 3) {
                String day = parts[0].length() == 1 ? "0" + parts[0] : parts[0];
                String month = parts[1].length() == 1 ? "0" + parts[1] : parts[1];
                String year = parts[2];
                formattedDate = day + month + year;
            }
        }
        Log.d("formattedDate", formattedDate);
        return formattedDate;
    }
}
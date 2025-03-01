package com.example.dayplanner.main.timeline;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dayplanner.R;
import com.example.dayplanner.main.habits.Habit;
import com.example.dayplanner.main.habits.HabitDialogFragment;
import com.example.dayplanner.main.habits.HabitEntry;
import com.example.dayplanner.main.tasks.Task;
import com.example.dayplanner.main.tasks.TaskDialogFragment;
import com.example.dayplanner.main.tasks.TasksDBHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ViewHolder> {
    private List<TimelineItem> items;
    private Context context;
    private String currentDate;
    private TasksDBHelper dbHelper;

    public TimelineAdapter(Context context, List<TimelineItem> items) {
        this.context = context;
        this.items = items;
    }

    public void setCurrentDate(String date) {
        this.currentDate = date;
        Log.d("TimelineAdapter", "Date set to: " + currentDate);  // Debugging log
        notifyDataSetChanged();  // Refresh the adapter
    }


    public void showTaskDetail(String taskId) {
        TasksDBHelper dbHelper = new TasksDBHelper(context);
        Task task = dbHelper.getTaskById(taskId);
        if (task != null) {
            TaskDialogFragment dialogFragment = new TaskDialogFragment(true, task);
            dialogFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "EditTaskDialog");
        } else {
            Log.e("TaskDetail", "Task not found with ID: " + taskId);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_timeline, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("TimelineAdapter", "Binding item for date: " + currentDate);
        TimelineItem item = items.get(position);
        Log.d("TimelineAdapter", "onBindViewHolder: Position " + position + ", Item: " + item.toString());

        if (item.isTask()) {
            Log.d("TimelineAdapter", "Binding task item: " + item.getTaskTitle());
            holder.taskTitleTextView.setText(item.getTaskTitle());
            holder.taskStartTimeTextView.setText(getTimeRangeForTask(item));
            holder.taskDescriptionTextView.setVisibility(View.VISIBLE);
            holder.progressTextView.setVisibility(View.GONE);
            holder.iconView.setOnClickListener(v -> showTaskDetail(item.getTaskId()));

            holder.statusIcon.setVisibility(View.VISIBLE);
            if (item.getTask().isTaskCompleted()) {
                holder.statusIcon.setImageResource(R.drawable.ic_chceck);
            } else {
                holder.statusIcon.setImageResource(R.drawable.ic_circle);
            }

            holder.seekBar.setVisibility(View.GONE);  // ✅ Hide SeekBar for tasks

            holder.statusIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Task task = item.getTask();
                    if(!task.isTaskCompleted()) {
                        //task is not completed
                        Log.d("Task clicked", task.toString());
                        task.setTaskCompleted(true);
                        Log.d("Task clicked", task.toString());
                        //save item to sqlite
                        TasksDBHelper dbHelper = new TasksDBHelper(context);
                        dbHelper.editTask(task);
                        //change the icon
                        holder.statusIcon.setImageResource(R.drawable.ic_chceck);
                    } else {
                        Log.d("Task clicked", "already completed");
                    }
                }
            });
        } else { // Habit
            Log.d("TimelineAdapter", "Binding habit item: " + item.toString());

            holder.taskStartTimeTextView.setText(getTimeRangeForHabit(item));
            holder.taskDescriptionTextView.setVisibility(View.GONE);
            holder.seekBar.setVisibility(View.VISIBLE);
            holder.taskTitleTextView.setText(item.getHabitName());
            holder.statusIcon.setVisibility(View.GONE);
            holder.progressTextView.setVisibility(View.VISIBLE);

            holder.iconView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Habit edit", item.toString());
                    HabitDialogFragment dialogFragment = new HabitDialogFragment(true, item.getHabit());
                    dialogFragment.show(((AppCompatActivity) context).getSupportFragmentManager(), "EditHabitDialog");
                }
            });

            // ✅ Fetch progress from Firebase
            fetchHabitProgress(item.getHabit(), currentDate, holder.seekBar);

            holder.seekBar.setMax(item.getHabit().getGoalValue());  // Set the SeekBar max value to the habit goal value
            Log.d("TimelineAdapter", "Setting SeekBar max to: " + item.getHabit().getGoalValue());

            // Set the progress increment step (optional, based on your tiling logic)
            int progressIncrement = item.getHabit().getGoalValue() / 10; // For example, break the range into 10 parts
            holder.seekBar.setKeyProgressIncrement(progressIncrement);

            holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        // Get the habit object and current date
                        Habit habit = item.getHabit();
                        String date = currentDate;

                        // Set the progress in the HabitEntry
                        HabitEntry habitEntry = new HabitEntry(date, progress >= habit.getGoalValue(), progress, habit.getGoalValue());

                        // Log the progress and completed status
                        Log.d("SeekBar", "Progress: " + progress + ", Goal: " + habit.getGoalValue() + ", Completed: " + (progress >= habit.getGoalValue()));

                        // Update the HabitEntry in Firebase (or create if it doesn't exist)
                        updateHabitEntryInFirebase(habit, date, progress, habit.getGoalValue());
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // When tracking stops, log the finished state
                    Log.d("SeekBar", "Finished editing progress for habit: " + item.getHabitName());

                    // Log the completed entry and habit in Firebase
                    Habit habit = item.getHabit();
                    String date = currentDate;
                    HabitEntry habitEntry = new HabitEntry(date, habit.getGoalValue() <= seekBar.getProgress(), seekBar.getProgress(), habit.getGoalValue());
                    Log.d("SeekBar", "Entry completed: " + habitEntry.toString());
                    Log.d("SeekBar", "Habit completed: " + habit.toString());

                    // Log final state for debug
                    Log.d("SeekBar", "Updated Habit Entry: " + habitEntry);
                    Log.d("SeekBar", "Updated Habit: " + habit);
                }
            });

        }
    }

    private void fetchHabitProgress(Habit habit, String date, SeekBar seekBar) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference entryRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("habits")
                .child(habit.getId()).child("entries").child(date);

        entryRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                Integer progress = task.getResult().child("progress").getValue(Integer.class);
                Integer goal = task.getResult().child("goalValue").getValue(Integer.class);
                if (progress != null) {
                    seekBar.setProgress(progress);  // ✅ Set retrieved progress
                } else {
                    seekBar.setProgress(0);  // ✅ Default to 0 if no progress found
                }

                if (goal != null) {
                    seekBar.setMax(goal); // ✅ Ensure seekBar uses the correct max value
                }

            } else {
                seekBar.setProgress(0);  // ✅ Default to 0 if no entry exists
            }
        }).addOnFailureListener(e -> {
            Log.e("TimelineAdapter", "Failed to fetch progress from Firebase", e);
            seekBar.setProgress(0);  // ✅ Ensure UI does not break
        });
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    private String getTimeRangeForTask(TimelineItem item) {
        int startTimeInMinutes = item.getStartTimeInMinutes();
        int endTimeInMinutes = startTimeInMinutes + item.getDurationInMinutes();
        return formatTimeInMinutes(startTimeInMinutes) + " - " + formatTimeInMinutes(endTimeInMinutes);
    }

    private String getTimeRangeForHabit(TimelineItem item) {
        return item.getHabitFrequency();
    }

    private String formatTimeInMinutes(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", hours, mins);
    }

    private void updateHabitEntryInFirebase(Habit habit, String date, int progress, int goalValue) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference entryRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("habits")
                .child(habit.getId()).child("entries").child(date);

        // Create a map with the progress, completed, and goal values to update the Firebase entry
        Map<String, Object> updates = new HashMap<>();
        updates.put("progress", progress);  // Set the progress value
        updates.put("completed", progress >= goalValue);  // Automatically update 'completed' status
        updates.put("entryGoalValue", goalValue);  // Ensure goalValue is included
        updates.put("date", date);  // Ensure date is included

        // Update the entry in Firebase
        entryRef.updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("TimelineAdapter", "Progress updated for " + date + " with goal: " + goalValue);
                    } else {
                        Log.e("TimelineAdapter", "Failed to update progress for " + date);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("TimelineAdapter", "Failed to update progress in Firebase", e);
                });
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitleTextView, taskStartTimeTextView, taskDescriptionTextView, progressTextView;
        ImageView iconView, statusIcon;
        View timelineTop, timelineBottom;
        SeekBar seekBar;

        public ViewHolder(View itemView) {
            super(itemView);
            taskStartTimeTextView = itemView.findViewById(R.id.task_start_time_txt);
            taskDescriptionTextView = itemView.findViewById(R.id.task_description_txt);
            iconView = itemView.findViewById(R.id.iconView);
            statusIcon = itemView.findViewById(R.id.statusIcon);
            timelineTop = itemView.findViewById(R.id.timelineTop);
            timelineBottom = itemView.findViewById(R.id.timelineBottom);
            seekBar = itemView.findViewById(R.id.progress_bar);
            taskTitleTextView = itemView.findViewById(R.id.task_title_txt);
            progressTextView = itemView.findViewById(R.id.progress_txt);
        }
    }
}

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
            Habit habit = item.getHabit();
            HabitEntry currentEntry = habit.getEntryForDate(currentDate);

            int progress = currentEntry != null ? currentEntry.getProgress() : 0;
            int entryGoalValue = currentEntry != null ? currentEntry.getEntryGoalValue() : 0;
            String metric = habit.getMetric();

            Log.d("XML preparation", String.valueOf(progress) + " / " + String.valueOf(entryGoalValue) + " " + metric);

            holder.progressTextView.setText(progress + " / " + entryGoalValue + " " + metric);

            Log.d("Current Date", String.valueOf(habit.getEntryForDate(currentDate)));

            Log.d("TimelineAdapter", "Binding habit item: " + item.getHabit().toString());

            holder.taskStartTimeTextView.setText(getTimeRangeForHabit(item));
            holder.taskDescriptionTextView.setVisibility(View.GONE);
            holder.seekBar.setVisibility(View.VISIBLE);
            holder.taskTitleTextView.setText(item.getHabitName());
            holder.statusIcon.setVisibility(View.GONE);
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

            // Set the progress increment step (optional, based on your tiling logic)
            int progressIncrement = item.getHabit().getGoalValue() / 10; // For example, break the range into 10 parts
            holder.seekBar.setKeyProgressIncrement(progressIncrement);

            holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        Log.d("TimelineAdapter", "Updating habit entry: " + item.getHabitName() + ", Progress: " + progress);

                        holder.progressTextView.setText(progress + " / " + entryGoalValue + " " + metric);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // ✅ Update Firebase with the new progress value
                    // Fetch and update the habit progress after the user has stopped sliding the SeekBar
                    int progress = seekBar.getProgress(); // Get the final progress when the user releases the SeekBar
                    int goal = item.getHabit().getGoalValue(); // Use the habit's goal


                    /** Update the progress in onChanged aswell as on stop tracking touch**/
                    Log.d("TimelineAdapter - onStopTrackingTouch", "Updating habit entry: " + item.getHabitName() + ", Progress: " + progress);
                    holder.progressTextView.setText(progress + " / " + entryGoalValue + " " + metric);

                    // Update Firebase with the new progress and goal when the user stops adjusting the SeekBar
                    updateHabitEntryInFirebase(item.getHabit(), currentDate, progress, goal);
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
                if (progress != null) {
                    seekBar.setProgress(progress);  // ✅ Set retrieved progress
                } else {
                    seekBar.setProgress(0);  // ✅ Default to 0 if no progress found
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

    private void updateHabitEntryInFirebase(Habit habit, String date, int progress, int goal) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference entryRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("habits")
                .child(habit.getId()).child("entries").child(date);

        // Create a map with the progress, completed, and goal values to update the Firebase entry
        Map<String, Object> updates = new HashMap<>();
        updates.put("progress", progress);  // Set the progress value
        updates.put("completed", progress >= goal);  // Automatically update 'completed' status based on progress and goal
        updates.put("goal", goal);  // Set the custom goal for the entry

        // Update the entry in Firebase
        entryRef.updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("TimelineAdapter", "Progress updated for " + date + " with goal: " + goal);
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

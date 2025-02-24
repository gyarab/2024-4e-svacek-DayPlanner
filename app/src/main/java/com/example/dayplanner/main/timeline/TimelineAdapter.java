package com.example.dayplanner.main.timeline;

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
import com.example.dayplanner.main.habits.HabitEntry;
import com.example.dayplanner.main.tasks.Task;
import com.example.dayplanner.main.tasks.TaskDialogFragment;
import com.example.dayplanner.main.tasks.TasksDBHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ViewHolder> {
    private List<TimelineItem> items;
    private Context context;
    // Store the currently selected date (e.g., passed from WeeklyHeaderFragment)
    private String currentDate;

    public TimelineAdapter(Context context, List<TimelineItem> items) {
        this.context = context;
        this.items = items;
    }

    // Setter so the hosting fragment/activity can update the current date.
    public void setCurrentDate(String date) {
        this.currentDate = date;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_timeline, parent, false);
        return new ViewHolder(view);
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

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimelineItem item = items.get(position);

        if (item.isTask()) {
            holder.taskTitleTextView.setText(item.getTaskTitle());
            holder.taskStartTimeTextView.setText(getTimeRangeForTask(item));
            holder.taskDescriptionTextView.setText(item.getTaskDescription());
            holder.taskDescriptionTextView.setVisibility(View.VISIBLE);
            holder.seekBar.setVisibility(View.GONE);
            Log.d("isTask", "HABIT: " + item.isTask());

            holder.iconView.setOnClickListener(v -> showTaskDetail(item.getTaskId()));
            holder.statusIcon.setImageResource(R.drawable.ic_circle);
            holder.progressTextView.setVisibility(View.GONE);
            holder.statusIcon.setOnClickListener(v -> {
                if (!item.isTaskCompleted()) {
                    holder.statusIcon.setImageResource(R.drawable.ic_chceck);
                    item.setTaskCompleted(true);
                    Log.d("Task is completed", item.toString());
                } else {
                    Log.d("Task is completed", "already completed: " + item.toString());
                }
            });
        } else { // Habit
            holder.taskStartTimeTextView.setText(getTimeRangeForHabit(item));
            holder.taskDescriptionTextView.setVisibility(View.GONE);
            holder.taskDescriptionTextView.setText("");
            holder.seekBar.setVisibility(View.VISIBLE);
            holder.taskTitleTextView.setText(item.getHabitName());
            Log.d("isTask", "isTask: " + item.isTask());

            holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        // Use the selected date from WeeklyHeaderFragment.
                        String habitDate = (currentDate != null) ? currentDate : item.getHabitDate();
                        // Update the local habit object
                        item.getHabit().setProgressForDate(habitDate, progress);
                        // Update only the specific habit entry in Firebase
                        updateHabitEntryInFirebase(item.getHabit(), habitDate, progress);
                    }
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { }
            });
        }

        // Dynamic height based on duration
        int minHeight = 100;
        int height = Math.max(minHeight, item.getDurationInMinutes() * 10);
        holder.iconView.setLayoutParams(new LinearLayout.LayoutParams(100, height));

        // Timeline connector lines
        holder.timelineTop.setVisibility(position == 0 ? View.INVISIBLE : View.VISIBLE);
        holder.timelineBottom.setVisibility(position == items.size() - 1 ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // Helper method for tasks
    private String getTimeRangeForTask(TimelineItem item) {
        int startTimeInMinutes = item.getStartTimeInMinutes();
        int endTimeInMinutes = startTimeInMinutes + item.getDurationInMinutes();
        return formatTimeInMinutes(startTimeInMinutes) + " - " + formatTimeInMinutes(endTimeInMinutes);
    }

    // Helper method for habits
    private String getTimeRangeForHabit(TimelineItem item) {
        return item.getHabitFrequency();
    }

    // Format minutes to HH:mm string
    private String formatTimeInMinutes(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        return String.format("%02d:%02d", hours, mins);
    }

    /**
     * Update only the specific habit entry in Firebase for a given date.
     * Assumes that the habit's entries are stored in Firebase as a map keyed by date.
     */
    private void updateHabitEntryInFirebase(Habit habit, String date, int progress) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Build a reference to the specific habit entry based on date.
        DatabaseReference entryRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("habits")
                .child(habit.getId())
                .child("entries")
                .child(date);

        // Prepare the update. You can add additional fields as needed.
        Map<String, Object> update = new HashMap<>();
        update.put("date", date);
        update.put("progress", progress);
        update.put("goal", habit.getGoalValue());
        update.put("completed", false); // Adjust as needed.

        entryRef.updateChildren(update)
                .addOnSuccessListener(aVoid ->
                        Log.d("TimelineAdapter", "Habit entry for date " + date + " updated in Firebase")
                )
                .addOnFailureListener(e ->
                        Log.e("TimelineAdapter", "Failed to update habit entry progress in Firebase", e)
                );
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitleTextView;
        TextView taskStartTimeTextView;
        TextView taskDescriptionTextView;
        ImageView iconView;
        ImageView statusIcon;
        View timelineTop;
        View timelineBottom;
        SeekBar seekBar;
        TextView progressTextView;

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

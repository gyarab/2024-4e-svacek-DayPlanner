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

import java.util.List;

import com.example.dayplanner.main.tasks.Task;
import com.example.dayplanner.main.tasks.TaskDialogFragment;
import com.example.dayplanner.main.tasks.TasksDBHelper;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.ViewHolder> {
    private List<TimelineItem> items;
    private Context context;

    public TimelineAdapter(Context context, List<TimelineItem> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_timeline, parent, false);
        return new ViewHolder(view);
    }

    public void showTaskDetail(String taskId) {
        TasksDBHelper dbHelper = new TasksDBHelper(context);
        Task task = dbHelper.getTaskById(taskId); // Fetch the task from DB

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

        // Set the time text based on task or habit
        if (item.isTask()) {
            holder.taskTitleTextView.setText(item.getTaskTitle());
            holder.taskStartTimeTextView.setText(getTimeRangeForTask(item)); // Set time range for tasks
            holder.taskDescriptionTextView.setText(item.getTaskDescription());  // Set task title as description
            holder.taskDescriptionTextView.setVisibility(View.VISIBLE);  // Show for tasks
            holder.seekBar.setVisibility(View.GONE);
            Log.d("isTask", "HABIT: " + item.isTask());

            holder.iconView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTaskDetail(item.getTaskId());
                }
            });

            holder.statusIcon.setImageResource(R.drawable.ic_circle);

            holder.statusIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!item.isTaskCompleted()) {
                        holder.statusIcon.setImageResource(R.drawable.ic_chceck);
                        item.setTaskCompleted(true);
                        Log.d("Task is completed", item.toString());
                    } else {
                        Log.d("Task is completed", "already completed: " + item.toString());
                    }
                }
            });

        } else {
            holder.taskStartTimeTextView.setText(getTimeRangeForHabit(item));  // Set time range for habits
            holder.taskDescriptionTextView.setVisibility(View.GONE);  // Hide description for habits
            holder.taskDescriptionTextView.setText("");  // Clear text when hidden
            holder.seekBar.setVisibility(View.VISIBLE);
            Log.d("isTask", "isTask: " + item.isTask());
        }

        // Dynamic Height for Duration (based on task or habit duration)
        int minHeight = 100;
        int height = Math.max(minHeight, item.getDurationInMinutes() * 10);
        holder.iconView.setLayoutParams(new LinearLayout.LayoutParams(100, height));  // Adjust icon height based on duration

        /*// Handle completion status for tasks
        if (item.isTask() && item.getTaskId() != null) {
            holder.statusIcon.setImageResource(R.drawable.ic_chceck);  // Set completed icon for tasks
        } else {
            holder.statusIcon.setImageResource(R.drawable.ic_circle);  // Set incomplete status
        }*/

        // Set timeline line visibility (showing top and bottom connectors for each task or habit)
        if (position == 0) {
            holder.timelineTop.setVisibility(View.INVISIBLE);  // No top line for the first item
        } else {
            holder.timelineTop.setVisibility(View.VISIBLE);  // Show top line for other items
        }

        if (position == items.size() - 1) {
            holder.timelineBottom.setVisibility(View.INVISIBLE);  // No bottom line for the last item
        } else {
            holder.timelineBottom.setVisibility(View.VISIBLE);  // Show bottom line for other items
        }
    }



    @Override
    public int getItemCount() {
        return items.size();
    }

    // Helper method to return time range for tasks
    private String getTimeRangeForTask(TimelineItem item) {
        int startTimeInMinutes = item.getStartTimeInMinutes();
        int endTimeInMinutes = startTimeInMinutes + item.getDurationInMinutes();
        return formatTimeInMinutes(startTimeInMinutes) + " - " + formatTimeInMinutes(endTimeInMinutes);
    }

    // Helper method to return time range for habits (assuming habits have a fixed duration or time)
    private String getTimeRangeForHabit(TimelineItem item) {
        // Assuming habits have a fixed start time and no duration for simplicity
        return item.getHabitFrequency();  // For example, "Daily", "Weekly", etc.
    }

    // Helper method to format minutes into time string
    private String formatTimeInMinutes(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        return String.format("%02d:%02d", hours, mins);
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

        }
    }

    /* Undo Dialog Fragment */

}

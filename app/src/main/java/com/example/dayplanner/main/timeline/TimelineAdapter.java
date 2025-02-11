package com.example.dayplanner.main.timeline;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dayplanner.R;
import com.example.dayplanner.main.habits.Habit;
import com.example.dayplanner.main.tasks.Task;
import com.example.dayplanner.main.tasks.TaskDialogFragment;
import com.example.dayplanner.main.tasks.TasksDBHelper;

import java.util.List;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.MyViewHolder> {

    private Context context;
    private List<TimelineItem> timelineItems;

    public TimelineAdapter(Context context, List<TimelineItem> timelineItems) {
        this.context = context;
        this.timelineItems = timelineItems;
    }

    @NonNull
    @Override
    public TimelineAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_timeline, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimelineAdapter.MyViewHolder holder, int position) {
        TimelineItem item = timelineItems.get(position);

        if (item.isTask()) {
            holder.task_title_txt.setText(item.getTaskTitle());
            holder.task_start_time_txt.setText("Start: " + item.getTaskStartTime());
            holder.task_description_txt.setVisibility(View.VISIBLE);
            holder.task_description_txt.setText("Task");

            holder.task_title_txt.setOnClickListener(v -> showTaskDetail(item.getTaskId())); // Pass only task ID
        } else {
            holder.task_title_txt.setText(item.getHabitName() + " (Habit)");
            holder.task_start_time_txt.setText("Repeat: " + item.getHabitFrequency());
            holder.task_description_txt.setVisibility(View.GONE);
        }
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
    public int getItemCount() {
        return timelineItems.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView task_start_time_txt, task_title_txt, task_description_txt;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            task_start_time_txt = itemView.findViewById(R.id.task_start_time_txt);
            task_title_txt = itemView.findViewById(R.id.task_title_txt);
            task_description_txt = itemView.findViewById(R.id.task_description_txt);
        }
    }
}

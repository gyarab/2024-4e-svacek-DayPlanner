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
import com.example.dayplanner.main.habits.HabitDialogFragment;
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
            holder.statusIcon.setImageResource(R.drawable.ic_circle);
            holder.seekBar.setVisibility(View.GONE);  // ✅ Hide SeekBar for tasks
        } else { // Habit
            Log.d("TimelineAdapter", "Binding habit item: " + item.getHabitName());

            holder.taskStartTimeTextView.setText(getTimeRangeForHabit(item));
            holder.taskDescriptionTextView.setVisibility(View.GONE);
            holder.seekBar.setVisibility(View.VISIBLE);
            holder.taskTitleTextView.setText(item.getHabitName());
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

            holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        Log.d("TimelineAdapter", "Updating habit entry: " + item.getHabitName() + ", Progress: " + progress);

                        // ✅ Update Firebase
                        updateHabitEntryInFirebase(item.getHabit(), currentDate, progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { }
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

    private void updateHabitEntryInFirebase(Habit habit, String date, int progress) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference entryRef = FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("habits")
                .child(habit.getId()).child("entries").child(date);

        Map<String, Object> update = new HashMap<>();
        update.put("date", date);
        update.put("progress", progress);
        update.put("goal", habit.getGoalValue());

        entryRef.updateChildren(update).addOnSuccessListener(aVoid ->
                Log.d("TimelineAdapter", "Habit updated: " + date)
        ).addOnFailureListener(e ->
                Log.e("TimelineAdapter", "Firebase update failed", e)
        );
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

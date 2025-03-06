package com.example.dayplanner.statistics;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dayplanner.R;
import com.example.dayplanner.main.habits.Habit;

import java.util.List;

public class HabitListAdapter extends RecyclerView.Adapter<HabitListAdapter.ViewHolder> {

    private List<Habit> habitList;
    private OnHabitClickListener listener;

    public interface OnHabitClickListener {
        void onHabitClick(String habitId);
    }

    public HabitListAdapter(List<Habit> habitList, OnHabitClickListener listener) {
        this.habitList = habitList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habit, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Habit habit = habitList.get(position);
        holder.habitNameTextView.setText(habit.getName());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onHabitClick(habit.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView habitNameTextView;

        ViewHolder(View itemView) {
            super(itemView);
            habitNameTextView = itemView.findViewById(R.id.tvHabitName);
        }
    }
}

package com.example.dayplanner.statistics;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
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
    private String selectedHabitId = null; // Track selected habit

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

        // Check if this item is selected
        boolean isSelected = habit.getId().equals(selectedHabitId);
        updateItemView(holder.itemView, isSelected);

        holder.itemView.setOnClickListener(v -> {
            selectedHabitId = habit.getId();
            notifyDataSetChanged(); // Refresh UI to reflect selection

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
    private void setTextColorBasedOnTheme(View view, Context context) {
        // Get the current theme's text color (light or dark)
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{android.R.attr.colorPrimary});
        int textColor = a.getColor(0, Color.BLACK); // Default to black if not found
        a.recycle();  // Don't forget to recycle the TypedArray to avoid memory leaks

        // Set the text color dynamically based on the theme
        view.setBackgroundColor(textColor);
    }

    // Update item UI
    private void updateItemView(View view, boolean isSelected) {
        Context context = view.getContext();

        // Set text color based on theme
        setTextColorBasedOnTheme(view, context);

        // Animate selection change
        float scale = isSelected ? 1.1f : 1.0f;
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", scale);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", scale);
        scaleX.setDuration(200);
        scaleY.setDuration(200);
        scaleX.start();
        scaleY.start();
    }
}

package com.example.dayplanner.statistics;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dayplanner.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonthlyProgressAdapter extends RecyclerView.Adapter<MonthlyProgressAdapter.ViewHolder> {
    private final Context context;
    private final Map<Integer, DailyProgress> progressMap = new HashMap<>();
    private final int totalDaysInMonth;
    private final int currentDay;

    public MonthlyProgressAdapter(Context context, List<DailyProgress> dailyProgressList) {
        this.context = context;

        // Store progress in a map for quick lookup
        for (DailyProgress progress : dailyProgressList) {
            progressMap.put(progress.getDay(), progress);
        }

        // Get month details
        Calendar calendar = Calendar.getInstance();
        totalDaysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        currentDay = calendar.get(Calendar.DAY_OF_MONTH);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_daily_progress, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int day = position + 1; // Convert position to 1-based index
        DailyProgress progress = progressMap.get(day);

        if (progress != null) {
            // Existing data
            holder.bind(progress, context, false);
        } else {
            // No data = upcoming day (gray)
            holder.bind(new DailyProgress(day, 0), context, day > currentDay);
        }
    }

    @Override
    public int getItemCount() {
        return totalDaysInMonth;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final CustomCircularProgressBar circularProgressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            circularProgressBar = itemView.findViewById(R.id.circularProgressBar);  // Updated to use CustomCircularProgressBar
        }

        public void bind(DailyProgress progress, Context context, boolean isUpcoming) {
            // Set the progress based on the completion percentage
            circularProgressBar.setProgress(progress.getCompletionPercentage(), String.valueOf(progress.getDay()));
            circularProgressBar.setTextStyle(Typeface.BOLD);
            // Set a thinner width for the progress bar
            circularProgressBar.setProgressWidth(10); // Change this value to make it thinner
            circularProgressBar.setBackgroundWidth(10); // Similarly adjust background width if necessary

            if (isUpcoming) {
                // Gray for upcoming days
                circularProgressBar.setProgressColor(ContextCompat.getColor(context, R.color.progress_upcoming));
            } else {
                // Use updated color mapping based on completion percentage
                int color = getColorForPercentage(progress.getCompletionPercentage(), context);
                circularProgressBar.setProgressColor(color);
            }
        }


        private int getColorForPercentage(float percentage, Context context) {
            if (percentage >= 75) return ContextCompat.getColor(context, R.color.progress_high);
            if (percentage >= 50) return ContextCompat.getColor(context, R.color.progress_medium);
            if (percentage >= 25) return ContextCompat.getColor(context, R.color.progress_low);
            return ContextCompat.getColor(context, R.color.progress_none); // For 0% completion
        }
    }
}

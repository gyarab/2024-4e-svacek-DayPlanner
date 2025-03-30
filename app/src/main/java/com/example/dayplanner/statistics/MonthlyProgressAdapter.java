package com.example.dayplanner.statistics;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

        for (DailyProgress progress : dailyProgressList) {
            progressMap.put(progress.getDay(), progress);
        }

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
            holder.bind(progress, context, false);
        } else {
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
            circularProgressBar = itemView.findViewById(R.id.circularProgressBar);
        }

        public void bind(DailyProgress progress, Context context, boolean isUpcoming) {
            circularProgressBar.setProgress(progress.getCompletionPercentage(), String.valueOf(progress.getDay()));
            circularProgressBar.setTextStyle(Typeface.BOLD);
            circularProgressBar.setProgressWidth(10);
            circularProgressBar.setBackgroundWidth(10);

            if (isUpcoming) {
                //future days will be gray
                circularProgressBar.setProgressColor(ContextCompat.getColor(context, R.color.progress_upcoming));
            } else {
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

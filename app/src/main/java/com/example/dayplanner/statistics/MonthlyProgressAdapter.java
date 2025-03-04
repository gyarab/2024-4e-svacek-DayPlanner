package com.example.dayplanner.statistics;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dayplanner.R;

import java.util.List;

public class MonthlyProgressAdapter extends RecyclerView.Adapter<MonthlyProgressAdapter.ViewHolder> {
    private List<DailyProgress> dailyProgressList;
    private Context context;

    public MonthlyProgressAdapter(Context context, List<DailyProgress> dailyProgressList) {
        this.context = context;
        this.dailyProgressList = dailyProgressList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_daily_progress, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(dailyProgressList.get(position), context);
    }

    @Override
    public int getItemCount() {
        return dailyProgressList.size();
    }

    public void updateData(List<DailyProgress> newProgressList) {
        this.dailyProgressList = newProgressList;
        notifyDataSetChanged(); // Ensure UI refreshes properly
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDay;
        private View circleView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            circleView = itemView.findViewById(R.id.circleView);
        }

        public void bind(DailyProgress progress, Context context) {
            tvDay.setText(String.valueOf(progress.getDay()));

            int color = getColorForPercentage(progress.getCompletionPercentage(), context);

            // Ensure the background is not null before setting color filter
            if (circleView.getBackground() != null) {
                circleView.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
            }
        }

        private int getColorForPercentage(float percentage, Context context) {
            if (percentage >= 75) return ContextCompat.getColor(context, R.color.progress_high);
            if (percentage >= 50) return ContextCompat.getColor(context, R.color.progress_medium);
            if (percentage >= 25) return ContextCompat.getColor(context, R.color.progress_low);
            return ContextCompat.getColor(context, R.color.progress_low);
        }
    }
}

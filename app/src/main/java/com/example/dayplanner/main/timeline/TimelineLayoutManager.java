package com.example.dayplanner.main.timeline;

import android.content.Context;
import android.view.View;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TimelineLayoutManager extends RecyclerView.LayoutManager {
    private static final int HOUR_HEIGHT = 200; // Base height for one hour, adjustable
    private static final int OVERLAP_OFFSET = 20; // Offset for overlapping tasks

    public TimelineLayoutManager(Context context) {}

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
        );
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        detachAndScrapAttachedViews(recycler);

        List<View> overlappingViews = new ArrayList<>();

        for (int i = 0; i < getItemCount(); i++) {
            View view = recycler.getViewForPosition(i);
            addView(view);
            measureChildWithMargins(view, 0, 0);

            // Retrieve item start time and duration
            TimelineItem item = (TimelineItem) view.getTag();
            int startMinute = item.getStartTimeInMinutes(); // Use directly
            int duration = item.getDurationInMinutes();
            int height = (duration * HOUR_HEIGHT) / 60;

            int top = (startMinute * HOUR_HEIGHT) / 60;
            int left = 0;

            // Handle overlapping tasks
            for (View other : overlappingViews) {
                if (Math.abs(other.getTop() - top) < height / 2) {
                    left += OVERLAP_OFFSET;
                }
            }

            layoutDecorated(view, left, top, left + getWidth(), top + height);
            overlappingViews.add(view);
        }
    }


    private int convertTimeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }
}

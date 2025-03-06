package com.example.dayplanner.main.timeline;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dayplanner.R;

public class TimelineLayoutManager extends LinearLayoutManager {

    private final Context context;

    public TimelineLayoutManager(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);

        if (getItemCount() == 0) return;

        // Get display density for converting dp to pixels
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float density = displayMetrics.density;

        // Define min and max height in DP
        int minHeightDp = 50;  // Minimum height (e.g., 24dp)
        int maxHeightDp = 250; // Maximum height (e.g., 100dp)

        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view == null) continue;

            ImageView iconView = view.findViewById(R.id.iconView);
            if (iconView == null) continue;

            RecyclerView recyclerView = (RecyclerView) view.getParent();
            RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();

            if (adapter instanceof TimelineAdapter) {
                TimelineItem item = ((TimelineAdapter) adapter).getItemAt(i);

                if (item != null && item.isTask()) { // Only apply to tasks
                    // Scale height based on duration, but keep within min/max limits
                    int iconHeightDp = Math.max(minHeightDp, Math.min(24 + (item.getDurationInMinutes() / 2), maxHeightDp));
                    int iconHeightPx = (int) (iconHeightDp * density); // Convert dp to pixels

                    ViewGroup.LayoutParams params = iconView.getLayoutParams();
                    params.height = iconHeightPx;
                    iconView.setLayoutParams(params);
                }
            }
        }
    }



    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
        );
    }

    @Override
    public void scrollToPosition(int position) {
        super.scrollToPosition(position);
    }
}

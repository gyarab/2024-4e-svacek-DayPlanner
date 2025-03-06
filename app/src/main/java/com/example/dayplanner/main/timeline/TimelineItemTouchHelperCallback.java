package com.example.dayplanner.main.timeline;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class TimelineItemTouchHelperCallback extends ItemTouchHelper.Callback {
    private final TimelineAdapter timelineAdapter;
    private final Context context;

    public TimelineItemTouchHelperCallback(TimelineAdapter timelineAdapter, Context context) {
        this.timelineAdapter = timelineAdapter;
        this.context = context;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        // Allow dragging of items in the RecyclerView
        return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        // Handle the move logic here
        int fromPosition = viewHolder.getAdapterPosition();
        int toPosition = target.getAdapterPosition();

        // Move the item within the adapter
        //timelineAdapter.onItemMove(fromPosition, toPosition);

        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        // Handle item swipe if needed
    }

    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);

        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            // Update the UI during drag (e.g., change item color)
            viewHolder.itemView.setAlpha(0.5f);
        } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            // Reset the UI after drag ends
            if (viewHolder != null) {
                viewHolder.itemView.setAlpha(1f);
            }
        }
    }
}

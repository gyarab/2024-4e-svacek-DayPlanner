package com.example.dayplanner.main.dayslist;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dayplanner.R;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.DayViewHolder> {
    private Context context;
    private ArrayList<DayModel> days;
    private int selectedPosition = -1; //serve to track the previous active position to later hide the active dot
    private HashMap<String, Integer> dateIdToPositionMap;
    private OnDayClickListener onDayClickListener;
    public interface OnDayClickListener {
        void onDayClick(String dateID);
    }
    public DayAdapter(Context context, ArrayList<DayModel> days, OnDayClickListener onDayClickListener) {
        this.context = context;
        this.days = days;
        this.onDayClickListener = onDayClickListener;
        buildDateIdToPositionMap();
    }

    private void buildDateIdToPositionMap() {
        dateIdToPositionMap = new HashMap<>();
        for (int i = 0; i < days.size(); i++) {
            DayModel dayModel = days.get(i);
            String dateId = dayModel.getDate() + dayModel.getMonth() + dayModel.getYear();
            dateIdToPositionMap.put(dateId, i);
        }
    }

    public void updateDays(ArrayList<DayModel> newDays) {
        this.days = newDays;
        buildDateIdToPositionMap();
        notifyDataSetChanged();
    }

    public void setActiveDotByDateId(String dateID) {
        Integer position = dateIdToPositionMap.get(dateID);
        if (position != null) {
            setActiveDot(position);
        } else {
            Log.d("SetDotByDateId", "No matching dateID found: " + dateID);
        }
    }


    @NonNull
    @Override
    public DayAdapter.DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayAdapter.DayViewHolder holder, int position) {
        DayModel dayModel = days.get(position);
        holder.dayTextView.setText(dayModel.getDayName());
        holder.dateTextView.setText(dayModel.getDate());

        String monthName = new DateFormatSymbols().getMonths()[Integer.parseInt(dayModel.getMonth()) - 1];

        holder.dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /** configure active dot **/
                setActiveDot(holder.getAdapterPosition());

                /** configure MonthYearTextView **/
                Log.d("CLICKED", "id: " + dayModel.getDate() + "" + dayModel.getMonth() + "" + dayModel.getYear());
                TextView textView = ((Activity) context).findViewById(R.id.monthYearTextView); //I need the context for using the method
                textView.setText(monthName  + " " + dayModel.getYear());

                /** Send the date it to main function **/
                String dateId = dayModel.getDate() + "" + dayModel.getMonth() + "" + dayModel.getYear();
                onDayClickListener.onDayClick(dateId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public void setActiveDot(int position) {
        Log.d("SetDot", "Clicked position: " + position);

        RecyclerView weeklyRecyclerView = ((Activity) context).findViewById(R.id.weeklyRecyclerView);

        RecyclerView.Adapter adapter = weeklyRecyclerView.getAdapter();
        if (adapter == null) return;

        // If a position is already selected, reset the active dot for that position
        if (selectedPosition != -1 && selectedPosition != position) {
            RecyclerView.ViewHolder previousViewHolder = weeklyRecyclerView.findViewHolderForAdapterPosition(selectedPosition);
            if (previousViewHolder instanceof DayViewHolder) {
                DayViewHolder previousDayViewHolder = (DayViewHolder) previousViewHolder;
                // Reset the active dot visibility for the previous item
                previousDayViewHolder.activeDot.setVisibility(View.GONE);
            }
        }

        // Set the active dot visibility to VISIBLE for the clicked item
        RecyclerView.ViewHolder currentViewHolder = weeklyRecyclerView.findViewHolderForAdapterPosition(position);
        if (currentViewHolder instanceof DayViewHolder) {
            DayViewHolder currentDayViewHolder = (DayViewHolder) currentViewHolder;
            currentDayViewHolder.activeDot.setVisibility(View.VISIBLE);
        }

        selectedPosition = position; // Updates the selected position to the new position

        Log.d("SetDot", "selectedPosition updated: " + selectedPosition);
    }

    public class DayViewHolder extends RecyclerView.ViewHolder {

        TextView dayTextView;
        TextView dateTextView;
        View activeDot;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            dayTextView = itemView.findViewById(R.id.dayTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);

            activeDot = itemView.findViewById(R.id.activeDot);
        }
    }
}

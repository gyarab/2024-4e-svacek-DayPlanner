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
    private int selectedPosition = -1;
    private HashMap<String, Integer> dateIdToPositionMap;
    private OnDayClickListener onDayClickListener;
    private static final String TAG = "DayAdapter";

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
            String dateId = formatDateId(dayModel);
            dateIdToPositionMap.put(dateId, i);
        }
        Log.d(TAG, "Built date map with " + dateIdToPositionMap.size() + " entries");
    }

    private String formatDateId(DayModel dayModel) {
        return dayModel.getDate() + dayModel.getMonth() + dayModel.getYear();
    }

    public void updateDays(ArrayList<DayModel> newDays) {
        this.days = new ArrayList<>(newDays);
        buildDateIdToPositionMap();

        selectedPosition = -1;

        notifyDataSetChanged();
        Log.d(TAG, "Updated days list with " + newDays.size() + " items");
    }

    public void setActiveDotByDateId(String dateID) {
        Log.d(TAG, "Attempting to set active dot for dateID: " + dateID);
        Integer position = dateIdToPositionMap.get(dateID);

        if (position != null) {
            setActiveDot(position);
            Log.d(TAG, "Found position for dateID " + dateID + ": " + position);
        } else {
            String dayNumber = dateID.substring(0, 2);
            for (int i = 0; i < days.size(); i++) {
                if (days.get(i).getDate().equals(dayNumber)) {
                    setActiveDot(i);
                    Log.d(TAG, "Found position by day number " + dayNumber + ": " + i);
                    return;
                }
            }
            Log.d(TAG, "No matching dateID found: " + dateID);
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

        holder.activeDot.setVisibility(position == selectedPosition ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    setActiveDot(adapterPosition);

                    DayModel selectedDay = days.get(adapterPosition);

                    updateMonthYearTextView(selectedDay, monthName);

                    String dateId = formatDateId(selectedDay);
                    onDayClickListener.onDayClick(dateId);
                    Log.d(TAG, "Day clicked: " + dateId);
                }
            }
        });
    }

    private void updateMonthYearTextView(DayModel dayModel, String monthName) {
        Activity activity = (Activity) context;
        if (activity != null) {
            TextView textView = activity.findViewById(R.id.monthYearTextView);
            if (textView != null) {
                textView.setText(monthName + " " + dayModel.getYear());
            }
        }
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public void setActiveDot(int position) {
        if (position < 0 || position >= days.size()) {
            Log.e(TAG, "Invalid position for active dot: " + position);
            return;
        }

        Log.d(TAG, "Setting active dot at position: " + position);

        int oldPosition = selectedPosition;
        selectedPosition = position;

        if (oldPosition >= 0 && oldPosition < days.size()) {
            notifyItemChanged(oldPosition);
        }
        notifyItemChanged(selectedPosition);

        Activity activity = (Activity) context;
        if (activity != null) {
            RecyclerView recyclerView = activity.findViewById(R.id.weeklyRecyclerView);
            if (recyclerView != null) {
                recyclerView.smoothScrollToPosition(position);
            }
        }
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public DayModel getSelectedDay() {
        if (selectedPosition >= 0 && selectedPosition < days.size()) {
            return days.get(selectedPosition);
        }
        return null;
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
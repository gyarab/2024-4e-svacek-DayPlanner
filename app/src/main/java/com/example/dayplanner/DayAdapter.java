package com.example.dayplanner;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;

public class DayAdapter extends RecyclerView.Adapter<DayAdapter.DayViewHolder> {
    private Context context;
    private ArrayList<DayModel> days;

    DayAdapter(Context context, ArrayList<DayModel> days) {
        this.context = context;
        this.days = days;
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
                Log.d("CLICKED", "id: " + dayModel.getDate() + "" + dayModel.getMonth() + "" + dayModel.getYear());
                TextView textView = ((Activity) context).findViewById(R.id.monthYearTextView); //I need the context for using the method
                textView.setText(monthName  + " " + dayModel.getYear());

                //GET THE ROWS CORRESPONDING TO THE ID AND SEND THE ARRAYS TO MAIN FUNCTION AND DISPLAY THEM
            }
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public class DayViewHolder extends RecyclerView.ViewHolder {

        TextView dayTextView;
        TextView dateTextView;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            dayTextView = itemView.findViewById(R.id.dayTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }
    }
}

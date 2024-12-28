package com.example.dayplanner;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class WeeklyHeaderFragment extends Fragment {

    RecyclerView weeklyRecyclerView;
    DayAdapter dayAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weekly_header, container, false);

        weeklyRecyclerView = view.findViewById(R.id.weeklyRecyclerView);
        DaysList daysList = new DaysList();
        dayAdapter = new DayAdapter(getContext(), daysList, new DayAdapter.OnDayClickListener() {
            @Override
            public void onDayClick(String dateId) {
                // Notify the activity
                if (getActivity() instanceof OnDaySelectedListener) {
                    ((OnDaySelectedListener) getActivity()).onDaySelected(dateId);
                    Log.d("WeeklyHeaderFragment", "Day clicked: " + dateId);
                } else {
                    Log.e("WeeklyHeaderFragment", "Activity must implement OnDaySelectedListener");
                }
            }
        });

        weeklyRecyclerView.setAdapter(dayAdapter);
        weeklyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        return view;
    }

    // Interface for communicating with the activity
    public interface OnDaySelectedListener {
        void onDaySelected(String dateId);
    }
}

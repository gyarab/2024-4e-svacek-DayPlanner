package com.example.dayplanner.main.dayslist;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.example.dayplanner.R;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WeeklyHeaderFragment extends Fragment {

    private RecyclerView weeklyRecyclerView;
    public DayAdapter dayAdapter;
    private DaysList daysList;
    private int currentWeekIndex = 0;
    private LinearLayoutManager layoutManager;
    private TextView monthYearTextView;
    private ImageButton prevWeekButton;
    private ImageButton nextWeekButton;
    private int totalWeeks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weekly_header, container, false);

        weeklyRecyclerView = view.findViewById(R.id.weeklyRecyclerView);
        monthYearTextView = view.findViewById(R.id.monthYearTextView);
        prevWeekButton = view.findViewById(R.id.prevWeekButton);
        nextWeekButton = view.findViewById(R.id.nextWeekButton);

        daysList = new DaysList();
        totalWeeks = daysList.getTotalWeeks();

        setCurrentWeekToToday();

        dayAdapter = new DayAdapter(getContext(), daysList.getWeek(currentWeekIndex), new DayAdapter.OnDayClickListener() {
            @Override
            public void onDayClick(String dateId) {
                if (getActivity() instanceof OnDaySelectedListener) {
                    ((OnDaySelectedListener) getActivity()).onDaySelected(dateId);
                    Log.d("WeeklyHeaderFragment", "Day clicked: " + dateId);
                } else {
                    Log.e("WeeklyHeaderFragment", "Activity must implement OnDaySelectedListener");
                }
            }
        });

        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        weeklyRecyclerView.setLayoutManager(layoutManager);
        weeklyRecyclerView.setAdapter(dayAdapter);

        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(weeklyRecyclerView);

        setupSwipeDetection();

        setupNavigationButtons();

        updateMonthYearDisplay();

        selectTodayByDefault();

        return view;
    }

    private void setCurrentWeekToToday() {
        currentWeekIndex = daysList.getCurrentWeekIndex();

        if (currentWeekIndex == -1) {
            currentWeekIndex = 0;
            Log.e("WeeklyHeaderFragment", "Could not find current week in DaysList");
        }
    }

    private void setupSwipeDetection() {
        weeklyRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int firstVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
                    if (firstVisibleItemPosition != RecyclerView.NO_POSITION) {
                        //TODO: not done
                        int newWeekIndex = firstVisibleItemPosition / 7;

                        if (newWeekIndex != currentWeekIndex) {
                            currentWeekIndex = newWeekIndex;
                            updateWeek();
                            updateMonthYearDisplay();
                            Log.d("WeeklyHeaderFragment", "Swiped to Week Index: " + currentWeekIndex);
                        }
                    }
                }
            }
        });
    }

    private void setupNavigationButtons() {
        prevWeekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToPreviousWeek();
            }
        });

        nextWeekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToNextWeek();
            }
        });
    }

    private void navigateToPreviousWeek() {
        if (currentWeekIndex > 0) {
            currentWeekIndex--;
            scrollToCurrentWeek();
            updateWeek();
            updateMonthYearDisplay();
            Log.d("WeeklyHeaderFragment", "Week Index: " + currentWeekIndex);
        }
    }

    private void navigateToNextWeek() {
        if (currentWeekIndex < totalWeeks - 1) {
            currentWeekIndex++;
            scrollToCurrentWeek();
            updateWeek();
            updateMonthYearDisplay();
            Log.d("WeeklyHeaderFragment", "Week Index: " + currentWeekIndex);
        }
    }

    private void scrollToCurrentWeek() {
        int scrollPosition = currentWeekIndex * 7;
        layoutManager.scrollToPositionWithOffset(scrollPosition, 0);
        Log.d("WeeklyHeaderFragment", "Scroll Position: " + scrollPosition);
    }

    private void updateWeek() {
        ArrayList<DayModel> newWeek = daysList.getWeek(currentWeekIndex);
        Log.d("WeeklyHeaderFragment", "New Week Data: " + newWeek.toString());
        dayAdapter.updateDays(newWeek);
    }

    private void updateMonthYearDisplay() {
        if (monthYearTextView != null) {
            ArrayList<DayModel> currentWeek = daysList.getWeek(currentWeekIndex);
            if (!currentWeek.isEmpty()) {
                DayModel middleDay = currentWeek.get(3);
                String month = middleDay.getMonth();
                String year = middleDay.getYear();

                String monthName = new DateFormatSymbols().getMonths()[Integer.parseInt(month) - 1];
                monthYearTextView.setText(monthName + " " + year);
                Log.d("WeeklyHeaderFragment", "Month-Year: " + monthName + " " + year);
            }
        }
    }

    private void selectTodayByDefault() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM", Locale.getDefault());
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());

        String date = dateFormat.format(calendar.getTime());
        String month = monthFormat.format(calendar.getTime());
        String year = yearFormat.format(calendar.getTime());

        String todayDateId = date + month + year;
        dayAdapter.setActiveDotByDateId(todayDateId);

        if (getActivity() instanceof OnDaySelectedListener) {
            ((OnDaySelectedListener) getActivity()).onDaySelected(todayDateId);
        }
    }

    public void navigateToDate(int year, int month, int day) {
        if (year < 2024 || year > 2100) {
            Log.e("WeeklyHeaderFragment", "Year out of supported range (2024-2100)");
            return;
        }

        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.set(year, month - 1, day);

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(2024, 0, 1);

        long diffInMillis = targetCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();
        int diffInWeeks = (int)(diffInMillis / (7 * 24 * 60 * 60 * 1000));

        currentWeekIndex = diffInWeeks;
        currentWeekIndex = Math.max(0, Math.min(currentWeekIndex, totalWeeks - 1));

        scrollToCurrentWeek();
        updateWeek();
        updateMonthYearDisplay();

        String dateId = String.format(Locale.US, "%02d%02d%d", day, month, year);
        dayAdapter.setActiveDotByDateId(dateId);

        if (getActivity() instanceof OnDaySelectedListener) {
            ((OnDaySelectedListener) getActivity()).onDaySelected(dateId);
        }
    }

    public interface OnDaySelectedListener {
        void onDaySelected(String dateId);
    }
}
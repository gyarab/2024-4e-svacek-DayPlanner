package com.example.dayplanner.main.dayslist;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.example.dayplanner.R;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class WeeklyHeaderFragment extends Fragment {

    private RecyclerView weeklyRecyclerView;
    public DayAdapter dayAdapter;
    private DaysList daysList;
    private int currentWeekIndex = 0;
    private GridLayoutManager gridLayoutManager;
    private TextView monthYearTextView;
    private ImageButton prevWeekButton;
    private ImageButton nextWeekButton;
    private int totalWeeks;
    SnapHelper snapHelper;

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

        monthYearTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });

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
        gridLayoutManager = new GridLayoutManager(getContext(), 7, GridLayoutManager.VERTICAL, false);
        weeklyRecyclerView.setLayoutManager(gridLayoutManager);

        weeklyRecyclerView.setLayoutManager(gridLayoutManager);
        weeklyRecyclerView.setAdapter(dayAdapter);

        snapHelper = new PagerSnapHelper();
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
        weeklyRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            private float startX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        return false;

                    case MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        float deltaX = startX - endX;

                        if (Math.abs(deltaX) > 100) {
                            if (deltaX > 0) {
                                navigateToNextWeek();
                            } else {
                                navigateToPreviousWeek();
                            }
                            return true;
                        }
                        break;
                }
                return false;
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
            weeklyRecyclerView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_left));
            scrollToCurrentWeek();
            updateWeek();
            updateMonthYearDisplay();
            Log.d("WeeklyHeaderFragment", "Week Index: " + currentWeekIndex);
        }
    }

    private void navigateToNextWeek() {
        if (currentWeekIndex < totalWeeks - 1) {
            currentWeekIndex++;
            weeklyRecyclerView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_right));
            scrollToCurrentWeek();
            updateWeek();
            updateMonthYearDisplay();
            Log.d("WeeklyHeaderFragment", "Week Index: " + currentWeekIndex);
        }
    }

    private void scrollToCurrentWeek() {
        int scrollPosition = currentWeekIndex * 7;
        gridLayoutManager.scrollToPositionWithOffset(scrollPosition, 0);
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
        Log.d("Navigate", year + " " + month + " " + day);
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

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            navigateToDate(year, month+1, dayOfMonth);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }
}
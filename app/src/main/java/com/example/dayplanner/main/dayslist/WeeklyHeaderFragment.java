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

        // Find current week based on today's date
        setCurrentWeekToToday();

        // Initialize adapter with the current week's data
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

        // Enables snapping to a full week view when swiping
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(weeklyRecyclerView);

        // Set up swipe detection
        setupSwipeDetection();

        // Set up navigation buttons
        setupNavigationButtons();

        // Update the month-year display based on initial week
        updateMonthYearDisplay();

        // Select today's date by default
        selectTodayByDefault();

        return view;
    }

    private void setCurrentWeekToToday() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);

        // Calculate the index based on year and week
        if (currentYear >= 2024 && currentYear <= 2100) {
            // Calculate weeks since 2024-01-01
            int yearOffset = currentYear - 2024;
            int weekOffset = currentWeek - 1; // Weeks are 0-indexed in our list

            // Approximate index (may need adjustment based on how your weeks are structured)
            currentWeekIndex = yearOffset * 52 + weekOffset;

            // Ensure within bounds
            currentWeekIndex = Math.max(0, Math.min(currentWeekIndex, totalWeeks - 1));
        } else {
            currentWeekIndex = 0; // Default to first week if outside range
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
                        // Calculate new week index based on the first visible item position
                        // Since we show a full week (7 days) at once, we need to handle it accordingly
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
        // Calculate position to scroll to (beginning of current week)
        int scrollPosition = currentWeekIndex * 7;
        layoutManager.scrollToPositionWithOffset(scrollPosition, 0);
        Log.d("WeeklyHeaderFragment", "Scroll Position: " + scrollPosition);
    }

    private void updateWeek() {
        ArrayList<DayModel> newWeek = daysList.getWeek(currentWeekIndex);
        dayAdapter.updateDays(newWeek);
        Log.d("WeeklyHeaderFragment", "New Week Data: " + newWeek.toString());
    }

    private void updateMonthYearDisplay() {
        if (monthYearTextView != null) {
            ArrayList<DayModel> currentWeek = daysList.getWeek(currentWeekIndex);
            if (!currentWeek.isEmpty()) {
                DayModel middleDay = currentWeek.get(3); // Thursday (middle of week)
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

        // Notify activity about the selected day
        if (getActivity() instanceof OnDaySelectedListener) {
            ((OnDaySelectedListener) getActivity()).onDaySelected(todayDateId);
        }
    }

    // Method to navigate to a specific date (can be called from parent activity)
    public void navigateToDate(int year, int month, int day) {
        if (year < 2024 || year > 2100) {
            Log.e("WeeklyHeaderFragment", "Year out of supported range (2024-2100)");
            return;
        }

        // Create a calendar instance and set it to the specified date
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.set(year, month - 1, day); // Month is 0-based in Calendar

        // Calculate weeks since start (2024-01-01)
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(2024, 0, 1); // January 1, 2024

        // Calculate the difference in weeks
        long diffInMillis = targetCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();
        int diffInWeeks = (int)(diffInMillis / (7 * 24 * 60 * 60 * 1000));

        // Set current week index and update UI
        currentWeekIndex = diffInWeeks;
        currentWeekIndex = Math.max(0, Math.min(currentWeekIndex, totalWeeks - 1));

        scrollToCurrentWeek();
        updateWeek();
        updateMonthYearDisplay();

        // Select the specific day
        String dateId = String.format(Locale.US, "%02d%02d%d", day, month, year);
        dayAdapter.setActiveDotByDateId(dateId);

        // Notify activity about the selected day
        if (getActivity() instanceof OnDaySelectedListener) {
            ((OnDaySelectedListener) getActivity()).onDaySelected(dateId);
        }
    }

    public interface OnDaySelectedListener {
        void onDaySelected(String dateId);
    }
}
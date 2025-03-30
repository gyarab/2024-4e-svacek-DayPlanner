package com.example.dayplanner.statistics;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dayplanner.R;
import com.example.dayplanner.main.MainActivity;
import com.example.dayplanner.main.habits.FirebaseHelper;
import com.example.dayplanner.main.habits.Habit;
import com.example.dayplanner.main.habits.HabitEntry;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatisticsActivity extends AppCompatActivity {

    FirebaseHelper firebaseHelper = new FirebaseHelper();
    private DatabaseReference habitsRef = firebaseHelper.getHabitsRef();
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Map<String, Date> dateCache = new HashMap<>();

    private Calendar currentCalendar;
    private SimpleDateFormat monthFormat = new SimpleDateFormat("MMyyyy", Locale.getDefault());
    private SimpleDateFormat dayMonthYearFormat = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());

    /** UI **/
    private TextView perfectDaysTextView, longestStreakTextView, tvMonthYear, totalMetricTextView;
    private CustomCircularProgressBar overallProgressPBar;
    private RecyclerView MonthlyProgressRecyclerView;
    private ImageButton btnPreviousMonth, btnNextMonth;
    private RecyclerView habitsRecyclerView;
    private HabitListAdapter habitListAdapter;
    private List<Habit> habitList = new ArrayList<>();
    private LineChart lineChart;
    private LinearLayout totalMetricLayout;
    MaterialToolbar settingsToolbar;
    private Map<String, StatisticsData> monthDataCache = new HashMap<>();

    private class StatisticsData {
        int overallProgress;
        int perfectDays;
        int longestStreak;
        int totalMetric;
        String metricType;
        LinkedHashMap<String, Float> dailyProgress = new LinkedHashMap<>();
        List<HabitProgressEntry> habitProgressData = new ArrayList<>();

        @Override
        public String toString() {
            return "StatisticsData{" +
                    "overallProgress=" + overallProgress +
                    ", perfectDays=" + perfectDays +
                    ", longestStreak=" + longestStreak +
                    ", totalMetric=" + totalMetric +
                    ", metricType='" + metricType + '\'' +
                    ", dailyProgress=" + dailyProgress +
                    ", habitProgressData=" + habitProgressData +
                    '}';
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_statistics);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        perfectDaysTextView = findViewById(R.id.tvPerfectDays);
        longestStreakTextView = findViewById(R.id.tvLongestStreak);
        overallProgressPBar = findViewById(R.id.overallProgressBar);
        totalMetricTextView = findViewById(R.id.totalMetric);
        totalMetricLayout = findViewById(R.id.totalMetricLayout);

        lineChart = findViewById(R.id.testLineChart);

        MonthlyProgressRecyclerView = findViewById(R.id.rvMonthlyProgress);
        MonthlyProgressRecyclerView.setLayoutManager(new GridLayoutManager(this, 7)); // 7 days per row

        tvMonthYear = findViewById(R.id.tvMonthYear);
        btnPreviousMonth = findViewById(R.id.btnPreviousMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);

        currentCalendar = Calendar.getInstance();

        updateMonthDisplay();

        btnPreviousMonth.setOnClickListener(v -> changeMonth(-1));
        btnNextMonth.setOnClickListener(v -> changeMonth(1));

        settingsToolbar = findViewById(R.id.topAppBar);
        settingsToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StatisticsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        /** recycler view for habits **/
        habitsRecyclerView = findViewById(R.id.rvHabitsList);
        habitsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        habitListAdapter = new HabitListAdapter(habitList, this::fetchDataForOneHabit);
        habitsRecyclerView.setAdapter(habitListAdapter);

        loadUserHabits();
        fetchAllHabitData(getCurrentMonthId());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void setupLineChart(LineChart lineChart, List<HabitProgressEntry> habitProgressData) {
        if (habitProgressData.size() <= 1) {
            lineChart.setVisibility(View.GONE);
            return;
        }

        lineChart.setVisibility(View.VISIBLE);

        Context context = lineChart.getContext();

        int[] attrs = new int[]{android.R.attr.colorPrimary, android.R.attr.colorSecondary, android.R.attr.textColorPrimary, android.R.attr.colorSecondary};
        TypedArray ta = context.obtainStyledAttributes(attrs);

        int lineColor = ta.getColor(0, Color.BLUE);
        int circleColor = ta.getColor(1, Color.RED);
        int textColor = ta.getColor(2, Color.BLACK);
        int fillColor = ta.getColor(3, Color.LTGRAY);

        ta.recycle();

        List<Entry> entries = new ArrayList<>();
        List<String> xLabels = new ArrayList<>();

        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("ddMMyyyy", Locale.ENGLISH);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale.ENGLISH);

        for (int i = 0; i < habitProgressData.size(); i++) {
            HabitProgressEntry entry = habitProgressData.get(i);

            String rawDate = entry.getDate();
            String formattedDate;
            try {
                LocalDate date = LocalDate.parse(rawDate, inputFormatter);
                formattedDate = date.format(outputFormatter);
            } catch (Exception e) {
                formattedDate = rawDate;
            }

            entries.add(new Entry(i, entry.getGoalValue()));
            xLabels.add(formattedDate);
        }

        LineDataSet dataSet = new LineDataSet(entries, "Progress Over Time");
        dataSet.setColor(lineColor);
        dataSet.setValueTextColor(textColor);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(circleColor);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(fillColor);
        dataSet.setValueTextSize(12f);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(textColor);
        xAxis.setTextSize(14f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xLabels));

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setTextColor(textColor);
        leftAxis.setTextSize(14f);
        lineChart.getAxisRight().setEnabled(false);

        lineChart.invalidate();
    }

    private void changeMonth(int direction) {
        currentCalendar.add(Calendar.MONTH, direction);
        updateMonthDisplay();
        fetchAllHabitData(getCurrentMonthId());
    }

    private void updateMonthDisplay() {
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvMonthYear.setText(displayFormat.format(currentCalendar.getTime()));
    }

    private String getCurrentMonthId() {
        return monthFormat.format(currentCalendar.getTime());
    }

    private String getActualCurrentMonthId() {
        return monthFormat.format(new Date());
    }

    private Date getDateFromCache(String dateStr) {
        if (!dateCache.containsKey(dateStr)) {
            try {
                dateCache.put(dateStr, dayMonthYearFormat.parse(dateStr));
            } catch (ParseException e) {
                Log.e("DateCache", "Error parsing date: " + dateStr);
                return null;
            }
        }
        return dateCache.get(dateStr);
    }

    public int calculateMonthOverallProgress(HashMap<String, Float> dailyCompletionPercentages) {
        Log.d("calculateMonthOverallProgress", "START");
        if (dailyCompletionPercentages.isEmpty()) {
            Log.d("calculateMonthOverallProgress", "EMPTY");
            return 0;
        }

        float sumOfAllPercentages = 0.0f;
        int numberOfRecords = 0;
        for (Map.Entry<String, Float> record : dailyCompletionPercentages.entrySet()) {
            sumOfAllPercentages += record.getValue();
            numberOfRecords++;
        }
        Log.d("calculateMonthOverallProgress", "END " + sumOfAllPercentages + " " + numberOfRecords);

        return numberOfRecords > 0 ? Math.round(sumOfAllPercentages / numberOfRecords) : 0;
    }

    private void loadUserHabits() {
        habitsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                habitList.clear();
                for (DataSnapshot habitSnapshot : snapshot.getChildren()) {
                    Habit habit = habitSnapshot.getValue(Habit.class);
                    if (habit != null) {
                        habit.setId(habitSnapshot.getKey());
                        habitList.add(habit);
                    }
                }
                habitListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("loadUserHabits", "Error fetching habits: " + error.getMessage());
            }
        });
    }

    private void fetchAllHabitData(String monthId) {
        if (monthDataCache.containsKey(monthId)) {
            updateUIWithCachedData(monthDataCache.get(monthId));
            Log.d("DATA", (monthDataCache.get(monthId)).toString());
            Log.d("fetchAllHabitData", "CACHED");
            return;
        }

        Log.d("fetchAllHabitData", "NOT CACHED");
        Log.d("fetchAllHabitData", "Fetching habits for month: " + monthId);
        lineChart.setVisibility(View.GONE);
        totalMetricLayout.setVisibility(View.GONE);

        habitsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                executor.execute(() -> {
                    processHabitData(dataSnapshot, monthId);
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("fetchAllHabitData", "Error fetching habits: " + databaseError.getMessage());
            }
        });
    }

    private void processHabitData(DataSnapshot dataSnapshot, String monthId) {
        LinkedHashMap<String, Float> dailyTotalPercentage = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> dailyEntryCount = new LinkedHashMap<>();
        LinkedHashMap<String, Boolean> perfectDays = new LinkedHashMap<>();

        String currentDateStr = dayMonthYearFormat.format(new Date());
        String firstDayOfMonth = "01" + monthId;
        String lastDayOfMonth = getLastDayOfMonth(monthId);

        for (DataSnapshot habitSnapshot : dataSnapshot.getChildren()) {
            Habit habit = habitSnapshot.getValue(Habit.class);
            if (habit == null) continue;

            try {
                processHabitForMonth(habit, monthId, currentDateStr, dailyTotalPercentage, dailyEntryCount, perfectDays);
            } catch (Exception e) {
                Log.e("processHabitData", "Error processing habit: " + e.getMessage());
            }
        }

        // Calculate statistics from processed data
        LinkedHashMap<String, Float> dailyAveragePercentage = calculateDailyAverages(dailyTotalPercentage, dailyEntryCount);
        int overallProgress = calculateMonthOverallProgress(dailyAveragePercentage);
        int perfectDaysCount = countPerfectDays(perfectDays, monthId);
        int longestStreak = countLongestStreak(perfectDays);

        // Create and cache StatisticsData
        StatisticsData monthData = new StatisticsData();
        monthData.overallProgress = overallProgress;

        Log.d("MONTHLX", ""+ monthData.overallProgress);

        monthData.perfectDays = perfectDaysCount;
        monthData.longestStreak = longestStreak;
        monthData.dailyProgress = dailyAveragePercentage;

        monthDataCache.put(monthId, monthData);

        runOnUiThread(() -> updateUIWithCachedData(monthData));
    }

    private void processHabitForMonth(Habit habit, String monthId, String currentDateStr,
                                      LinkedHashMap<String, Float> dailyTotalPercentage,
                                      LinkedHashMap<String, Integer> dailyEntryCount,
                                      LinkedHashMap<String, Boolean> perfectDays) throws ParseException {

        String startDate = habit.getStartDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getDateFromCache(startDate));

        while (true) {
            String dateKey = dayMonthYearFormat.format(calendar.getTime());

            // Stop if we've gone past today
            if (dateKey.compareTo(currentDateStr) > 0) break;

            // Process only dates in the requested month
            if (dateKey.substring(2, 8).equals(monthId)) {
                if (habit.isHabitVisibleOnDate(dateKey)) {
                    if (!dailyTotalPercentage.containsKey(dateKey)) {
                        dailyTotalPercentage.put(dateKey, 0.0f);
                        dailyEntryCount.put(dateKey, 0);
                        perfectDays.put(dateKey, true); //assume true from beginning
                    }

                    HabitEntry habitEntry = habit.getEntryForDate(dateKey);
                    if (habitEntry != null) {
                        float percentage = (float) habitEntry.getProgress() / habitEntry.getEntryGoalValue() * 100;
                        dailyTotalPercentage.put(dateKey, dailyTotalPercentage.get(dateKey) + percentage);
                        dailyEntryCount.put(dateKey, dailyEntryCount.get(dateKey) + 1);

                        if (percentage < 100) {
                            perfectDays.put(dateKey, false);
                        }
                    } else {
                        // No entry means incomplete
                        dailyEntryCount.put(dateKey, dailyEntryCount.get(dateKey) + 1);
                        perfectDays.put(dateKey, false);
                    }
                }
            }

            // Move to next day
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            if (dateKey.equals(currentDateStr)) break;
        }
    }

    private LinkedHashMap<String, Float> calculateDailyAverages(LinkedHashMap<String, Float> totals, LinkedHashMap<String, Integer> counts) {
        LinkedHashMap<String, Float> averages = new LinkedHashMap<>();
        for (String date : totals.keySet()) {
            int count = counts.get(date);
            averages.put(date, count > 0 ? totals.get(date) / count : 0.0f);
        }
        return averages;
    }

    private int countPerfectDays(LinkedHashMap<String, Boolean> perfectDays, String monthId) {
        int count = 0;
        for (Map.Entry<String, Boolean> entry : perfectDays.entrySet()) {
            if (entry.getKey().substring(2, 8).equals(monthId) && entry.getValue()) {
                count++;
            }
        }
        return count;
    }

    private void updateUIWithCachedData(StatisticsData data) {
        overallProgressPBar.setProgress(data.overallProgress);
        overallProgressPBar.setTextStyle(Typeface.NORMAL);
        overallProgressPBar.setTextSize(80f);

        Log.d("STATS", data.toString());

        perfectDaysTextView.setText("Perfect Days: " + data.perfectDays);
        longestStreakTextView.setText("Longest Streak: " + data.longestStreak);

        updateMonthlyProgressRecyclerView(data.dailyProgress);

        if (data.metricType != null) {
            totalMetricLayout.setVisibility(View.VISIBLE);
            totalMetricTextView.setText("In total: " + data.totalMetric + " " + data.metricType);
        } else {
            totalMetricLayout.setVisibility(View.GONE);
        }

        if (data.habitProgressData != null && !data.habitProgressData.isEmpty()) {
            setupLineChart(lineChart, data.habitProgressData);
        } else {
            lineChart.setVisibility(View.GONE);
        }
    }

    private void updateMonthlyProgressRecyclerView(HashMap<String, Float> dailyProgress) {
        List<DailyProgress> dayProgressList = new ArrayList<>();
        for (Map.Entry<String, Float> entry : dailyProgress.entrySet()) {
            int day = Integer.parseInt(entry.getKey().substring(0, 2));
            float completion = entry.getValue();
            dayProgressList.add(new DailyProgress(day, completion));
        }

        MonthlyProgressAdapter adapter = new MonthlyProgressAdapter(this, dayProgressList);
        MonthlyProgressRecyclerView.setAdapter(adapter);
    }

    private void fetchDataForOneHabit(String habitId) {
        String currentMonthId = getCurrentMonthId();
        DatabaseReference oneHabitRef = FirebaseHelper.getHabitsRef().child(habitId);

        oneHabitRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                executor.execute(() -> {
                    Habit habit = dataSnapshot.getValue(Habit.class);
                    if (habit == null) return;

                    processHabitDetails(habit, currentMonthId);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("fetchDataForOneHabit", "Error: " + error.getMessage());
            }
        });
    }

    private void processHabitDetails(Habit habit, String currentMonthId) {
        try {
            Map<String, HabitEntry> entries = habit.getEntries();
            LinkedHashMap<String, Float> dailyCompletionPercentages = new LinkedHashMap<>();
            List<HabitProgressEntry> habitProgressData = new ArrayList<>();
            int totalMetric = 0;

            String todayDate = dayMonthYearFormat.format(new Date());
            String firstDayOfMonth = "01" + currentMonthId;
            String lastDayOfMonth = getLastDayOfMonth(currentMonthId);

            // Determine the correct end date
            String lastAvailableDate;
            if (currentMonthId.equals(getActualCurrentMonthId())) {
                lastAvailableDate = todayDate; // Limit to today if it's the current month
            } else {
                lastAvailableDate = lastDayOfMonth;
            }

            Date startDate = getDateFromCache(firstDayOfMonth);
            Date endDate = getDateFromCache(lastAvailableDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);

            while (!calendar.getTime().after(endDate)) {
                String entryDate = dayMonthYearFormat.format(calendar.getTime());

                if (habit.isHabitVisibleOnDate(entryDate)) {
                    // Check if entry exists, otherwise set progress to 0
                    if (entries.containsKey(entryDate)) {
                        HabitEntry habitEntry = entries.get(entryDate);
                        float percentage = (float) habitEntry.getProgress() / habitEntry.getEntryGoalValue() * 100;
                        dailyCompletionPercentages.put(entryDate, percentage);
                        totalMetric += habitEntry.getProgress();

                        habitProgressData.add(new HabitProgressEntry(entryDate, habitEntry.getProgress(), habitEntry.getEntryGoalValue()));
                    } else {
                        dailyCompletionPercentages.put(entryDate, 0f);
                    }
                }

                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }

            int overallMonthProgress = calculateMonthOverallProgress(dailyCompletionPercentages);
            LinkedHashMap<String, Boolean> dailyCompletionBooleans = convertToBoolean(dailyCompletionPercentages);
            int perfectDaysCount = countPerfectDaysForHabit(dailyCompletionBooleans);
            int longestStreak = countLongestStreak(dailyCompletionBooleans);

            List<HabitProgressEntry> formattedData = formatHabitProgressDataForChart(habitProgressData);

            StatisticsData habitData = new StatisticsData();
            habitData.overallProgress = overallMonthProgress;
            habitData.perfectDays = perfectDaysCount;
            habitData.longestStreak = longestStreak;
            habitData.totalMetric = totalMetric;
            habitData.metricType = habit.getMetric();
            habitData.dailyProgress = dailyCompletionPercentages;
            habitData.habitProgressData = formattedData;

            runOnUiThread(() -> updateUIWithCachedData(habitData));
        } catch (Exception e) {
            Log.e("processHabitDetails", "Error: " + e.getMessage());
        }
    }

    private LinkedHashMap<String, Boolean> convertToBoolean(LinkedHashMap<String, Float> percentages) {
        LinkedHashMap<String, Boolean> result = new LinkedHashMap<>();
        for (Map.Entry<String, Float> entry : percentages.entrySet()) {
            result.put(entry.getKey(), entry.getValue() == 100);
        }
        return result;
    }

    private int countPerfectDaysForHabit(LinkedHashMap<String, Boolean> dailyCompletions) {
        int perfectDays = 0;
        for (Boolean isComplete : dailyCompletions.values()) {
            if (isComplete) perfectDays++;
        }
        return perfectDays;
    }

    public String getLastDayOfMonth(String monthId) {
        try {
            int month = Integer.parseInt(monthId.substring(0, 2)) - 1;
            int year = Integer.parseInt(monthId.substring(2, 6));

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

            return dayMonthYearFormat.format(calendar.getTime());
        } catch (Exception e) {
            Log.e("getLastDayOfMonth", "Invalid monthId format: " + monthId, e);
            return null;
        }
    }

    private int countLongestStreak(LinkedHashMap<String, Boolean> perfectDays) {
        int longestStreak = 0;
        int currentStreak = 0;

        for (Boolean isPerfect : perfectDays.values()) {
            if (isPerfect) {
                currentStreak++;
                longestStreak = Math.max(longestStreak, currentStreak);
            } else {
                currentStreak = 0;
            }
        }

        return longestStreak;
    }

    private List<HabitProgressEntry> formatHabitProgressDataForChart(List<HabitProgressEntry> habitProgressData) {
        List<HabitProgressEntry> formattedData = new ArrayList<>();

        if (habitProgressData.isEmpty()) return formattedData;

        int lastGoalValue = -1;

        for (HabitProgressEntry entry : habitProgressData) {
            if (lastGoalValue != entry.getGoalValue()) {
                formattedData.add(entry);
                lastGoalValue = entry.getGoalValue();
            }
        }

        return formattedData;
    }
}
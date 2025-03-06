package com.example.dayplanner.statistics;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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
import com.example.dayplanner.main.habits.FirebaseHelper;
import com.example.dayplanner.main.habits.Habit;
import com.example.dayplanner.main.habits.HabitEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity {

    FirebaseHelper firebaseHelper = new FirebaseHelper();
    private DatabaseReference habitsRef = firebaseHelper.getHabitsRef();

    private Calendar currentCalendar;
    private SimpleDateFormat monthFormat = new SimpleDateFormat("MMyyyy", Locale.getDefault());

    /** UI **/
    private TextView overallProgressTextView, perfectDaysTextView, longestStreakTextView, tvMonthYear;
    private CustomCircularProgressBar overallProgressPBar;
    private RecyclerView MonthlyProgressRecyclerView;
    private ImageButton btnPreviousMonth, btnNextMonth;
    private RecyclerView habitsRecyclerView;
    private HabitListAdapter habitListAdapter;
    private List<Habit> habitList = new ArrayList<>();

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

        //overallProgressTextView = findViewById(R.id.tvOverallProgress);
        perfectDaysTextView = findViewById(R.id.tvPerfectDays);
        longestStreakTextView = findViewById(R.id.tvLongestStreak);
        overallProgressPBar = findViewById(R.id.overallProgressBar);
        //MonthlyProgressRecyclerView = findViewById(R.id.rvMonthlyProgress);

        MonthlyProgressRecyclerView = findViewById(R.id.rvMonthlyProgress);
        MonthlyProgressRecyclerView.setLayoutManager(new GridLayoutManager(this, 7)); // 7 days per row

        tvMonthYear = findViewById(R.id.tvMonthYear);
        btnPreviousMonth = findViewById(R.id.btnPreviousMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);

        currentCalendar = Calendar.getInstance();

        updateMonthDisplay();
        fetchAndStoreHabitsForMonth(getCurrentMonthId());

        btnPreviousMonth.setOnClickListener(v -> changeMonth(-1));
        btnNextMonth.setOnClickListener(v -> changeMonth(1));

        /** recycler view for habits **/
        habitsRecyclerView = findViewById(R.id.rvHabitsList);
        habitsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        habitListAdapter = new HabitListAdapter(habitList, this::fetchDataForOneHabit);
        habitsRecyclerView.setAdapter(habitListAdapter);

        loadUserHabits();

        String monthId = "032025";

        //TODO: fetch for current date
        fetchAndStoreHabitsForMonth(monthId);

        //TODO: on click on habit element in UI
        fetchDataForOneHabit("-OKNMSZx8GHaqlG6ZxDp");

        countPerfectDays(monthId);
    }
    private void changeMonth(int direction) {
        currentCalendar.add(Calendar.MONTH, direction);
        updateMonthDisplay();
        fetchAndStoreHabitsForMonth(getCurrentMonthId());
        countPerfectDays(getCurrentMonthId());
    }
    private void updateMonthDisplay() {
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvMonthYear.setText(displayFormat.format(currentCalendar.getTime()));
    }
    private String getCurrentMonthId() {
        return monthFormat.format(currentCalendar.getTime());
    }
    public int calculateMonthOverallProgress(HashMap<String, Float> dailyCompletionPercentages) {
        Float sumOfAllPercentages = 0.0f;
        int numberOfRecords = 0;
        for (Map.Entry<String, Float> record : dailyCompletionPercentages.entrySet()) {
            Log.d("calculateMonthOverallProgress", "Date: " + record.getKey() + ", Completion: " + record.getValue() + "%");

            sumOfAllPercentages += record.getValue();
            numberOfRecords ++;
        }

        int result = Math.round(sumOfAllPercentages / numberOfRecords);
        Log.d("calculateMonthOverallProgress", "Overall Progress: " + result + "%");

        return result;
    }

    private void loadUserHabits() {
        habitsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                habitList.clear();
                for (DataSnapshot habitSnapshot : snapshot.getChildren()) {
                    Habit habit = habitSnapshot.getValue(Habit.class);
                    if (habit != null) {
                        habit.setId(habitSnapshot.getKey()); // Set habit ID
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

    public void fetchAndStoreHabitsForMonth(String monthId) {
        Log.d("fetchAndStoreHabits", "Fetching habits for month: " + monthId);

        habitsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LinkedHashMap<String, Float> dailyTotalPercentage = new LinkedHashMap<>();
                LinkedHashMap<String, Integer> dailyEntryCount = new LinkedHashMap<>();

                long currentDateMillis = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
                String currentDateStr = sdf.format(new Date(currentDateMillis));

                for (DataSnapshot habitSnapshot : dataSnapshot.getChildren()) {
                    Habit habit = habitSnapshot.getValue(Habit.class);

                    if (habit != null) {
                        Log.d("fetchAndStoreHabits", "Processing habit: " + habit.getName());
                        String startDate = habit.getStartDate();

                        Calendar calendar = Calendar.getInstance();
                        try {
                            calendar.setTime(sdf.parse(startDate));
                        } catch (ParseException e) {
                            Log.e("fetchAndStoreHabits", "Invalid start date format: " + startDate);
                            continue;
                        }

                        while (true) {
                            String dateKey = sdf.format(calendar.getTime());

                            if (dateKey.substring(2, 8).equals(monthId)) {
                                if (dateKey.compareTo(currentDateStr) > 0) {
                                    break;
                                }

                                // Ensure the habit is visible on this date
                                if (habit.isHabitVisibleOnDate(dateKey)) {
                                    if (!dailyTotalPercentage.containsKey(dateKey)) {
                                        dailyTotalPercentage.put(dateKey, 0.0f);
                                        dailyEntryCount.put(dateKey, 0);
                                    }

                                    HabitEntry habitEntry = habit.getEntryForDate(dateKey);
                                    if (habitEntry != null) {
                                        float percentage = (float) habitEntry.getProgress() / habitEntry.getEntryGoalValue() * 100;
                                        dailyTotalPercentage.put(dateKey, dailyTotalPercentage.get(dateKey) + percentage);
                                        dailyEntryCount.put(dateKey, dailyEntryCount.get(dateKey) + 1);
                                    } else {
                                        dailyEntryCount.put(dateKey, dailyEntryCount.get(dateKey) + 1);
                                    }
                                }
                            }

                            calendar.add(Calendar.DAY_OF_MONTH, 1);
                            if (dateKey.equals(currentDateStr)) break;
                        }
                    }
                }

                LinkedHashMap<String, Float> dailyAveragePercentage = new LinkedHashMap<>();
                for (String date : dailyTotalPercentage.keySet()) {
                    int count = dailyEntryCount.get(date);
                    dailyAveragePercentage.put(date, count > 0 ? dailyTotalPercentage.get(date) / count : 0.0f);
                }

                Log.d("fetchAndStoreHabits", "dailyTotalPercentage: " + dailyTotalPercentage);
                Log.d("fetchAndStoreHabits", "dailyEntryCount: " + dailyEntryCount);
                Log.d("fetchAndStoreHabits", "dailyAveragePercentage: " + dailyAveragePercentage);

                updateUIWithMonthlyProgress(dailyAveragePercentage);
                updateUIWithMonthOverallProgress(calculateMonthOverallProgress(dailyAveragePercentage));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("fetchAndStoreHabits", "Error fetching habits: " + databaseError.getMessage());
            }
        });
    }


    private void fetchDataForOneHabit(String habitId) {
        //TODO: fetch data for one habit
        Log.d("fetchAndStoreHabits", "Fetching data for habit: " + habitId);

        String monthId = getCurrentMonthId(); // Get the current month ID
        DatabaseReference oneHabitRef = FirebaseHelper.getHabitsRef().child(habitId);

        oneHabitRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String currentMonthId = getCurrentMonthId();

                Habit habit = dataSnapshot.getValue(Habit.class);
                if (habit != null) {
                    Log.d("fetchAndStoreHabits - v", "Fetched Habit for: " + currentMonthId + " is:" + habit.toString());

                    // Get all entries from the habit
                    Map<String, HabitEntry> entries = habit.getEntries();

                    // Clear the previous entries (assuming you're using a Map to store them)
                    Map<String, HabitEntry> currentEntries = new LinkedHashMap<>();

                    // Iterate through each entry in the habit's entries map
                    for (Map.Entry<String, HabitEntry> entry : entries.entrySet()) {
                        String entryDate = entry.getKey();  // The key is the date of the entry (assumed to be in ddMMyyyy format)

                        // Compare month part of the date (assuming it's in ddMMyyyy format)
                        if (entryDate.substring(2, 8).equals(currentMonthId)) {
                            // This entry matches the monthId, process it
                            HabitEntry habitEntry = entry.getValue();
                            Log.d("fetchAndStoreHabits", "Matching entry: " + habitEntry.toString());

                            // Add matching entries to the currentEntries map or process as needed
                            currentEntries.put(entryDate, habitEntry);
                        }
                    }

                    // Now, currentEntries will only contain the habit entries for the current month
                    // You can use currentEntries to update your UI, save to local storage, etc.
                    Log.d("fetchAndStoreHabits", "Filtered entries for the current month: " + currentEntries);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
                Log.e("fetchAndStoreHabits", "Error fetching habit: " + error.getMessage());
            }
        });
    }




    private void countPerfectDays(String monthId) {
        /** perfect day = all visible habits of the day are completed **/

        habitsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LinkedHashMap<String, Boolean> perfectDays = new LinkedHashMap<>(); // Track perfect days

                long currentDateMillis = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
                String currentDateStr = sdf.format(new Date(currentDateMillis));

                for (DataSnapshot habitSnapshot : dataSnapshot.getChildren()) {
                    Habit habit = habitSnapshot.getValue(Habit.class);
                    if (habit == null) continue;

                    String startDate = habit.getStartDate();
                    Calendar calendar = Calendar.getInstance();

                    try {
                        calendar.setTime(sdf.parse(startDate)); // Start from habit's start date
                    } catch (ParseException e) {
                        Log.e("countPerfectDays", "Invalid start date format: " + startDate);
                        continue;
                    }

                    while (true) {
                        String dateKey = sdf.format(calendar.getTime());

                        if (dateKey.compareTo(currentDateStr) > 0) {
                            break; // Stop at today’s date
                        }

                        if (dateKey.substring(2, 8).equals(monthId) && habit.isHabitVisibleOnDate(dateKey)) {
                            // Initialize the day as perfect (true) if not already present
                            if (!perfectDays.containsKey(dateKey)) {
                                perfectDays.put(dateKey, true);
                            }

                            HabitEntry habitEntry = habit.getEntryForDate(dateKey);
                            if (habitEntry == null || habitEntry.getProgress() < habitEntry.getEntryGoalValue()) {
                                // Entry missing or not 100% -> so not a perfect day
                                perfectDays.put(dateKey, false);
                            }
                        }

                        calendar.add(Calendar.DAY_OF_MONTH, 1); // Move to next day
                        if (dateKey.equals(currentDateStr)) break;
                    }
                }

                // Count the number of perfect days in the given month
                int perfectDaysCount = 0;
                for (String date : perfectDays.keySet()) {
                    if (date.substring(2, 8).equals(monthId) && perfectDays.get(date)) {
                        perfectDaysCount++;
                    }
                }

                // Calculate longest streak
                int longestStreak = countLongestStreak(perfectDays);

                Log.d("countPerfectDays", "Perfect days in " + monthId + ": " + perfectDaysCount);
                Log.d("countPerfectDays", "Longest streak in " + monthId + ": " + longestStreak);

                updateUIWithLongestStreak(longestStreak);
                updateUIWithPerfectDays(perfectDaysCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("countPerfectDays", "Error fetching habits: " + error.getMessage());
            }
        });
    }

    /**
     * Counts the longest streak of consecutive perfect days in the given month.
     */
    private int countLongestStreak(LinkedHashMap<String, Boolean> perfectDays) {
        int longestStreak = 0;
        int currentStreak = 0;

        for (String date : perfectDays.keySet()) {
            if (perfectDays.get(date)) {
                currentStreak++;
                longestStreak = Math.max(longestStreak, currentStreak);
            } else {
                currentStreak = 0; // Reset streak if there's a break
            }
        }

        return longestStreak;
    }

    private void updateUIWithLongestStreak(int longestStreak) {
        runOnUiThread(() -> longestStreakTextView.setText("Longest Streak: " + longestStreak + " days"));
    }



    private void updateUIWithMonthlyProgress(HashMap<String, Float> dailyCompletionPercentages) {
        Log.d("Monthly Progress", "Updating UI with monthly progress: " + dailyCompletionPercentages.toString());

        List<DailyProgress> dayProgressList = new ArrayList<>();
        for (Map.Entry<String, Float> entry : dailyCompletionPercentages.entrySet()) {
            int day = Integer.parseInt(entry.getKey().substring(0, 2)); // Extract day from date format "ddMMyyyy"
            float completion = entry.getValue();
            dayProgressList.add(new DailyProgress(day, completion));
        }

        runOnUiThread(() -> {
            MonthlyProgressAdapter adapter = new MonthlyProgressAdapter(this, dayProgressList);
            MonthlyProgressRecyclerView.setAdapter(adapter);
        });
    }


    private void updateUIWithMonthOverallProgress(int overallMonthProgress) {
        runOnUiThread(() -> {
            overallProgressPBar.setProgress(overallMonthProgress);
            overallProgressPBar.setTextStyle(Typeface.NORMAL);
            overallProgressPBar.setTextSize(80f);
        });
    }


    private void updateUIWithPerfectDays(int perfectDaysCount) {
        runOnUiThread(() -> perfectDaysTextView.setText("Perfect Days: " + perfectDaysCount));
    }

}

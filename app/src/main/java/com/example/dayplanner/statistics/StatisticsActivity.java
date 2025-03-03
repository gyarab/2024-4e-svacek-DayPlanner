package com.example.dayplanner.statistics;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dayplanner.R;
import com.example.dayplanner.main.habits.FirebaseHelper;
import com.example.dayplanner.main.habits.Habit;
import com.example.dayplanner.main.habits.HabitEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

        String monthId = "032025";

        //TODO: fetch for current date
        fetchAndStoreHabitsForMonth(monthId);

        //TODO: on click on habit element in UI
        fetchDataForOneHabit("-OKNMSZx8GHaqlG6ZxDp");

        countPerfectDays(monthId);
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

    public void fetchAndStoreHabitsForMonth(String monthId) {
        Log.d("fetchAndStoreHabits", "Fetching habits for month: " + monthId);

        habitsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LinkedHashMap<String, Float> dailyTotalPercentage = new LinkedHashMap<>(); //Calculates total percentage for each day for all habits
                LinkedHashMap<String, Integer> dailyEntryCount = new LinkedHashMap<>(); //Calculates all habit entries for each day

                long currentDateMillis = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
                String currentDateStr = sdf.format(new Date(currentDateMillis));

                for (DataSnapshot habitSnapshot : dataSnapshot.getChildren()) {
                    //Iterating through all habits
                    Habit habit = habitSnapshot.getValue(Habit.class);

                    if (habit != null) {
                        Log.d("fetchAndStoreHabits", "Processing habit: " + habit.getName());
                        String startDate = habit.getStartDate();

                        // Iterate from startDate to currentDate
                        Calendar calendar = Calendar.getInstance();
                        try {
                            calendar.setTime(sdf.parse(startDate)); // Start from habit's start date
                        } catch (ParseException e) {
                            Log.e("fetchAndStoreHabits", "Invalid start date format: " + startDate);
                            continue;
                        }

                        while (true) {
                            String dateKey = sdf.format(calendar.getTime()); // Convert to ddMMyyyy format

                            if (dateKey.substring(2, 8).equals(monthId)) { //01022025 -> 022025
                                if (dateKey.compareTo(currentDateStr) > 0) {
                                    break; // Stop when exceeding today’s date
                                }

                                if (!dailyTotalPercentage.containsKey(dateKey)) {
                                    dailyTotalPercentage.put(dateKey, 0.0f);
                                    dailyEntryCount.put(dateKey, 0); // Assume zero entries initially
                                }

                                // Check if habit has an entry for this date
                                HabitEntry habitEntry = habit.getEntryForDate(dateKey);
                                if (habitEntry != null) {
                                    float percentage = (float) habitEntry.getProgress() / habitEntry.getEntryGoalValue() * 100;
                                    dailyTotalPercentage.put(dateKey, dailyTotalPercentage.get(dateKey) + percentage);
                                    dailyEntryCount.put(dateKey, dailyEntryCount.get(dateKey) + 1);
                                } else {
                                    // No recorded entry, but habit existed → Count as 0% completion
                                    dailyEntryCount.put(dateKey, dailyEntryCount.get(dateKey) + 1);
                                }
                            }

                            calendar.add(Calendar.DAY_OF_MONTH, 1); // Move to next day
                            if (dateKey.equals(currentDateStr)) break; // Stop at today’s date
                        }
                    }
                }

                // Calculate daily average percentage
                LinkedHashMap<String, Float> dailyAveragePercentage = new LinkedHashMap<>();
                for (String date : dailyTotalPercentage.keySet()) {
                    int count = dailyEntryCount.get(date);
                    if (count > 0) {
                        dailyAveragePercentage.put(date, dailyTotalPercentage.get(date) / count);
                    } else {
                        dailyAveragePercentage.put(date, 0.0f);
                    }
                }

                Log.d("fetchAndStoreHabits", "dailyTotalPercentage: " + dailyTotalPercentage);
                Log.d("fetchAndStoreHabits", "dailyEntryCount: " + dailyEntryCount);
                Log.d("fetchAndStoreHabits", "dailyAveragePercentage: " + dailyAveragePercentage);

                updateUIWithMonthlyProgress(dailyAveragePercentage);

                int overallMonthProgress = calculateMonthOverallProgress(dailyAveragePercentage);

                updateUIWithMonthOverallProgress(overallMonthProgress);
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

        DatabaseReference oneHabitRef = FirebaseHelper.getHabitsRef().child(habitId);
        oneHabitRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Habit habit = dataSnapshot.getValue(Habit.class);
                if (habit != null) {
                    Log.d("fetchAndStoreHabits", "Fetched Habit: " + habit.toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void countPerfectDays(String monthId) {
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

                        // Check if the date belongs to the given month
                        if (dateKey.substring(2, 8).equals(monthId)) { // Extract MMYYYY part
                            // Initialize the day as perfect (true) if not already present
                            if (!perfectDays.containsKey(dateKey)) {
                                perfectDays.put(dateKey, true);
                            }

                            HabitEntry habitEntry = habit.getEntryForDate(dateKey);
                            if (habitEntry == null || habitEntry.getProgress() < habitEntry.getEntryGoalValue()) {
                                // Entry missing or not 100%, so not a perfect day
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

                Log.d("countPerfectDays", "Perfect days in " + monthId + ": " + perfectDaysCount);
                updateUIWithPerfectDays(perfectDaysCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("countPerfectDays", "Error fetching habits: " + error.getMessage());
            }
        });
    }


    private void updateUIWithMonthlyProgress(HashMap<String, Float> dailyCompletionPercentages) {
        //TODO: make and pass data to design
        Log.d("Monthly Progress", "Updating UI with monthly progress: " + dailyCompletionPercentages.toString());

        // Example: Create a list of entries from 01-02-2025 to 28-02-2025 with the corresponding progress percentages
        for (int i = 0; i < dailyCompletionPercentages.size(); i++) {
            // Display or store the date and its completion percentage
            Log.d("Monthly Progress", "Date: " + (i + 1) + " Completion: " + dailyCompletionPercentages.get(i) + "%");
        }
    }

    private void updateUIWithMonthOverallProgress(int overallMonthProgress) {
        //TODO: make and pass data to design

        Log.d("Monthly Progress", "Updating UI with overall month progress: " + overallMonthProgress + "%");
    }

    private void updateUIWithPerfectDays(int perfectDaysCount) {

    }
}

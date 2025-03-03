package com.example.dayplanner.statistics;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dayplanner.R;
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

public class StatisticsActivity extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private String userId;
    private DatabaseReference habitsRef;

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

        fetchAndStoreHabitsForMonth("032025");
    }

    public void fetchAndStoreHabitsForMonth(String monthId) {
        Log.d("fetchAndStoreHabits", "Fetching habits for month: " + monthId);

        // Initialize Firebase database reference
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        userId = currentUser.getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();
        habitsRef = firebaseDatabase.getReference("users").child(userId).child("habits");

        habitsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LinkedHashMap<String, Float> dailyTotalPercentage = new LinkedHashMap<>();
                LinkedHashMap<String, Integer> dailyEntryCount = new LinkedHashMap<>();

                long currentDateMillis = System.currentTimeMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
                String currentDateStr = sdf.format(new Date(currentDateMillis)); // Get today's date in ddMMyyyy format

                // Iterate over habits
                for (DataSnapshot habitSnapshot : dataSnapshot.getChildren()) {
                    Habit habit = habitSnapshot.getValue(Habit.class);

                    if (habit != null) {
                        Log.d("fetchAndStoreHabits", "Processing habit: " + habit.getName());
                        String startDate = habit.getStartDate();  // Get habit's start date (ddMMyyyy)

                        // Iterate from startDate to currentDate
                        Calendar calendar = Calendar.getInstance();
                        try {
                            calendar.setTime(sdf.parse(startDate)); // Start from habit's start date
                        } catch (ParseException e) {
                            Log.e("fetchAndStoreHabits", "Invalid start date format: " + startDate);
                            continue;
                        }

                        while (true) {
                            String dateKey = sdf.format(calendar.getTime()); // Generate date in ddMMyyyy format

                            if (dateKey.substring(2, 8).equals(monthId)) { // Ensure it's within the selected month
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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("fetchAndStoreHabits", "Error fetching habits: " + databaseError.getMessage());
            }
        });
    }



    private String getNextDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(date));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            return sdf.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return date;
        }
    }

    private boolean isDateWithinRange(String date, String start, String end) {
        return date.compareTo(start) >= 0 && date.compareTo(end) <= 0;
    }

    private int getLastDayOfMonth(String monthYear) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMyyyy", Locale.getDefault());
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(sdf.parse(monthYear));
            return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        } catch (ParseException e) {
            e.printStackTrace();
            return 31; // Default to avoid errors
        }
    }

    private int getDayIndexFromDate(String date) {
        // Assuming the date format is ddMMyyyy (e.g., "01022025")
        int dayIndex = Integer.parseInt(date.substring(0, 2)) - 1;  // Extract day and use it as index (1st day -> index 0)
        Log.d("fetchAndStoreHabits", "Extracted day index from date " + date + ": " + dayIndex);
        return dayIndex;
        //return Integer.parseInt(date);
    }

    private int getNumberOfHabitsForDay(int dayIndex, String monthId) {
        // This method will count how many habits have entries for the given day (e.g., 01-02-2025)
        // Return the count for the specific day
        Log.d("fetchAndStoreHabits", "Counting habits for day " + (dayIndex + 1) + " in month " + monthId);
        return 1; // This is a stub. You need to implement the logic based on how your data is structured.
    }

    private void updateUIWithMonthlyProgress(HashMap<String, Float> dailyCompletionPercentages) {
        /** Use this method to update your UI with the list of daily completion percentages **/
        Log.d("Monthly Progress", "Updating UI with monthly progress: " + dailyCompletionPercentages.toString());

        // Example: Create a list of entries from 01-02-2025 to 28-02-2025 with the corresponding progress percentages
        for (int i = 0; i < dailyCompletionPercentages.size(); i++) {
            // Display or store the date and its completion percentage
            Log.d("Monthly Progress", "Date: " + (i + 1) + " Completion: " + dailyCompletionPercentages.get(i) + "%");
        }
    }
}

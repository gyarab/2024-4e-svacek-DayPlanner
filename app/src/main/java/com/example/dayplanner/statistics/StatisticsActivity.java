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

import java.util.ArrayList;
import java.util.List;

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

        // Use addValueEventListener to listen to the habits data
        habitsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This will store the completion percentages for each day
                List<Float> dailyCompletionPercentages = new ArrayList<>();

                // Loop through each habit in the database
                for (DataSnapshot habitSnapshot : dataSnapshot.getChildren()) {
                    // Assuming habit data is a custom model class
                    Habit habit = habitSnapshot.getValue(Habit.class);

                    if (habit != null) {
                        Log.d("fetchAndStoreHabits", "Found habit: " + habit.toString());
                        // Loop through habit entries (one entry per day)
                        for (String date : habit.getEntries().keySet()) {
                            // Extract month and year part from the date (e.g., "022025" for February 2025)
                            String entryMonthId = date.substring(2, 8);  // Extract MMYYYY part from ddMMyyyy date format
                            Log.d("fetchAndStoreHabits", "Entry month ID: " + entryMonthId);

                            // Check if the entry's date corresponds to the specified monthId
                            if (entryMonthId.equals(monthId)) {
                                Log.d("fetchAndStoreHabits", "Matching month ID: " + entryMonthId);
                                HabitEntry habitEntry = habit.getEntryForDate(date);
                                if (habitEntry != null) {
                                    // Calculate the completion percentage for this entry
                                    float percentage = (float) habitEntry.getProgress() / habitEntry.getEntryGoalValue() * 100;
                                    Log.d("fetchAndStoreHabits", "Found entry for date " + date + " with progress: "
                                            + habitEntry.getProgress() + " / " + habitEntry.getEntryGoalValue()
                                            + " = " + percentage + "%");

                                    // Store the completion percentage for that day
                                    int dayIndex = getDayIndexFromDate(date);  // Extract day from date (e.g., 01 from 01022025)
                                    Log.d("fetchAndStoreHabits", "Day index for date " + date + ": " + dayIndex);

                                    while (dailyCompletionPercentages.size() <= dayIndex) {
                                        dailyCompletionPercentages.add(0f);  // Add default value of 0% if not already initialized
                                        Log.d("fetchAndStoreHabits", "Initialized default value for day " + (dayIndex + 1));
                                    }
                                    // Add this entry's percentage to the corresponding day's total
                                    float existingPercentage = dailyCompletionPercentages.get(dayIndex);
                                    Log.d("fetchAndStoreHabits", "Existing percentage for day " + (dayIndex + 1) + ": " + existingPercentage);
                                    dailyCompletionPercentages.set(dayIndex, existingPercentage + percentage);
                                    Log.d("fetchAndStoreHabits", "Updated percentage for day " + (dayIndex + 1) + ": " + dailyCompletionPercentages.get(dayIndex) + "%");
                                }
                            }
                        }
                    }
                }

                // After looping through all habits and entries, calculate the average percentage for each day
                for (int i = 0; i < dailyCompletionPercentages.size(); i++) {
                    // Normalize the percentage (divide by number of habits with entries for this day)
                    int habitsForDay = getNumberOfHabitsForDay(i, monthId);
                    Log.d("fetchAndStoreHabits", "Normalizing day " + (i + 1) + " with " + habitsForDay + " habit(s) for the day.");

                    if (habitsForDay > 0) {
                        float normalizedPercentage = dailyCompletionPercentages.get(i) / habitsForDay;
                        Log.d("fetchAndStoreHabits", "Normalized percentage for day " + (i + 1) + ": " + normalizedPercentage + "%");
                        dailyCompletionPercentages.set(i, normalizedPercentage);
                    } else {
                        Log.d("fetchAndStoreHabits", "No habits for day " + (i + 1) + ", keeping percentage at 0%");
                    }
                }

                // Store or display the calculated daily percentages
                updateUIWithMonthlyProgress(dailyCompletionPercentages);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors
                Log.e("fetchAndStoreHabits", "Error fetching habits: " + databaseError.getMessage());
            }
        });
    }

    private int getDayIndexFromDate(String date) {
        // Assuming the date format is ddMMyyyy (e.g., "01022025")
        int dayIndex = Integer.parseInt(date.substring(0, 2)) - 1;  // Extract day and use it as index (1st day -> index 0)
        Log.d("fetchAndStoreHabits", "Extracted day index from date " + date + ": " + dayIndex);
        return dayIndex;
    }

    private int getNumberOfHabitsForDay(int dayIndex, String monthId) {
        // This method will count how many habits have entries for the given day (e.g., 01-02-2025)
        // Return the count for the specific day
        Log.d("fetchAndStoreHabits", "Counting habits for day " + (dayIndex + 1) + " in month " + monthId);
        return 1; // This is a stub. You need to implement the logic based on how your data is structured.
    }

    private void updateUIWithMonthlyProgress(List<Float> dailyCompletionPercentages) {
        /** Use this method to update your UI with the list of daily completion percentages **/
        Log.d("Monthly Progress", "Updating UI with monthly progress: " + dailyCompletionPercentages.toString());

        // Example: Create a list of entries from 01-02-2025 to 28-02-2025 with the corresponding progress percentages
        for (int i = 0; i < dailyCompletionPercentages.size(); i++) {
            // Display or store the date and its completion percentage
            Log.d("Monthly Progress", "Date: " + (i + 1) + " Completion: " + dailyCompletionPercentages.get(i) + "%");
        }
    }
}

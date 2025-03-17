package com.example.dayplanner.main.habits;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GoalManager {
    private final DatabaseReference habitsRef;

    public GoalManager() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        this.habitsRef = FirebaseDatabase.getInstance().getReference("users")
                .child(auth.getCurrentUser().getUid()).child("habits");
    }

    public void updateGoalValue(String habitId, int newGoalValue, String clickedDate) {
        DatabaseReference habitRef = habitsRef.child(habitId);

        habitRef.child("goalValue").setValue(newGoalValue)
                .addOnSuccessListener(aVoid -> Log.d("GoalManager", "Goal value updated"))
                .addOnFailureListener(e -> Log.e("GoalManager", "Failed to update goal value", e));

        updateFutureEntriesGoalValue(habitId, clickedDate, newGoalValue);
        logGoalHistory(habitId, newGoalValue, clickedDate);  // Pass clickedDate to log history
    }

    private void updateFutureEntriesGoalValue(String habitId, String clickedDate, int newGoalValue) {
        DatabaseReference entriesRef = habitsRef.child(habitId).child("entries");

        entriesRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                for (DataSnapshot entrySnapshot : snapshot.getChildren()) {
                    String entryDate = entrySnapshot.getKey();
                    if (entryDate.compareTo(clickedDate) >= 0) {
                        entrySnapshot.getRef().child("entryGoalValue").setValue(newGoalValue);
                    }
                }
                Log.d("GoalManager", "Future goal values updated successfully.");
            }
        }).addOnFailureListener(e -> Log.e("GoalManager", "Error updating future entries", e));
    }

    private void logGoalHistory(String habitId, int newGoalValue, String clickedDate) {
        DatabaseReference goalHistoryRef = habitsRef.child(habitId).child("goalHistory");

        goalHistoryRef.get().addOnSuccessListener(snapshot -> {
            boolean dateExists = false;

            if (snapshot.exists()) {
                for (DataSnapshot entrySnapshot : snapshot.getChildren()) {
                    String entryDate = entrySnapshot.getKey();

                    // ✅ If the same date exists, update it
                    if (entryDate.equals(clickedDate)) {
                        entrySnapshot.getRef().child("goalValue").setValue(newGoalValue);
                        dateExists = true;
                    }

                    // 🗑️ Delete future records if clicked date is in the past
                    if (clickedDate.compareTo(entryDate) < 0) {
                        entrySnapshot.getRef().removeValue();
                    }
                }
            }

            // ✅ If the date doesn't exist, add a new record
            if (!dateExists) {
                goalHistoryRef.child(clickedDate).child("goalValue").setValue(newGoalValue);
            }

            Log.d("GoalManager", "Goal history updated successfully.");
        }).addOnFailureListener(e -> Log.e("GoalManager", "Failed to update goal history", e));
    }
}

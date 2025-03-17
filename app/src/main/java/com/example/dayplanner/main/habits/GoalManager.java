package com.example.dayplanner.main.habits;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
        logGoalHistory(habitId, newGoalValue, clickedDate);
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
        Log.d("entry", "clickeddate " + clickedDate);

        goalHistoryRef.get().addOnSuccessListener(snapshot -> {
            boolean dateExists = false;

            if (snapshot.exists()) {
                Log.d("entry", "clickeddate " + clickedDate);
                for (DataSnapshot entrySnapshot : snapshot.getChildren()) {
                    String entryDate = entrySnapshot.getKey();

                    Log.d("entry", "entrydate " + entryDate);
                    if (entryDate.equals(clickedDate)) {
                        Log.d("entry", "date exists");
                        entrySnapshot.getRef().setValue(newGoalValue);
                        dateExists = true;
                    }

                    if (clickedDate.compareTo(entryDate) < 0) {
                        entrySnapshot.getRef().removeValue();
                    }
                }
            }

            if (!dateExists) {
                Log.d("entry", "date does not exist");
                goalHistoryRef.child(clickedDate).setValue(newGoalValue);
            }

            Log.d("GoalManager", "Goal history updated successfully.");
        }).addOnFailureListener(e -> Log.e("GoalManager", "Failed to update goal history", e));
    }
}

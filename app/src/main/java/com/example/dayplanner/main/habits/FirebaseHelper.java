package com.example.dayplanner.main.habits;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseHelper {

    private static FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private static FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    public static FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public static DatabaseReference getHabitsRef() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            return firebaseDatabase.getReference("users").child(currentUser.getUid()).child("habits");
        }
        return null;
    }

    public static String getUserId() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        }
        return null;
    }

    public static boolean isUserLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }
}

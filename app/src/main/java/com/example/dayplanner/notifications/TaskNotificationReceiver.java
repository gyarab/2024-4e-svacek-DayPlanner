package com.example.dayplanner.notifications;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.dayplanner.R;

public class TaskNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        boolean notificationsEnabled = preferences.getBoolean("notifications_enabled", true);

        if (!notificationsEnabled) {
            Log.d("notification", "Notifications are disabled, skipping...");
            return; // Exit if notifications are turned off
        }

        String taskTitle = intent.getStringExtra("taskTitle");
        Log.d("notification", "Notification triggered for task: " + taskTitle);

        createNotification(context, taskTitle);
    }


    private void createNotification(Context context, String taskTitle) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, TaskNotificationHelper.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Task Reminder")
                .setContentText("Your task: " + taskTitle + " starts in 5 minutes")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true); // Notification will disappear once tapped

        Log.d("notification", "Notification created for task: " + taskTitle);

        if (notificationManager != null) {
            notificationManager.notify(taskTitle.hashCode(), builder.build());
        }
    }
}


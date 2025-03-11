package com.example.dayplanner.notifications;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

public class TaskNotificationHelper {
    public static final String CHANNEL_ID = "task_notifications";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Task Reminders";
            String description = "Notifies users about upcoming tasks";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d("notification", "Notification channel created: " + CHANNEL_ID);
            }
        }
    }

    public static void scheduleNotification(Context context, String taskId, String taskTitle, long taskTimeMillis) {
        createNotificationChannel(context);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                context.startActivity(intent);
                return;
            }
        }

        Intent intent = new Intent(context, TaskNotificationReceiver.class);
        intent.putExtra("taskTitle", taskTitle);

        Log.d("notification", "Scheduling notification for taskId: " + taskId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId.hashCode(),  // Convert taskId to hash code for unique identifier
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Schedule notification 5 minutes before task
        long notificationTime = taskTimeMillis - (5 * 60 * 1000);

        Log.d("notification", "Notification scheduled for: " + notificationTime);

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationTime, pendingIntent);
        }
    }

    public static void cancelNotification(Context context, String taskId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TaskNotificationReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, taskId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}

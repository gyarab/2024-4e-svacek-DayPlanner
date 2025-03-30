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
    public static final String NOTIFICATION_TYPE = "notification_type";
    public static final int NOTIFICATION_TYPE_HOUR = 1;
    public static final int NOTIFICATION_TYPE_FIVE_MIN = 2;

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

        // Schedule one hour reminder
        scheduleHourReminder(context, alarmManager, taskId, taskTitle, taskTimeMillis);

        // Schedule five minute reminder
        scheduleFiveMinReminder(context, alarmManager, taskId, taskTitle, taskTimeMillis);
    }

    private static void scheduleHourReminder(Context context, AlarmManager alarmManager,
                                             String taskId, String taskTitle, long taskTimeMillis) {
        Intent hourIntent = new Intent(context, TaskNotificationReceiver.class);
        hourIntent.putExtra("taskTitle", taskTitle);
        hourIntent.putExtra("taskId", taskId);
        hourIntent.putExtra(NOTIFICATION_TYPE, NOTIFICATION_TYPE_HOUR);

        int hourRequestCode = taskId.hashCode() + NOTIFICATION_TYPE_HOUR;

        PendingIntent hourPendingIntent = PendingIntent.getBroadcast(
                context,
                hourRequestCode,
                hourIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long hourNotificationTime = taskTimeMillis - (60 * 60 * 1000);

        Log.d("notification", "Hour notification scheduled for taskId: " + taskId + " at: " + hourNotificationTime);

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, hourNotificationTime, hourPendingIntent);
        }
    }

    private static void scheduleFiveMinReminder(Context context, AlarmManager alarmManager,
                                                String taskId, String taskTitle, long taskTimeMillis) {
        Intent fiveMinIntent = new Intent(context, TaskNotificationReceiver.class);
        fiveMinIntent.putExtra("taskTitle", taskTitle);
        fiveMinIntent.putExtra("taskId", taskId);
        fiveMinIntent.putExtra(NOTIFICATION_TYPE, NOTIFICATION_TYPE_FIVE_MIN);

        int fiveMinRequestCode = taskId.hashCode() + NOTIFICATION_TYPE_FIVE_MIN;

        PendingIntent fiveMinPendingIntent = PendingIntent.getBroadcast(
                context,
                fiveMinRequestCode,
                fiveMinIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Schedule notification 5 minutes before task
        long fiveMinNotificationTime = taskTimeMillis - (5 * 60 * 1000);

        Log.d("notification", "Five minute notification scheduled for taskId: " + taskId + " at: " + fiveMinNotificationTime);

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, fiveMinNotificationTime, fiveMinPendingIntent);
        }
    }

    public static void cancelNotification(Context context, String taskId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Cancel hour notification
        Intent hourIntent = new Intent(context, TaskNotificationReceiver.class);
        int hourRequestCode = taskId.hashCode() + NOTIFICATION_TYPE_HOUR;
        PendingIntent hourPendingIntent = PendingIntent.getBroadcast(
                context, hourRequestCode, hourIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Cancel five minute notification
        Intent fiveMinIntent = new Intent(context, TaskNotificationReceiver.class);
        int fiveMinRequestCode = taskId.hashCode() + NOTIFICATION_TYPE_FIVE_MIN;
        PendingIntent fiveMinPendingIntent = PendingIntent.getBroadcast(
                context, fiveMinRequestCode, fiveMinIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(hourPendingIntent);
            alarmManager.cancel(fiveMinPendingIntent);
            Log.d("notification", "Notifications canceled for taskId: " + taskId);
        }
    }
}
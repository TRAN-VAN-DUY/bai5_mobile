package com.example.app_xemphim.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class ReminderScheduler {

    public static void scheduleShowReminder(Context context, int requestCode,
            long triggerAtMillis,
            String movieTitle,
            String theater,
            String showtime) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("movieTitle", movieTitle);
        intent.putExtra("theater", theater);
        intent.putExtra("showtime", showtime);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }
}

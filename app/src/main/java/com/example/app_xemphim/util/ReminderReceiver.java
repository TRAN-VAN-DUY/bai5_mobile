package com.example.app_xemphim.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String movieTitle = intent.getStringExtra("movieTitle");
        String showtime = intent.getStringExtra("showtime");
        String theater = intent.getStringExtra("theater");

        String title = "Sap den gio chieu";
        String message = movieTitle + " - " + theater + " luc " + showtime;

        NotificationHelper.createChannel(context);
        NotificationHelper.showNotification(context, (int) System.currentTimeMillis(), title, message);
    }
}

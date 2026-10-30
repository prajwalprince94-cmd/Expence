package com.knowspends.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.knowspends.app.util.NotificationHelper
import com.knowspends.app.util.ReminderScheduler

class DailyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.showDailyReminderNotification(context)

        // Reschedule for next day
        val timeString = intent.getStringExtra("reminder_time") ?: "20:00"
        ReminderScheduler.scheduleDailyReminder(context, timeString)
    }
}

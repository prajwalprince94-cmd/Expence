package com.knowspends.app.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.knowspends.app.MainActivity
import com.knowspends.app.R

object NotificationHelper {

    const val CHANNEL_REMINDERS_ID = "knowspends_reminders"
    const val CHANNEL_BUDGETS_ID = "knowspends_budgets"
    const val DAILY_REMINDER_NOTIFICATION_ID = 1001
    const val BUDGET_ALERT_NOTIFICATION_ID = 1002

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS_ID,
                "Daily Expense Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminders to record expenses and keep your ledger up to date"
                enableLights(true)
            }

            val budgetChannel = NotificationChannel(
                CHANNEL_BUDGETS_ID,
                "Budget Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when monthly category budgets approach or exceed limits"
                enableLights(true)
                enableVibration(true)
            }

            val smsChannel = NotificationChannel(
                CHANNEL_SMS_TRANSACTIONS_ID,
                "SMS & Bank Transactions",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Live notifications for auto-detected bank and UPI spendings"
                enableLights(true)
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(listOf(reminderChannel, budgetChannel, smsChannel))
        }
    }

    fun showDailyReminderNotification(context: Context) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("initial_route", "add_transaction")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_REMINDERS_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("KnowSpends Daily Review")
            .setContentText("Take a moment to record today's expenses and keep your balance verified.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Take a moment to record today's expenses and keep your balance verified. Tap to quickly add a transaction.")
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(DAILY_REMINDER_NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS permission not granted
        }
    }

    fun showBudgetAlertNotification(context: Context, categoryName: String, percentSpent: Int) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_BUDGETS_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Budget Alert: $categoryName")
            .setContentText("You have spent $percentSpent% of your budget for $categoryName.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(BUDGET_ALERT_NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS permission not granted
        }
    }

    const val CHANNEL_SMS_TRANSACTIONS_ID = "knowspends_sms_transactions"
    const val SMS_TRANSACTION_NOTIFICATION_ID = 1003

    fun showSmsTransactionNotification(context: Context, title: String, message: String, route: String = "inbox") {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("ROUTE", route)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_SMS_TRANSACTIONS_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                (System.currentTimeMillis() % 100000).toInt(),
                notification
            )
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS permission not granted
        }
    }
}

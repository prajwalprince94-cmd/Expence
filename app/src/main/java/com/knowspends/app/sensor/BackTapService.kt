package com.knowspends.app.sensor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.knowspends.app.MainActivity
import com.knowspends.app.R
import com.knowspends.app.domain.repository.UserPreferencesRepository
import com.knowspends.app.util.VyxelHaptics
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BackTapService : Service() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var backTapDetector: BackTapDetector? = null

    companion object {
        private const val TAG = "BackTapService"
        const val CHANNEL_ID = "knowspends_back_tap_service"
        const val HEADS_UP_CHANNEL_ID = "knowspends_back_tap_headsup"
        const val NOTIFICATION_ID = 9081
        const val HEADS_UP_NOTIFICATION_ID = 9082

        const val ACTION_START = "com.knowspends.app.action.START_BACK_TAP"
        const val ACTION_STOP = "com.knowspends.app.action.STOP_BACK_TAP"

        fun start(context: Context) {
            val intent = Intent(context, BackTapService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, BackTapService::class.java).apply {
                action = ACTION_STOP
            }
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createChannels()
        startForeground(NOTIFICATION_ID, buildForegroundNotification())

        backTapDetector = BackTapDetector(this) {
            onTapDetected()
        }

        serviceScope.launch {
            val prefs = userPreferencesRepository.userPreferencesFlow.first()
            updateDetectorSensitivity(prefs.backTapSensitivity)
            backTapDetector?.start(SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                serviceScope.launch {
                    val prefs = userPreferencesRepository.userPreferencesFlow.first()
                    if (!prefs.backTapEnabled) {
                        stopSelf()
                    } else {
                        updateDetectorSensitivity(prefs.backTapSensitivity)
                        backTapDetector?.start(SensorManager.SENSOR_DELAY_NORMAL)
                    }
                }
            }
        }
        return START_STICKY
    }

    private fun onTapDetected() {
        Log.d(TAG, "Hardware Back-Tap detected in BackTapService!")
        VyxelHaptics.heavy(this)

        val openIslandIntent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("knowspends://dynamic-island")
            putExtra("TRIGGER_DYNAMIC_ISLAND", true)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        // 1. Attempt to launch MainActivity directly
        try {
            startActivity(openIslandIntent)
        } catch (e: Exception) {
            Log.w(TAG, "Direct activity launch from background restricted: ${e.message}")
        }

        // 2. Post high-priority Heads-up notification to guarantee popup access even if OS blocks background start
        showHeadsUpPopupNotification(openIslandIntent)
    }

    private fun showHeadsUpPopupNotification(targetIntent: Intent) {
        val pendingIntent = PendingIntent.getActivity(
            this,
            HEADS_UP_NOTIFICATION_ID,
            targetIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val headsUpNotification = NotificationCompat.Builder(this, HEADS_UP_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⚡ Quick Add Expense")
            .setContentText("Tap to open Dynamic Island and log your spending")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setTimeoutAfter(8000L) // Auto dismiss after 8s if ignored
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(HEADS_UP_NOTIFICATION_ID, headsUpNotification)
    }

    private fun updateDetectorSensitivity(sensitivity: String) {
        backTapDetector?.sensitivityThreshold = when (sensitivity) {
            "high" -> BackTapDetector.SENSITIVITY_HIGH
            "low" -> BackTapDetector.SENSITIVITY_LOW
            else -> BackTapDetector.SENSITIVITY_NORMAL
        }
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Tap to Add Background Detection",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Monitors device back-tap sensor for quick expense entry"
                setShowBadge(false)
            }

            val headsUpChannel = NotificationChannel(
                HEADS_UP_CHANNEL_ID,
                "Quick Add Expense Island",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Heads-up alert to log spending on back-tap gesture"
                enableLights(true)
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(listOf(serviceChannel, headsUpChannel))
        }
    }

    private fun buildForegroundNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Tap to Add is Active")
            .setContentText("Double-tap the back of your phone to quickly log spending")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        backTapDetector?.stop()
        serviceJob.cancel()
        Log.d(TAG, "BackTapService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

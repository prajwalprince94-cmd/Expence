package com.knowspends.app.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import android.util.Log
import kotlin.math.abs

/**
 * Hardware accelerometer-based Back-Tap Detector.
 *
 * Physics & Detection Dynamics:
 * - Detects physical double-taps on the back of the device by analyzing the transient
 *   jerk (derivative of acceleration) along the Z-axis (normal to the device's back plate).
 * - Filters out continuous motion / walking / pocket shakes via high-pass thresholding.
 * - Confirms a double-tap when two sharp spike peaks occur within [minWindowMs] .. [maxWindowMs].
 * - Debounces for [debounceMs] following a confirmed gesture to prevent multi-firing.
 */
class BackTapDetector(
    context: Context,
    private val onBackTapDetected: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var isListening = false
    private var lastZ = 0f
    private var lastTapTimestamp = 0L
    private var lastConfirmedGestureTimestamp = 0L

    // Sensitivity thresholds (m/s^2 spike)
    var sensitivityThreshold: Float = SENSITIVITY_NORMAL

    companion object {
        private const val TAG = "BackTapDetector"
        const val SENSITIVITY_HIGH = 14.0f
        const val SENSITIVITY_NORMAL = 19.0f
        const val SENSITIVITY_LOW = 25.0f

        private const val MIN_TAP_INTERVAL_MS = 100L
        private const val MAX_TAP_INTERVAL_MS = 480L
        private const val DEBOUNCE_GESTURE_MS = 850L
    }

    fun start() {
        if (isListening || accelerometer == null || sensorManager == null) return
        // Use SENSOR_DELAY_GAME (~20ms / 50Hz) for responsive transient peak capture without battery drain
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        isListening = true
        lastZ = 0f
        lastTapTimestamp = 0L
        Log.d(TAG, "BackTapDetector started with sensitivity threshold: $sensitivityThreshold")
    }

    fun stop() {
        if (!isListening || sensorManager == null) return
        sensorManager.unregisterListener(this)
        isListening = false
        Log.d(TAG, "BackTapDetector stopped")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val currentZ = event.values[2]
        val deltaZ = abs(currentZ - lastZ)
        lastZ = currentZ

        val now = SystemClock.elapsedRealtime()

        // Ignore events within debounce window of last successful trigger
        if (now - lastConfirmedGestureTimestamp < DEBOUNCE_GESTURE_MS) {
            return
        }

        // Check if transient acceleration along Z-axis exceeds impact threshold
        if (deltaZ >= sensitivityThreshold) {
            val timeSinceLastTap = now - lastTapTimestamp

            if (timeSinceLastTap in MIN_TAP_INTERVAL_MS..MAX_TAP_INTERVAL_MS) {
                // Second tap confirmed within valid double-tap window!
                lastConfirmedGestureTimestamp = now
                lastTapTimestamp = 0L
                Log.d(TAG, "Back-tap double-tap detected! Delta: $deltaZ, Interval: ${timeSinceLastTap}ms")
                onBackTapDetected()
            } else {
                // First tap recorded, start waiting for second tap
                lastTapTimestamp = now
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}

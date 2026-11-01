package com.knowspends.app.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import android.util.Log
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * High-precision physical Back-Tap Detector.
 *
 * Physics & Detection Dynamics:
 * - Uses Sensor.TYPE_LINEAR_ACCELERATION when available (gravity-free user acceleration),
 *   falling back to Sensor.TYPE_ACCELEROMETER with a dynamic high-pass filter.
 * - Samples at SENSOR_DELAY_FASTEST (100Hz-200Hz) to reliably capture transient 10ms
 *   finger-impact shockwaves on the phone's back housing.
 * - Calculates 3D jerk (derivative of acceleration) with primary weighting along the
 *   Z-axis (normal to the phone's rear plate).
 * - Calibrated thresholds: High (2.4 m/s²), Normal (3.6 m/s²), Low (5.2 m/s²).
 * - Double-tap temporal gate: 80ms to 500ms between tap 1 and tap 2.
 * - Debounces for 900ms after confirmed gesture to prevent multi-firing.
 */
class BackTapDetector(
    context: Context,
    private val onBackTapDetected: () -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    // Prefer Linear Acceleration (excludes 1G gravity), fallback to standard Accelerometer
    private val sensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val isLinearSensor = sensor?.type == Sensor.TYPE_LINEAR_ACCELERATION

    private var isListening = false
    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

    // High-pass filter gravity estimation for raw accelerometer fallback
    private var gravityX = 0f
    private var gravityY = 0f
    private var gravityZ = 9.8f

    private var startTime = 0L
    private var lastTapTimestamp = 0L
    private var lastConfirmedGestureTimestamp = 0L

    var sensitivityThreshold: Float = SENSITIVITY_NORMAL

    companion object {
        private const val TAG = "BackTapDetector"

        // Calibrated realistic finger tap impact thresholds (m/s² jerk)
        const val SENSITIVITY_HIGH = 2.4f    // Light finger taps or phone with case
        const val SENSITIVITY_NORMAL = 3.6f  // Standard firm double-tap
        const val SENSITIVITY_LOW = 5.2f     // Deliberate strong double-tap

        private const val MIN_TAP_INTERVAL_MS = 80L
        private const val MAX_TAP_INTERVAL_MS = 500L
        private const val DEBOUNCE_GESTURE_MS = 900L
        private const val STARTUP_IGNORE_WINDOW_MS = 350L
    }

    fun start() {
        if (isListening || sensor == null || sensorManager == null) return
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST)
        isListening = true
        startTime = SystemClock.elapsedRealtime()
        lastX = 0f
        lastY = 0f
        lastZ = 0f
        lastTapTimestamp = 0L
        Log.d(TAG, "BackTapDetector started. Sensor: ${sensor.name}, Linear: $isLinearSensor, Threshold: $sensitivityThreshold")
    }

    fun stop() {
        if (!isListening || sensorManager == null) return
        sensorManager.unregisterListener(this)
        isListening = false
        Log.d(TAG, "BackTapDetector stopped")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || (event.sensor.type != Sensor.TYPE_LINEAR_ACCELERATION && event.sensor.type != Sensor.TYPE_ACCELEROMETER)) return

        val now = SystemClock.elapsedRealtime()

        // Settle sensor after start to avoid spurious trigger
        if (now - startTime < STARTUP_IGNORE_WINDOW_MS) {
            lastX = event.values[0]
            lastY = event.values[1]
            lastZ = event.values[2]
            return
        }

        // Ignore events within debounce window
        if (now - lastConfirmedGestureTimestamp < DEBOUNCE_GESTURE_MS) {
            return
        }

        val linX: Float
        val linY: Float
        val linZ: Float

        if (isLinearSensor) {
            linX = event.values[0]
            linY = event.values[1]
            linZ = event.values[2]
        } else {
            // Apply high-pass filter to remove constant gravity vector
            val alpha = 0.85f
            gravityX = alpha * gravityX + (1 - alpha) * event.values[0]
            gravityY = alpha * gravityY + (1 - alpha) * event.values[1]
            gravityZ = alpha * gravityZ + (1 - alpha) * event.values[2]

            linX = event.values[0] - gravityX
            linY = event.values[1] - gravityY
            linZ = event.values[2] - gravityZ
        }

        // Calculate instantaneous jerk along 3 axes
        val dx = linX - lastX
        val dy = linY - lastY
        val dz = linZ - lastZ

        lastX = linX
        lastY = linY
        lastZ = linZ

        // A finger tap on the rear plate produces a sharp shock along the Z-axis
        // with minor lateral torsion (X, Y)
        val vectorImpulse = sqrt(dx * dx + dy * dy + dz * dz)
        val shock = abs(dz) * 0.7f + vectorImpulse * 0.3f

        if (shock >= sensitivityThreshold) {
            val timeSinceLastTap = now - lastTapTimestamp

            if (timeSinceLastTap in MIN_TAP_INTERVAL_MS..MAX_TAP_INTERVAL_MS) {
                // Confirmed double-tap!
                lastConfirmedGestureTimestamp = now
                lastTapTimestamp = 0L
                Log.d(TAG, "Double-tap detected! Shock: $shock, Interval: ${timeSinceLastTap}ms")
                onBackTapDetected()
            } else {
                // First tap recorded, begin window for second tap
                lastTapTimestamp = now
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}

package com.knowspends.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.knowspends.app.ui.theme.KnowSpendsMotion
import com.knowspends.app.ui.theme.LocalMotionIntensity
import kotlin.math.PI
import kotlin.math.sin

/**
 * Material 3 Expressive Wavy Progress Indicator for budget health tracking.
 *
 * Physics & Dynamics per AGENTS.md §4.5:
 * - Amplitude and frequency dynamically increase as spending approaches 100%.
 * - Honors [LocalMotionIntensity] to disable phase oscillation for users with motion sensitivity.
 */
@Composable
fun ExpressiveWavyProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    strokeWidth: Dp = 6.dp,
    height: Dp = 16.dp,
    animateProgress: Boolean = true
) {
    val motionIntensity = LocalMotionIntensity.current
    val density = LocalDensity.current

    // Smoothly animate progress updates with snappy spring
    val targetProgress = progress.coerceIn(0f, 1.5f)
    val animatedProgress by if (animateProgress && motionIntensity > 0f) {
        animateFloatAsState(
            targetValue = targetProgress,
            animationSpec = KnowSpendsMotion.snappy(),
            label = "wavyProgress"
        )
    } else {
        animateFloatAsState(targetValue = targetProgress, label = "wavyProgress")
    }

    // Dynamic wave parameters based on budget strain
    val strain = (animatedProgress.coerceIn(0f, 1f))
    // Calm under 60%, escalating up to 100%+
    val amplitudeDp = when {
        animatedProgress < 0.6f -> 1.5.dp
        animatedProgress < 0.85f -> 2.5.dp
        animatedProgress <= 1.0f -> 4.0.dp
        else -> 5.0.dp // Overbudget agitation
    }

    val wavelengthDp = when {
        animatedProgress < 0.6f -> 36.dp
        animatedProgress < 0.85f -> 28.dp
        else -> 20.dp // High frequency warning ripple
    }

    // Continuous wave oscillation (freeze if motion sensitivity is requested)
    val transition = rememberInfiniteTransition(label = "wavyPhaseTransition")
    val phase by if (motionIntensity > 0f) {
        val duration = if (animatedProgress >= 0.85f) 1200 else 2400
        transition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "wavyPhase"
        )
    } else {
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(tween(1000)),
            label = "staticPhase"
        )
    }

    val strokeWidthPx = with(density) { strokeWidth.toPx() }
    val amplitudePx = with(density) { amplitudeDp.toPx() }
    val wavelengthPx = with(density) { wavelengthDp.toPx() }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val w = size.width
        val centerY = size.height / 2f
        if (w <= 0f) return@Canvas

        // 1. Draw straight background track
        drawLine(
            color = trackColor,
            start = Offset(0f, centerY),
            end = Offset(w, centerY),
            strokeWidth = strokeWidthPx,
            cap = StrokeCap.Round
        )

        val progressWidth = (w * animatedProgress.coerceIn(0f, 1f)).coerceAtLeast(0f)
        if (progressWidth <= 1f) return@Canvas

        // 2. Draw active wavy progress path
        val path = Path()
        path.moveTo(0f, centerY)

        val stepPx = 3f
        var x = 0f
        while (x <= progressWidth) {
            val wave = amplitudePx * sin(2 * PI * (x / wavelengthPx) + phase)
            // Gently taper amplitude near start and end to meet track smoothly
            val taper = when {
                x < 16f -> x / 16f
                x > progressWidth - 16f -> (progressWidth - x) / 16f
                else -> 1f
            }.coerceIn(0f, 1f)

            val y = centerY + (wave.toFloat() * taper)
            path.lineTo(x, y)
            x += stepPx
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokeWidthPx,
                cap = StrokeCap.Round
            )
        )
    }
}

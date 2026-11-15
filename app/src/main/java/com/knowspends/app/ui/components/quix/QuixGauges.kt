package com.knowspends.app.ui.components.quix

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knowspends.app.ui.theme.QuixTokens
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Quix Studio Radial Gauges & Progress Indicators
 * Source: https://www.quixstudio.net/free/components
 *
 * Provides Apple/Quix-grade radial meters:
 * - [QuixArcGauge]: 240-degree radial gauge with tick marks and center percentage readout.
 * - [QuixSegmentDonut]: Segmented circular ring for Category Budget health tracking.
 * - [QuixRadialBarPercent]: Compact circular percentage indicator for cards and lists.
 */

@Composable
fun QuixArcGauge(
    progressRatio: Float,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp,
    strokeWidth: Dp = 12.dp,
    centerValue: String? = null,
    centerSubtitle: String? = null,
    statusColorOverride: Color? = null,
    showTicks: Boolean = true
) {
    val safeRatio = if (progressRatio.isNaN()) 0f else progressRatio.coerceAtLeast(0f)
    val clampedRatio = safeRatio
    val animatedRatio by animateFloatAsState(
        targetValue = clampedRatio.coerceAtMost(1.0f),
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "gauge_progress"
    )

    val targetColor = statusColorOverride ?: when {
        clampedRatio > 1.0f -> QuixTokens.CoralWarning
        clampedRatio >= 0.80f -> QuixTokens.AmberWarning
        else -> QuixTokens.NeonLime
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 400),
        label = "gauge_color"
    )

    val percentInt = (clampedRatio * 100f).toInt()
    val displayValue = centerValue ?: "$percentInt%"

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val diameter = (this.size.minDimension - strokePx - 16f).coerceAtLeast(0f)
            val radius = diameter / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            // 240 degree gauge (from 150 deg to 390 deg / 30 deg past bottom)
            val startAngle = 150f
            val totalSweep = 240f

            // 1. Tick marks along the outer perimeter
            if (showTicks) {
                val tickCount = 25
                val tickOuterRadius = radius + strokePx * 0.8f
                val tickInnerRadius = radius + strokePx * 0.35f
                for (t in 0..tickCount) {
                    val tickAngleDeg = startAngle + (t.toFloat() / tickCount) * totalSweep
                    val tickAngleRad = (tickAngleDeg * PI / 180.0).toFloat()
                    val p1 = Offset(
                        x = center.x + tickInnerRadius * cos(tickAngleRad),
                        y = center.y + tickInnerRadius * sin(tickAngleRad)
                    )
                    val p2 = Offset(
                        x = center.x + tickOuterRadius * cos(tickAngleRad),
                        y = center.y + tickOuterRadius * sin(tickAngleRad)
                    )
                    val isPassed = (t.toFloat() / tickCount) <= animatedRatio
                    drawLine(
                        color = if (isPassed) animatedColor.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.08f),
                        start = p1,
                        end = p2,
                        strokeWidth = 2f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // 2. Background Track Arc
            drawArc(
                color = Color.White.copy(alpha = 0.08f),
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(diameter, diameter),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // 3. Active Progress Arc
            if (animatedRatio > 0.005f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to animatedColor.copy(alpha = 0.6f),
                        1.0f to animatedColor,
                        center = center
                    ),
                    startAngle = startAngle,
                    sweepAngle = totalSweep * animatedRatio,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                // 4. Glowing Head Dot
                val headAngleDeg = startAngle + (totalSweep * animatedRatio)
                val headAngleRad = (headAngleDeg * PI / 180.0).toFloat()
                val headOffset = Offset(
                    x = center.x + radius * cos(headAngleRad),
                    y = center.y + radius * sin(headAngleRad)
                )

                drawCircle(
                    color = animatedColor.copy(alpha = 0.35f),
                    radius = strokePx * 1.2f,
                    center = headOffset
                )
                drawCircle(
                    color = Color.White,
                    radius = strokePx * 0.45f,
                    center = headOffset
                )
            }
        }

        // Center Readout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = displayValue,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = (-0.5).sp
                ),
                color = animatedColor,
                textAlign = TextAlign.Center
            )

            if (centerSubtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = centerSubtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp
                    ),
                    color = QuixTokens.TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun QuixSegmentDonut(
    progressRatio: Float,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    strokeWidth: Dp = 7.dp,
    segmentCount: Int = 10,
    accentColor: Color? = null
) {
    val safeRatio = if (progressRatio.isNaN()) 0f else progressRatio.coerceAtLeast(0f)
    val safeSegmentCount = segmentCount.coerceAtLeast(1)
    val clampedRatio = safeRatio
    val animatedRatio by animateFloatAsState(
        targetValue = clampedRatio.coerceAtMost(1f),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "donut_progress"
    )

    val activeColor = accentColor ?: when {
        clampedRatio > 1.0f -> QuixTokens.CoralWarning
        clampedRatio >= 0.80f -> QuixTokens.AmberWarning
        else -> QuixTokens.NeonLime
    }

    Canvas(modifier = modifier.size(size)) {
        val strokePx = strokeWidth.toPx()
        val diameter = (this.size.minDimension - strokePx).coerceAtLeast(0f)
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val arcSpan = (360f / safeSegmentCount) - 6f

        for (i in 0 until safeSegmentCount) {
            val startAngle = (i.toFloat() / safeSegmentCount) * 360f - 90f + 3f
            val segmentThreshold = (i + 1).toFloat() / safeSegmentCount
            val isFilled = animatedRatio >= segmentThreshold

            val color = if (isFilled) {
                activeColor
            } else {
                Color.White.copy(alpha = 0.08f)
            }

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = arcSpan,
                useCenter = false,
                topLeft = Offset(center.x - diameter / 2f, center.y - diameter / 2f),
                size = Size(diameter, diameter),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun QuixRadialBarPercent(
    progressRatio: Float,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    strokeWidth: Dp = 4.5.dp,
    color: Color = QuixTokens.NeonLime,
    showText: Boolean = true
) {
    val safeRatio = if (progressRatio.isNaN()) 0f else progressRatio.coerceIn(0f, 1f)
    val animatedRatio by animateFloatAsState(
        targetValue = safeRatio,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "radial_bar"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val diameter = (this.size.minDimension - strokePx).coerceAtLeast(0f)
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            // Background ring
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = diameter / 2f,
                center = center,
                style = Stroke(width = strokePx)
            )

            // Progress arc
            if (animatedRatio > 0.01f) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedRatio,
                    useCenter = false,
                    topLeft = Offset(center.x - diameter / 2f, center.y - diameter / 2f),
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        if (showText && size >= 32.dp) {
            Text(
                text = "${(safeRatio * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                ),
                color = color
            )
        }
    }
}

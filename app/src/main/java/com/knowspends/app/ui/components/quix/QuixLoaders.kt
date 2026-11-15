package com.knowspends.app.ui.components.quix

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knowspends.app.ui.theme.QuixTokens
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Quix Studio Animated Loaders
 * Source: https://www.quixstudio.net/free/components
 *
 * Implements the signature canvas-based animated loaders:
 * - [QuixCometLoader]: Orbiting circular comet arc with fading particle trail and glowing head.
 * - [QuixSpokesLoader]: 12-spoke radial loader with trailing alpha sweep.
 * - [QuixSegmentDonutLoader]: 8-segment circular ring with wave highlight.
 * - [QuixBarGlowLoader]: Sleek horizontal indeterminate bar with glowing neon head.
 */

@Composable
fun QuixCometLoader(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    accentColor: Color = QuixTokens.NeonLime,
    trailColor: Color = QuixTokens.NeonLime.copy(alpha = 0.15f),
    strokeWidth: Dp = 4.dp,
    label: String? = "LOADING",
    durationMillis: Int = 1200
) {
    val transition = rememberInfiniteTransition(label = "quix_comet")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "comet_rot"
    )

    val pulse by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis / 2, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "comet_pulse"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val diameter = this.size.minDimension - strokePx
            val radius = diameter / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)

            // 1. Faint circular track
            drawCircle(
                color = trailColor.copy(alpha = 0.1f),
                radius = radius,
                center = center,
                style = Stroke(width = strokePx * 0.75f)
            )

            // 2. Comet sweeping arc with rotation
            rotate(rotation, pivot = center) {
                // Sweeping tail arc
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to Color.Transparent,
                        0.65f to trailColor,
                        1.0f to accentColor,
                        center = center
                    ),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                // Glowing comet head dot
                val headAngleRad = (0.0).toFloat()
                val headOffset = Offset(
                    x = center.x + radius * cos(headAngleRad),
                    y = center.y + radius * sin(headAngleRad)
                )

                // Outer glow
                drawCircle(
                    color = accentColor.copy(alpha = 0.4f),
                    radius = strokePx * 1.8f,
                    center = headOffset
                )
                // Core bright dot
                drawCircle(
                    color = Color.White,
                    radius = strokePx * 0.9f,
                    center = headOffset
                )
            }
        }

        if (label != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    fontFamily = FontFamily.Monospace
                ),
                color = accentColor.copy(alpha = pulse)
            )
        }
    }
}

@Composable
fun QuixSpokesLoader(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    accentColor: Color = QuixTokens.NeonLime,
    spokeCount: Int = 12,
    durationMillis: Int = 1000
) {
    val safeSpokeCount = spokeCount.coerceAtLeast(1)
    val transition = rememberInfiniteTransition(label = "quix_spokes")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = safeSpokeCount.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spoke_phase"
    )

    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val outerRadius = (this.size.minDimension / 2f) * 0.88f
        val innerRadius = outerRadius * 0.55f
        val strokePx = (size.toPx() / 14f).coerceAtLeast(3f)

        for (i in 0 until safeSpokeCount) {
            val angleDeg = (i.toFloat() / safeSpokeCount.toFloat()) * 360f - 90f
            val angleRad = (angleDeg * PI / 180.0).toFloat()

            // Calculate trailing alpha relative to current phase
            val diff = ((phase - i + safeSpokeCount) % safeSpokeCount) / safeSpokeCount.toFloat()
            val alpha = (1.0f - diff).coerceIn(0.12f, 1.0f)

            val p1 = Offset(
                x = center.x + innerRadius * cos(angleRad),
                y = center.y + innerRadius * sin(angleRad)
            )
            val p2 = Offset(
                x = center.x + outerRadius * cos(angleRad),
                y = center.y + outerRadius * sin(angleRad)
            )

            drawLine(
                color = accentColor.copy(alpha = alpha),
                start = p1,
                end = p2,
                strokeWidth = strokePx,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun QuixSegmentDonutLoader(
    modifier: Modifier = Modifier,
    size: Dp = 60.dp,
    segmentCount: Int = 8,
    accentColor: Color = QuixTokens.NeonLime,
    durationMillis: Int = 1100
) {
    val transition = rememberInfiniteTransition(label = "quix_segment_donut")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "segment_progress"
    )

    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val strokePx = (size.toPx() / 6f).coerceAtLeast(6f)
        val diameter = this.size.minDimension - strokePx
        val arcSpan = (360f / segmentCount) - 8f

        for (i in 0 until segmentCount) {
            val startAngle = (i.toFloat() / segmentCount) * 360f - 90f + 4f
            val segProgress = ((progress * segmentCount - i + segmentCount) % segmentCount) / segmentCount
            val alpha = (1.0f - segProgress).coerceIn(0.15f, 1.0f)

            drawArc(
                color = accentColor.copy(alpha = alpha),
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
fun QuixBarGlowLoader(
    modifier: Modifier = Modifier,
    height: Dp = 4.dp,
    accentColor: Color = QuixTokens.NeonLime,
    trackColor: Color = QuixTokens.ObsidianCardElevated,
    durationMillis: Int = 1400
) {
    val transition = rememberInfiniteTransition(label = "quix_bar_glow")
    val translate by transition.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bar_translate"
    )

    Canvas(
        modifier = modifier
            .height(height)
            .clipToBounds()
    ) {
        val w = this.size.width
        val h = this.size.height
        val cornerRadius = h / 2f

        // Track
        drawRoundRect(
            color = trackColor,
            size = Size(w, h),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
        )

        // Sweeping glowing bar
        val barWidth = w * 0.45f
        val startX = translate * w - (barWidth / 2f)

        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    accentColor.copy(alpha = 0.5f),
                    accentColor,
                    Color.White
                ),
                startX = startX,
                endX = startX + barWidth
            ),
            topLeft = Offset(startX, 0f),
            size = Size(barWidth, h),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
        )
    }
}

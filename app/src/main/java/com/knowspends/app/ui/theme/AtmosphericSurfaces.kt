package com.knowspends.app.ui.theme

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Atmospheric Aurora Glow shader effect adapted from Vyxel Apps.
 * Renders 3 drifting radial color orbs that slowly oscillate behind content.
 */
@Composable
fun Modifier.auroraGlow(
    color1: Color = MaterialTheme.colorScheme.primary,
    color2: Color = MaterialTheme.colorScheme.secondary,
    color3: Color = MaterialTheme.colorScheme.tertiary,
    alpha: Float = 0.22f
): Modifier {
    val motionIntensity = LocalMotionIntensity.current
    val phase = if (motionIntensity <= 0f) {
        0f
    } else {
        val transition = rememberInfiniteTransition(label = "auroraGlow")
        val animatedPhase by transition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * Math.PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(12000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "auroraPhase"
        )
        animatedPhase
    }

    return this.drawBehind {
        val w = size.width
        val h = size.height

        // Orb 1: moves in oval in top-left quadrant
        val center1 = Offset(
            x = w * (0.25f + 0.15f * sin(phase)),
            y = h * (0.35f + 0.15f * cos(phase))
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color1.copy(alpha = alpha), Color.Transparent),
                center = center1,
                radius = w * 0.6f
            ),
            center = center1,
            radius = w * 0.6f
        )

        // Orb 2: moves out-of-phase in top-right quadrant
        val center2 = Offset(
            x = w * (0.75f - 0.15f * cos(phase * 0.8f)),
            y = h * (0.30f + 0.20f * sin(phase * 0.8f))
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color2.copy(alpha = alpha), Color.Transparent),
                center = center2,
                radius = w * 0.55f
            ),
            center = center2,
            radius = w * 0.55f
        )

        // Orb 3: moves slowly across bottom
        val center3 = Offset(
            x = w * (0.50f + 0.20f * sin(phase * 0.6f)),
            y = h * (0.70f + 0.12f * cos(phase * 0.6f))
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color3.copy(alpha = alpha * 0.8f), Color.Transparent),
                center = center3,
                radius = w * 0.65f
            ),
            center = center3,
            radius = w * 0.65f
        )
    }
}

/**
 * Frosted Glass surface modifier adapted from Vyxel Apps.
 * Clips to [shape], fills with [containerColor], and applies a subtle high-contrast border.
 * Guarantees zero blur on child text and icons so all navigation and interactive components
 * remain crisp, sharp, and perfectly readable.
 */
fun Modifier.glassSurface(
    shape: Shape,
    containerColor: Color = Color.White.copy(alpha = 0.08f),
    borderColor: Color = Color.White.copy(alpha = 0.18f),
    borderWidth: Dp = 1.dp
): Modifier = this
    .clip(shape)
    .background(containerColor)
    .border(borderWidth, borderColor, shape)

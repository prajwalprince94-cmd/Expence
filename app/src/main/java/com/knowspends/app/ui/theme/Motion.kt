package com.knowspends.app.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Physics-based spring specifications adapted from Vyxel Apps and Material 3 Expressive.
 * Avoids rigid duration tweens for user-initiated interactions.
 */
object KnowSpendsMotion {
    /** Snappy spring for micro-interactions (chips, toggles, keypad taps). */
    val snappy = spring<Float>(dampingRatio = 0.78f, stiffness = 600f)
    fun <T> snappy() = spring<T>(dampingRatio = 0.78f, stiffness = 600f)

    /** Expressive spring for cards, container expansions, and modal sheets. */
    val expressive = spring<Float>(dampingRatio = 0.72f, stiffness = 480f)
    fun <T> expressive() = spring<T>(dampingRatio = 0.72f, stiffness = 480f)

    /** Bouncy spring for alerts, confirmation badges, and floating pops. */
    val bouncy = spring<Float>(dampingRatio = 0.62f, stiffness = 380f)
    fun <T> bouncy() = spring<T>(dampingRatio = 0.62f, stiffness = 380f)

    /** Smooth spring for scroll parallax and shape transitions. */
    val smooth = spring<Float>(dampingRatio = 0.85f, stiffness = 260f)
    fun <T> smooth() = spring<T>(dampingRatio = 0.85f, stiffness = 260f)

    /** Fade tween strictly for alpha cross-fades matching VyxelMotion.fade(). */
    fun <T> fade(durationMs: Int = 200) = tween<T>(durationMillis = durationMs)
}

/**
 * App-wide motion scale multiplier (0.0 = reduced motion / accessibility, 1.0 = normal, 1.5 = hyper-expressive).
 */
val LocalMotionIntensity = compositionLocalOf { 1.0f }

/**
 * Tactile spring-based press-scale modifier.
 * Scales the element down slightly on touch down and rebounds with physics spring on release.
 */
@Composable
fun Modifier.pressScale(
    targetScale: Float = 0.955f
): Modifier {
    val intensity = LocalMotionIntensity.current
    if (intensity <= 0f) return this

    var isPressed by remember { mutableStateOf(false) }
    val effectiveScale = if (isPressed) {
        1f - (1f - targetScale) * intensity
    } else {
        1f
    }

    val animatedScale by animateFloatAsState(
        targetValue = effectiveScale,
        animationSpec = KnowSpendsMotion.snappy,
        label = "pressScale"
    )

    return this
        .graphicsLayer {
            scaleX = animatedScale
            scaleY = animatedScale
        }
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                isPressed = true
                waitForUpOrCancellation()
                isPressed = false
            }
        }
}

/**
 * Applies dynamic alpha gradient masks to the top and bottom of scrollable containers.
 * Prevents harsh edge cuts against app bars and floating navigation pills.
 */
fun Modifier.fadingEdges(
    topFade: Dp = 16.dp,
    bottomFade: Dp = 16.dp
): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()

        val topFadePx = topFade.toPx()
        val bottomFadePx = bottomFade.toPx()
        val height = size.height

        if (topFadePx > 0f) {
            drawRect(
                brush = Brush.verticalGradient(
                    0f to Color.Transparent,
                    1f to Color.Black,
                    startY = 0f,
                    endY = topFadePx
                ),
                blendMode = BlendMode.DstIn
            )
        }

        if (bottomFadePx > 0f && height > bottomFadePx) {
            drawRect(
                brush = Brush.verticalGradient(
                    0f to Color.Black,
                    1f to Color.Transparent,
                    startY = height - bottomFadePx,
                    endY = height
                ),
                blendMode = BlendMode.DstIn
            )
        }
    }

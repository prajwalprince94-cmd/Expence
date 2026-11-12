package com.knowspends.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.knowspends.app.ui.theme.LocalMotionIntensity
import kotlinx.coroutines.launch

/**
 * Aceternity-inspired Spotlight Card:
 * Features an interactive specular radial highlight that dynamically illuminates
 * the card surface and its perimeter border based on touch/drag coordinates.
 */
@Composable
fun AceternitySpotlightCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ),
    spotlightColor: Color = MaterialTheme.colorScheme.primary,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    content: @Composable BoxScope.() -> Unit
) {
    var touchOffset by remember { mutableStateOf(Offset.Unspecified) }
    val spotlightAlpha = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val motionIntensity = LocalMotionIntensity.current

    val dynamicBorder = remember(touchOffset, spotlightAlpha.value, borderColor, spotlightColor) {
        val alpha = if (touchOffset != Offset.Unspecified) spotlightAlpha.value else 0f
        if (alpha > 0.05f && touchOffset != Offset.Unspecified) {
            BorderStroke(
                1.dp,
                Brush.radialGradient(
                    colors = listOf(
                        spotlightColor.copy(alpha = (alpha * 0.85f).coerceIn(0f, 1f)),
                        borderColor.copy(alpha = 0.5f),
                        borderColor.copy(alpha = 0.25f)
                    ),
                    center = touchOffset,
                    radius = 500f
                )
            )
        } else {
            BorderStroke(1.dp, borderColor)
        }
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(colors.containerColor)
            .pointerInput(motionIntensity) {
                if (motionIntensity <= 0f) return@pointerInput
                detectTapGestures(
                    onPress = { offset ->
                        touchOffset = offset
                        scope.launch {
                            spotlightAlpha.animateTo(1f, tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        }
                        tryAwaitRelease()
                        scope.launch {
                            spotlightAlpha.animateTo(0f, spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow))
                        }
                    }
                )
            }
            .pointerInput(motionIntensity) {
                if (motionIntensity <= 0f) return@pointerInput
                detectDragGestures(
                    onDragStart = { offset ->
                        touchOffset = offset
                        scope.launch {
                            spotlightAlpha.animateTo(1f, tween(180, easing = androidx.compose.animation.core.FastOutSlowInEasing))
                        }
                    },
                    onDrag = { change, _ ->
                        touchOffset = change.position
                    },
                    onDragEnd = {
                        scope.launch {
                            spotlightAlpha.animateTo(0f, spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow))
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            spotlightAlpha.animateTo(0f, spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow))
                        }
                    }
                )
            }
            .drawBehind {
                val center = if (touchOffset != Offset.Unspecified) touchOffset else Offset(size.width / 2f, size.height / 3f)
                val alpha = if (touchOffset != Offset.Unspecified) spotlightAlpha.value else 0.12f

                if (alpha > 0f) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                spotlightColor.copy(alpha = alpha * 0.22f),
                                spotlightColor.copy(alpha = alpha * 0.08f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.width.coerceAtLeast(size.height) * 0.75f
                        ),
                        center = center,
                        radius = size.width.coerceAtLeast(size.height) * 0.75f
                    )
                }
            }
            .border(dynamicBorder, shape)
    ) {
        content()
    }
}

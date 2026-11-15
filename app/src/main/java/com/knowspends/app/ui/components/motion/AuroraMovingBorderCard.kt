package com.knowspends.app.ui.components.motion

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.knowspends.app.ui.theme.LocalMotionIntensity
import com.knowspends.app.ui.theme.QuixColors
import com.knowspends.app.ui.theme.pressScale

/**
 * Aceternity & ObsidianUI-inspired Aurora Moving Border Card:
 * Renders an elevated obsidian surface surrounded by an iridescent beam traveling
 * smoothly along the perimeter stroke with tactile spring interaction.
 */
@Composable
fun AuroraMovingBorderCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    cornerRadiusDp: Dp = 16.dp,
    containerColor: Color = QuixColors.ObsidianSurfaceCard,
    beamColor: Color = QuixColors.NeonLime,
    baseBorderColor: Color = QuixColors.BorderSubtle,
    borderWidth: Dp = 1.2.dp,
    durationMillis: Int = 4500,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val motionIntensity = LocalMotionIntensity.current
    val infiniteTransition = rememberInfiniteTransition(label = "aurora_beam")
    val angleDegrees by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing)
        ),
        label = "aurora_angle"
    )

    // Pre-create sweep gradient to avoid 60/120fps allocations in drawWithContent
    val sweepBrush = remember(beamColor) {
        Brush.sweepGradient(
            0.0f to Color.Transparent,
            0.65f to Color.Transparent,
            0.85f to beamColor.copy(alpha = 0.35f),
            0.95f to beamColor.copy(alpha = 0.95f),
            1.0f to Color.Transparent
        )
    }

    val clickModifier = if (onClick != null) {
        Modifier
            .pressScale(0.985f)
            .clickable(onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(clickModifier)
            .clip(shape)
            .drawWithContent {
                val w = size.width
                val h = size.height

                if (w > 0f && h > 0f) {
                    val radiusPx = cornerRadiusDp.toPx()
                    val strokePx = borderWidth.toPx()
                    val halfStroke = strokePx / 2f
                    val innerSize = Size(w - strokePx, h - strokePx)
                    val innerTopLeft = Offset(halfStroke, halfStroke)
                    val innerRadius = (radiusPx - halfStroke).coerceAtLeast(0f)
                    val centerOffset = Offset(w / 2f, h / 2f)

                    // 1. Draw solid container background
                    drawRoundRect(
                        color = containerColor,
                        size = size,
                        cornerRadius = CornerRadius(radiusPx, radiusPx)
                    )

                    // 2. Base hairline border (inset to prevent clip truncation)
                    drawRoundRect(
                        color = baseBorderColor,
                        topLeft = innerTopLeft,
                        size = innerSize,
                        cornerRadius = CornerRadius(innerRadius, innerRadius),
                        style = Stroke(width = strokePx)
                    )

                    // 3. Dynamic rotating aurora beam (if motion enabled)
                    if (motionIntensity > 0.05f) {
                        rotate(degrees = angleDegrees, pivot = centerOffset) {
                            drawRoundRect(
                                brush = sweepBrush,
                                topLeft = innerTopLeft,
                                size = innerSize,
                                cornerRadius = CornerRadius(innerRadius, innerRadius),
                                style = Stroke(width = strokePx * 1.5f)
                            )
                        }
                    }
                }

                // 4. Draw children content
                drawContent()
            }
    ) {
        content()
    }
}

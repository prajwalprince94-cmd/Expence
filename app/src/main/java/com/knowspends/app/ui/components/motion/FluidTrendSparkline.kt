package com.knowspends.app.ui.components.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.knowspends.app.ui.theme.LocalMotionIntensity
import com.knowspends.app.ui.theme.QuixColors

/**
 * Pure mathematical utilities for sparkline normalization and boundary handling.
 */
object SparklineMath {
    fun cleanPoints(data: List<Float>): List<Float> {
        return data.map { if (it.isNaN() || it.isInfinite()) 0f else it }
    }

    fun calculateRange(points: List<Float>): Float {
        val minVal = points.minOrNull() ?: 0f
        val maxVal = points.maxOrNull() ?: 1f
        val diff = maxVal - minVal
        return if (diff <= 0.0001f) 1f else diff
    }
}

/**
 * D3-Motion & ReverseUI-inspired Fluid Trend Sparkline:
 * Renders an ultra-smooth cubic bezier spline curve through financial data points
 * with a luminous gradient area fill, animated draw-in stroke, and glowing endpoint indicator.
 */
@Composable
fun FluidTrendSparkline(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(72.dp),
    lineColor: Color = QuixColors.NeonLime,
    strokeWidth: Dp = 2.5.dp,
    showGradientFill: Boolean = true,
    showEndDot: Boolean = true,
    animationDurationMillis: Int = 1000
) {
    val motionIntensity = LocalMotionIntensity.current
    val progress = remember { Animatable(if (motionIntensity > 0.05f) 0f else 1f) }

    LaunchedEffect(dataPoints, motionIntensity) {
        if (motionIntensity > 0.05f) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = animationDurationMillis, easing = FastOutSlowInEasing)
            )
        } else {
            progress.snapTo(1f)
        }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        if (w <= 0f || h <= 0f || dataPoints.isEmpty()) return@Canvas

        val paddingX = 8.dp.toPx()
        val paddingY = 8.dp.toPx()
        val usableWidth = (w - (2 * paddingX)).coerceAtLeast(1f)
        val usableHeight = (h - (2 * paddingY)).coerceAtLeast(1f)

        val cleanPoints = SparklineMath.cleanPoints(dataPoints)
        val minVal = cleanPoints.minOrNull() ?: 0f
        val range = SparklineMath.calculateRange(cleanPoints)

        val n = cleanPoints.size
        val coords = cleanPoints.mapIndexed { index, value ->
            val x = if (n > 1) {
                paddingX + (index.toFloat() / (n - 1).toFloat()) * usableWidth
            } else {
                w / 2f
            }
            val normY = (value - minVal) / range
            val y = h - paddingY - (normY * usableHeight)
            Offset(x, y)
        }

        if (coords.isEmpty()) return@Canvas

        if (coords.size == 1) {
            val pt = coords.first()
            drawCircle(color = lineColor, radius = 4.dp.toPx(), center = pt)
            return@Canvas
        }

        // Build continuous cubic bezier path
        val fullPath = Path()
        val fillPath = Path()

        val firstPt = coords.first()
        fullPath.moveTo(firstPt.x, firstPt.y)
        fillPath.moveTo(firstPt.x, h - paddingY)
        fillPath.lineTo(firstPt.x, firstPt.y)

        for (i in 1 until coords.size) {
            val prev = coords[i - 1]
            val curr = coords[i]
            val cp1X = prev.x + (curr.x - prev.x) / 2f
            val cp1Y = prev.y
            val cp2X = curr.x - (curr.x - prev.x) / 2f
            val cp2Y = curr.y

            fullPath.cubicTo(cp1X, cp1Y, cp2X, cp2Y, curr.x, curr.y)
            fillPath.cubicTo(cp1X, cp1Y, cp2X, cp2Y, curr.x, curr.y)
        }

        val lastPt = coords.last()
        fillPath.lineTo(lastPt.x, h - paddingY)
        fillPath.close()

        val animatedProgress = progress.value.coerceIn(0f, 1f)

        // Draw luminous area underfill
        if (showGradientFill) {
            val fillBrush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.28f * animatedProgress),
                    lineColor.copy(alpha = 0.08f * animatedProgress),
                    Color.Transparent
                ),
                startY = paddingY,
                endY = h
            )
            drawPath(path = fillPath, brush = fillBrush)
        }

        // Draw animated stroke path using PathMeasure
        val pathMeasure = PathMeasure()
        pathMeasure.setPath(fullPath, false)
        val pathLength = pathMeasure.length

        if (pathLength > 0f) {
            val animatedPath = Path()
            pathMeasure.getSegment(
                startDistance = 0f,
                stopDistance = pathLength * animatedProgress,
                destination = animatedPath,
                startWithMoveTo = true
            )

            drawPath(
                path = animatedPath,
                color = lineColor,
                style = Stroke(
                    width = strokeWidth.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )
        }

        // Glowing end indicator dot on latest data point
        if (showEndDot && animatedProgress > 0.95f) {
            val dotRadius = 4.dp.toPx()
            val glowRadius = 8.dp.toPx()

            // Outer glow ring
            drawCircle(
                color = lineColor.copy(alpha = 0.25f),
                radius = glowRadius,
                center = lastPt
            )
            // Inner core dot
            drawCircle(
                color = lineColor,
                radius = dotRadius,
                center = lastPt
            )
        }
    }
}

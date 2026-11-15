package com.knowspends.app.ui.components.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.knowspends.app.domain.util.MoneyFormatter
import com.knowspends.app.ui.theme.LocalMotionIntensity

/**
 * ReactBits-inspired Animated CountUp Ticker:
 * Smoothly interpolates numeric financial values using normalized progress and Double precision,
 * preventing IEEE 754 32-bit Float precision loss on amounts exceeding ₹1.67 Lakhs.
 * Employs OpenType "tnum" (tabular numerals) to eliminate character width jitter during rolling.
 */
@Composable
fun AnimatedCountUpTicker(
    targetAmountPaise: Long,
    modifier: Modifier = Modifier,
    currencySymbol: String = "₹",
    style: TextStyle = MaterialTheme.typography.headlineLarge,
    color: Color = Color.Unspecified,
    showPositiveSign: Boolean = false,
    durationMillis: Int = 800
) {
    val motionIntensity = LocalMotionIntensity.current
    val progress = remember { Animatable(1f) }
    var startAmount by remember { mutableLongStateOf(targetAmountPaise) }
    var currentTarget by remember { mutableLongStateOf(targetAmountPaise) }

    LaunchedEffect(targetAmountPaise, motionIntensity) {
        if (targetAmountPaise != currentTarget) {
            if (motionIntensity <= 0.05f) {
                startAmount = targetAmountPaise
                currentTarget = targetAmountPaise
                progress.snapTo(1f)
            } else {
                startAmount = if (progress.value >= 1f) {
                    currentTarget
                } else {
                    startAmount + ((currentTarget - startAmount) * progress.value.toDouble()).toLong()
                }
                currentTarget = targetAmountPaise
                progress.snapTo(0f)
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    val currentPaise = if (progress.value >= 1f || motionIntensity <= 0.05f) {
        targetAmountPaise
    } else {
        startAmount + ((currentTarget - startAmount) * progress.value.toDouble()).toLong()
    }

    val formattedText = formatCountUpCurrency(
        amountPaise = currentPaise,
        currencySymbol = currencySymbol,
        showPositiveSign = showPositiveSign
    )

    Text(
        text = formattedText,
        modifier = modifier,
        style = style.copy(
            fontFeatureSettings = "tnum",
            fontWeight = style.fontWeight ?: FontWeight.Bold
        ),
        color = color
    )
}

/**
 * Animated pure integer count ticker (e.g., transaction count or review items count).
 */
@Composable
fun AnimatedIntegerTicker(
    targetCount: Int,
    modifier: Modifier = Modifier,
    prefix: String = "",
    suffix: String = "",
    style: TextStyle = MaterialTheme.typography.titleMedium,
    color: Color = Color.Unspecified,
    durationMillis: Int = 600
) {
    val motionIntensity = LocalMotionIntensity.current
    val progress = remember { Animatable(1f) }
    var startCount by remember { mutableIntStateOf(targetCount) }
    var currentTarget by remember { mutableIntStateOf(targetCount) }

    LaunchedEffect(targetCount, motionIntensity) {
        if (targetCount != currentTarget) {
            if (motionIntensity <= 0.05f) {
                startCount = targetCount
                currentTarget = targetCount
                progress.snapTo(1f)
            } else {
                startCount = if (progress.value >= 1f) {
                    currentTarget
                } else {
                    startCount + ((currentTarget - startCount) * progress.value).toInt()
                }
                currentTarget = targetCount
                progress.snapTo(0f)
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = durationMillis, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    val currentCount = if (progress.value >= 1f || motionIntensity <= 0.05f) {
        targetCount
    } else {
        startCount + ((currentTarget - startCount) * progress.value).toInt()
    }

    Text(
        text = "$prefix$currentCount$suffix",
        modifier = modifier,
        style = style.copy(fontFeatureSettings = "tnum"),
        color = color
    )
}

/**
 * Pure helper for formatting currency with symbol and sign.
 */
fun formatCountUpCurrency(
    amountPaise: Long,
    currencySymbol: String,
    showPositiveSign: Boolean = false
): String {
    val isNegative = amountPaise < 0
    val absPaise = if (amountPaise == Long.MIN_VALUE) Long.MAX_VALUE else kotlin.math.abs(amountPaise)
    val formattedAbs = MoneyFormatter.formatPaise(
        amountPaise = absPaise,
        includeSymbol = false
    ).trim()

    return when {
        isNegative -> "-$currencySymbol$formattedAbs"
        showPositiveSign && amountPaise > 0 -> "+$currencySymbol$formattedAbs"
        else -> "$currencySymbol$formattedAbs"
    }
}

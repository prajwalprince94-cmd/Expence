package com.knowspends.app.ui.components.quix

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import com.knowspends.app.ui.theme.QuixTokens
import com.knowspends.app.ui.theme.pressScale

/**
 * Quix Studio Timeline Stepper
 * Source: https://www.quixstudio.net/free/components
 *
 * Implements the signature interactive timeline stepper with:
 * - Animated connecting rail lines
 * - Milestone indicator dots (completed check, active pulsing glow ring, pending outline)
 * - Row container with subtle hover/active highlight and timestamp/badge support
 */

enum class QuixStepState {
    COMPLETED,
    ACTIVE,
    PENDING
}

data class QuixTimelineStep(
    val title: String,
    val subtitle: String? = null,
    val badgeText: String? = null,
    val state: QuixStepState = QuixStepState.PENDING,
    val amountFormatted: String? = null,
    val onClick: (() -> Unit)? = null
)

@Composable
fun QuixTimelineStepper(
    steps: List<QuixTimelineStep>,
    modifier: Modifier = Modifier,
    accentColor: Color = QuixTokens.NeonLime,
    activeBackgroundColor: Color = QuixTokens.ObsidianCardElevated,
    lineColor: Color = Color.White.copy(alpha = 0.12f)
) {
    val transition = rememberInfiniteTransition(label = "stepper_pulse")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        steps.forEachIndexed { index, step ->
            val isLast = index == steps.size - 1

            val clickModifier = if (step.onClick != null) {
                Modifier
                    .pressScale(0.99f)
                    .clickable(onClick = step.onClick)
            } else Modifier

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .clip(RoundedCornerShape(12.dp))
                    .then(
                        if (step.state == QuixStepState.ACTIVE) {
                            Modifier
                                .background(activeBackgroundColor)
                                .border(1.dp, QuixTokens.BorderMedium, RoundedCornerShape(12.dp))
                        } else Modifier
                    )
                    .then(clickModifier)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.Top
            ) {
                // ── Left Rail Column (Dot + Connecting Line) ─────────────────
                Column(
                    modifier = Modifier
                        .width(32.dp)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Milestone Dot
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .then(
                                if (step.state == QuixStepState.ACTIVE) {
                                    Modifier
                                        .background(accentColor.copy(alpha = pulseAlpha), CircleShape)
                                        .padding(3.dp)
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when (step.state) {
                            QuixStepState.COMPLETED -> {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(accentColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Completed",
                                        tint = QuixTokens.ObsidianCanvas,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            QuixStepState.ACTIVE -> {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .border(2.dp, accentColor, CircleShape)
                                        .background(QuixTokens.ObsidianCard, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(accentColor, CircleShape)
                                    )
                                }
                            }
                            QuixStepState.PENDING -> {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .border(1.5.dp, lineColor, CircleShape)
                                        .background(Color.Transparent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Color.White.copy(alpha = 0.22f), CircleShape)
                                    )
                                }
                            }
                        }
                    }

                    // Connecting vertical rail line
                    if (!isLast) {
                        val railColor = if (step.state == QuixStepState.COMPLETED) accentColor else lineColor
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .weight(1f)
                                .background(railColor, RoundedCornerShape(1.dp))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // ── Right Content Column (Title, Subtitle, Badges) ───────────
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 2.dp, bottom = if (!isLast) 14.dp else 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (step.state == QuixStepState.ACTIVE) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            color = if (step.state == QuixStepState.PENDING) QuixTokens.TextSecondary else QuixTokens.TextPrimary
                        )

                        if (step.amountFormatted != null) {
                            Text(
                                text = step.amountFormatted,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                ),
                                color = if (step.state == QuixStepState.ACTIVE) accentColor else QuixTokens.TextPrimary
                            )
                        } else if (step.badgeText != null) {
                            QuixPillBadge(
                                text = step.badgeText,
                                accentColor = if (step.state == QuixStepState.ACTIVE) accentColor else QuixTokens.TextSecondary
                            )
                        }
                    }

                    if (step.subtitle != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = step.subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            ),
                            color = QuixTokens.TextMuted
                        )
                    }
                }
            }
        }
    }
}

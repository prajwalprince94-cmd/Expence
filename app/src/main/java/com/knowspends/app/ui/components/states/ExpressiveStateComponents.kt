package com.knowspends.app.ui.components.states

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.ui.theme.pressScale
import com.knowspends.app.util.VyxelHaptics

// ─────────────────────────────────────────────────────────────────────────────
// 1. EXPRESSIVE SHIMMER SKELETON ENGINE
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Sweeping shimmer animation modifier matching KnowSpends dark palette rhythm.
 */
fun Modifier.expressiveShimmer(
    shape: Shape = VyxelShapeTokens.CardSmall,
    baseColor: Color? = null,
    highlightColor: Color? = null
): Modifier = composed {
    val scheme = MaterialTheme.colorScheme
    val actualBase = baseColor ?: scheme.surfaceContainerHigh.copy(alpha = 0.5f)
    val actualHighlight = highlightColor ?: scheme.surfaceContainerHighest.copy(alpha = 0.85f)

    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "expressive_shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = -1.5f * size.width.coerceAtLeast(300),
        targetValue = 2.5f * size.width.coerceAtLeast(300),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    this
        .onGloballyPositioned { size = it.size }
        .clip(shape)
        .background(
            brush = Brush.linearGradient(
                colors = listOf(actualBase, actualHighlight, actualBase),
                start = Offset(x = translateAnimation, y = 0f),
                end = Offset(x = translateAnimation + size.width.toFloat().coerceAtLeast(300f), y = size.height.toFloat().coerceAtLeast(300f))
            ),
            shape = shape
        )
}

/**
 * Standardized single transaction skeleton row.
 */
@Composable
fun ExpressiveTransactionSkeletonItem(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = VyxelShapeTokens.CardSmall,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon squircle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .expressiveShimmer(shape = VyxelShapeTokens.IconSmall)
            )

            // Text lines
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(14.dp)
                        .expressiveShimmer(shape = RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.35f)
                        .height(11.dp)
                        .expressiveShimmer(shape = RoundedCornerShape(4.dp))
                )
            }

            // Amount pill
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .height(18.dp)
                    .expressiveShimmer(shape = VyxelShapeTokens.Pill)
            )
        }
    }
}

/**
 * Transaction list shimmer container.
 */
@Composable
fun ExpressiveTransactionListSkeleton(
    count: Int = 4,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(count) {
            ExpressiveTransactionSkeletonItem()
        }
    }
}

/**
 * Spotlight hero card skeleton for Dashboard and Subscriptions.
 */
@Composable
fun ExpressiveHeroCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = VyxelShapeTokens.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(12.dp)
                        .expressiveShimmer(shape = RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .expressiveShimmer(shape = CircleShape)
                )
            }

            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(36.dp)
                    .expressiveShimmer(shape = RoundedCornerShape(6.dp))
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .expressiveShimmer(shape = VyxelShapeTokens.Pill)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. EXPRESSIVE EMPTY STATE COMPONENT
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Cohesive, expressive empty state matching KnowSpends dark surface aesthetics.
 */
@Composable
fun ExpressiveEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    isCompact: Boolean = false
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = if (isCompact) VyxelShapeTokens.CardSmall else VyxelShapeTokens.Card,
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isCompact) 18.dp else 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon container with amber accent glow
            Box(
                modifier = Modifier
                    .size(if (isCompact) 48.dp else 64.dp)
                    .clip(VyxelShapeTokens.CardSmall)
                    .background(scheme.surfaceContainerHighest.copy(alpha = 0.8f))
                    .border(1.dp, scheme.primary.copy(alpha = 0.25f), VyxelShapeTokens.CardSmall),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(if (isCompact) 24.dp else 32.dp),
                    tint = scheme.primary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                style = if (isCompact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = if (isCompact) 8.dp else 16.dp),
                lineHeight = 18.sp
            )

            if (!actionLabel.isNullOrBlank() && onActionClick != null) {
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = {
                        VyxelHaptics.click(context)
                        onActionClick()
                    },
                    shape = VyxelShapeTokens.Pill,
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.primary),
                    modifier = Modifier
                        .height(42.dp)
                        .pressScale(0.97f)
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onPrimary
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. EXPRESSIVE NO SEARCH RESULTS
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Dedicated contextual state for search without results.
 */
@Composable
fun ExpressiveNoSearchResults(
    query: String,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier,
    suggestedQueries: List<String> = emptyList(),
    onSuggestionClick: ((String) -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = VyxelShapeTokens.Card,
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(scheme.surfaceContainerHighest.copy(alpha = 0.8f))
                    .border(1.dp, scheme.outlineVariant.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SearchOff,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = scheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "No results found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "No matches found for \"$query\". Check spelling or try a broader keyword.",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    VyxelHaptics.click(context)
                    onClearSearch()
                },
                shape = VyxelShapeTokens.Pill,
                colors = ButtonDefaults.buttonColors(
                    containerColor = scheme.surfaceContainerHigh,
                    contentColor = scheme.onSurface
                ),
                border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.height(38.dp).pressScale(0.97f)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Clear Search",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (suggestedQueries.isNotEmpty() && onSuggestionClick != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "QUICK SUGGESTIONS",
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(suggestedQueries) { suggestion ->
                        Surface(
                            shape = VyxelShapeTokens.Pill,
                            color = scheme.surfaceContainerHighest.copy(alpha = 0.7f),
                            border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .pressScale(0.96f)
                                .clickable {
                                    VyxelHaptics.click(context)
                                    onSuggestionClick(suggestion)
                                }
                        ) {
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. EXPRESSIVE ERROR STATE & ERROR BANNER
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Standardized retryable error card.
 */
@Composable
fun ExpressiveErrorState(
    title: String = "Something went wrong",
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = VyxelShapeTokens.Card,
        colors = CardDefaults.cardColors(containerColor = scheme.errorContainer.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, scheme.error.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(scheme.error.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = scheme.error
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        VyxelHaptics.click(context)
                        onRetry()
                    },
                    shape = VyxelShapeTokens.Pill,
                    colors = ButtonDefaults.buttonColors(containerColor = scheme.error),
                    modifier = Modifier.height(40.dp).pressScale(0.97f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = scheme.onError
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Try Again",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onError
                    )
                }

                if (onDismiss != null) {
                    TextButton(
                        onClick = {
                            VyxelHaptics.click(context)
                            onDismiss()
                        },
                        shape = VyxelShapeTokens.Pill,
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(
                            text = "Dismiss",
                            style = MaterialTheme.typography.labelLarge,
                            color = scheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Compact inline error banner for non-blocking alerts.
 */
@Composable
fun ExpressiveErrorBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = VyxelShapeTokens.CardSmall,
        color = scheme.errorContainer.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, scheme.error.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = scheme.error,
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (onRetry != null) {
                TextButton(
                    onClick = {
                        VyxelHaptics.click(context)
                        onRetry()
                    },
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "Retry",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(
                onClick = {
                    VyxelHaptics.click(context)
                    onDismiss()
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = scheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

package com.knowspends.app.ui.island

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knowspends.app.ui.components.CategoryBadge
import com.knowspends.app.ui.components.bouncyClickable
import com.knowspends.app.ui.theme.ExpenseColor
import com.knowspends.app.ui.theme.IncomeColor
import com.knowspends.app.ui.theme.LocalMotionIntensity
import com.knowspends.app.ui.theme.VyxelShapeTokens

/**
 * iOS Dynamic Island style Quick Spend Popup for Jetpack Compose.
 *
 * Motion Dynamics per User Request:
 * - Employs Framer Motion physics springs: low damping (0.72) for organic playful bounce
 *   and 380f stiffness for immediate 90fps responsiveness.
 * - Hardware layer accelerated via [graphicsLayer] to eliminate jank on 90Hz/120Hz displays.
 * - Anchored around the camera cutout / top status bar.
 */
@Composable
fun DynamicIslandQuickSpend(
    uiState: DynamicIslandUiState,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onCategorySelected: (Long) -> Unit,
    onAccountSelected: (Long) -> Unit,
    onSaveExpense: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val scheme = MaterialTheme.colorScheme
    val motionIntensity = LocalMotionIntensity.current
    val isExpanded = uiState.isExpanded

    // Framer Motion spring physics spec
    val morphSpring = if (motionIntensity > 0f) {
        spring<Float>(dampingRatio = 0.72f, stiffness = 380f)
    } else {
        spring(dampingRatio = 1.0f, stiffness = 1000f)
    }

    val morphDpSpring = if (motionIntensity > 0f) {
        spring<androidx.compose.ui.unit.Dp>(dampingRatio = 0.72f, stiffness = 380f)
    } else {
        spring(dampingRatio = 1.0f, stiffness = 1000f)
    }

    val morphIntSizeSpring = if (motionIntensity > 0f) {
        spring<androidx.compose.ui.unit.IntSize>(dampingRatio = 0.72f, stiffness = 380f)
    } else {
        spring(dampingRatio = 1.0f, stiffness = 1000f)
    }

    val islandWidth by animateDpAsState(
        targetValue = if (isExpanded) 364.dp else 156.dp,
        animationSpec = morphDpSpring,
        label = "islandWidth"
    )

    val islandCorner by animateDpAsState(
        targetValue = if (isExpanded) 30.dp else 22.dp,
        animationSpec = morphDpSpring,
        label = "islandCorner"
    )

    // Subtle breathing pulse for idle notch mode
    val pulseTransition = rememberInfiniteTransition(label = "islandPulse")
    val idlePulse by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (motionIntensity > 0f && !isExpanded) 1.04f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idlePulse"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            modifier = Modifier
                .width(islandWidth)
                .wrapContentHeight()
                .shadow(
                    elevation = if (isExpanded) 20.dp else 6.dp,
                    shape = RoundedCornerShape(islandCorner),
                    spotColor = scheme.primary.copy(alpha = if (isExpanded) 0.45f else 0.20f),
                    ambientColor = Color.Black.copy(alpha = 0.35f)
                )
                .graphicsLayer {
                    if (!isExpanded) {
                        scaleX = idlePulse
                        scaleY = idlePulse
                    }
                }
                .clip(RoundedCornerShape(islandCorner))
                .animateContentSize(animationSpec = morphIntSizeSpring),
            color = Color(0xFF0F111A), // AMOLED deep glass surface
            border = BorderStroke(
                width = if (isExpanded) 1.5.dp else 1.dp,
                brush = Brush.linearGradient(
                    if (uiState.isSuccess) {
                        listOf(IncomeColor, scheme.primary)
                    } else {
                        listOf(
                            scheme.primary.copy(alpha = 0.70f),
                            scheme.tertiary.copy(alpha = 0.45f),
                            scheme.outlineVariant.copy(alpha = 0.25f)
                        )
                    }
                )
            ),
            shape = RoundedCornerShape(islandCorner)
        ) {
            if (!isExpanded) {
                // Compact Floating Notch State
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = "Expand Quick Spend Island"
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onExpand()
                        }
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(scheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = scheme.onPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Text(
                        text = "Tap Back",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )

                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(scheme.primary)
                    )
                }
            } else {
                // Expanded Dynamic Island Quick-Add Modal
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Bar: Icon + Account Selector + Close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(VyxelShapeTokens.Pill)
                                    .background(scheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = scheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "Dynamic Island",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Account Picker Dropdown Pill
                        var showAccountMenu by remember { mutableStateOf(false) }
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(VyxelShapeTokens.Pill)
                                    .background(scheme.surfaceContainerHighest.copy(alpha = 0.6f))
                                    .clickable { showAccountMenu = true }
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = uiState.selectedAccount?.name ?: "Account",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = scheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.Default.ExpandMore,
                                    contentDescription = "Select account",
                                    tint = scheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showAccountMenu,
                                onDismissRequest = { showAccountMenu = false }
                            ) {
                                uiState.accounts.forEach { acc ->
                                    DropdownMenuItem(
                                        text = { Text(acc.name) },
                                        onClick = {
                                            onAccountSelected(acc.id)
                                            showAccountMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Close Button
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onCollapse()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Dynamic Island",
                                tint = scheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Hero Amount Display
                    Text(
                        text = uiState.formattedAmount,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1.0).sp
                        ),
                        color = if (uiState.isSuccess) IncomeColor else ExpenseColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category Selector Quick Carousel
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        uiState.categories.forEach { cat ->
                            val isSelected = cat.id == uiState.selectedCategoryId
                            val badgeBorder = if (isSelected) {
                                BorderStroke(2.dp, scheme.primary)
                            } else null

                            Surface(
                                shape = VyxelShapeTokens.Pill,
                                color = if (isSelected) scheme.primaryContainer else scheme.surfaceContainerHighest.copy(alpha = 0.5f),
                                border = badgeBorder,
                                modifier = Modifier
                                    .bouncyClickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onCategorySelected(cat.id)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    CategoryBadge(
                                        categoryName = cat.name,
                                        iconKey = cat.iconKey,
                                        color = Color(cat.colour),
                                        size = 20.dp
                                    )
                                    Text(
                                        text = cat.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) scheme.onPrimaryContainer else Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Mini Keypad (3 columns × 4 rows)
                    val keypadRows = listOf(
                        listOf('1', '2', '3'),
                        listOf('4', '5', '6'),
                        listOf('7', '8', '9'),
                        listOf('.', '0', '⌫')
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        keypadRows.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                row.forEach { key ->
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .bouncyClickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                if (key == '⌫') onBackspace() else onDigit(key)
                                            },
                                        shape = VyxelShapeTokens.Chip,
                                        color = scheme.surfaceContainerLowest
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            if (key == '⌫') {
                                                Icon(
                                                    imageVector = Icons.Default.Backspace,
                                                    contentDescription = "Backspace",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            } else {
                                                Text(
                                                    text = key.toString(),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Save / Log Expense Action Button
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSaveExpense()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = VyxelShapeTokens.Pill,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isSuccess) IncomeColor else scheme.primary
                        ),
                        enabled = !uiState.isSaving
                    ) {
                        AnimatedVisibility(
                            visible = uiState.isSuccess,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Logged!",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        if (!uiState.isSuccess) {
                            Text(
                                text = if (uiState.isSaving) "Saving..." else "Log Expense",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

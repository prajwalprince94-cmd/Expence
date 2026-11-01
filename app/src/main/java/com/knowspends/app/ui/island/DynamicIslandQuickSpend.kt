package com.knowspends.app.ui.island

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
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
import kotlinx.coroutines.delay

/**
 * iOS Dynamic Island Quick-Spend Modal.
 *
 * Design & Motion Physics:
 * - Zero Idle Screen Intrusion: When idle, completely hidden. Nothing shows on top of any screens.
 * - Fluid Physics Ejection: When triggered by double-tap on the back of the phone,
 *   the island fluidly ejects downwards from the top camera cut-out over a translucent dark scrim.
 * - Framer Motion spring physics: Damping ratio 0.65 (organic liquid overshoot) and stiffness 320f (fast, snappy 90fps).
 * - Interactive Dismissal: Tapping outside on the dimmed backdrop, swiping up on the handle, or tapping close ("✕")
 *   smoothly retracts the island back into the camera notch.
 * - Success feedback: Shows fluid green confirmation badge and auto-collapses on expense save.
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

    // Auto-dismiss after success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            delay(800)
            onCollapse()
        }
    }

    // Amount bounce animation when digit is pressed
    val amountScale = remember { Animatable(1f) }
    LaunchedEffect(uiState.amountInput) {
        if (isExpanded && uiState.amountMinor > 0L) {
            amountScale.animateTo(
                targetValue = 1.08f,
                animationSpec = tween(durationMillis = 60)
            )
            amountScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.60f, stiffness = Spring.StiffnessMedium)
            )
        }
    }

    val fluidSpring = if (motionIntensity > 0f) {
        spring<Float>(dampingRatio = 0.65f, stiffness = 320f)
    } else {
        spring(dampingRatio = 1.0f, stiffness = 1000f)
    }

    // Outer Overlay: Full screen modal container when expanded; completely gone when collapsed
    AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn(animationSpec = tween(180)),
        exit = fadeOut(animationSpec = tween(150))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            // 1. Dimmed backdrop scrim (Tap outside to collapse)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.62f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCollapse()
                    }
            )

            // 2. Fluid Dynamic Island Card
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(animationSpec = tween(160)) +
                        scaleIn(
                            initialScale = 0.55f,
                            transformOrigin = TransformOrigin(0.5f, 0f),
                            animationSpec = fluidSpring
                        ) +
                        slideInVertically(
                            initialOffsetY = { -it / 2 },
                            animationSpec = spring(dampingRatio = 0.65f, stiffness = 320f)
                        ),
                exit = fadeOut(animationSpec = tween(120)) +
                        scaleOut(
                            targetScale = 0.55f,
                            transformOrigin = TransformOrigin(0.5f, 0f),
                            animationSpec = spring<Float>(dampingRatio = 0.85f, stiffness = 400f)
                        ) +
                        slideOutVertically(
                            targetOffsetY = { -it / 2 },
                            animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f)
                        )
            ) {
                val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

                Surface(
                    modifier = Modifier
                        .padding(top = statusBarHeight + 8.dp)
                        .padding(horizontal = 16.dp)
                        .width(360.dp)
                        .wrapContentHeight()
                        .shadow(
                            elevation = 24.dp,
                            shape = RoundedCornerShape(28.dp),
                            spotColor = scheme.primary.copy(alpha = 0.40f),
                            ambientColor = Color.Black.copy(alpha = 0.50f)
                        )
                        .clip(RoundedCornerShape(28.dp))
                        .pointerInput(Unit) {
                            detectVerticalDragGestures { change, dragAmount ->
                                change.consume()
                                if (dragAmount < -12f) {
                                    // Swipe-up detected: collapse
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onCollapse()
                                }
                            }
                        },
                    color = Color(0xFF0F111A), // Deep AMOLED obsidian
                    border = BorderStroke(
                        width = 1.2.dp,
                        brush = Brush.verticalGradient(
                            if (uiState.isSuccess) {
                                listOf(IncomeColor, scheme.primary.copy(alpha = 0.5f))
                            } else {
                                listOf(
                                    scheme.primary.copy(alpha = 0.75f),
                                    scheme.tertiary.copy(alpha = 0.40f),
                                    scheme.outlineVariant.copy(alpha = 0.20f)
                                )
                            }
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Top Drag / Dismiss Handle
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(VyxelShapeTokens.Pill)
                                .background(Color.White.copy(alpha = 0.25f))
                        )

                        Spacer(Modifier.height(10.dp))

                        // Header Bar: Icon + Account Selector + Close Button
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
                                        .clip(CircleShape)
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
                                    text = "Quick Spend",
                                    style = MaterialTheme.typography.titleSmall,
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

                        Spacer(Modifier.height(8.dp))

                        // Hero Amount Display with scale bounce on keystroke
                        Text(
                            text = uiState.formattedAmount,
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-1.0).sp
                            ),
                            color = if (uiState.isSuccess) IncomeColor else ExpenseColor,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    scaleX = amountScale.value
                                    scaleY = amountScale.value
                                }
                        )

                        if (uiState.errorMessage != null) {
                            Text(
                                text = uiState.errorMessage,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(Modifier.height(10.dp))

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

                        Spacer(Modifier.height(14.dp))

                        // Tactile 3x4 Keypad
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

                        Spacer(Modifier.height(14.dp))

                        // Save Expense CTA Button
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
                                        text = "Saved!",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            if (!uiState.isSuccess) {
                                Text(
                                    text = if (uiState.isSaving) "Saving..." else "Save Expense",
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
}

package com.knowspends.app.ui.island

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knowspends.app.ui.components.CategoryBadge
import com.knowspends.app.ui.components.bouncyClickable
import com.knowspends.app.ui.theme.ExpenseColor
import com.knowspends.app.ui.theme.IncomeColor
import com.knowspends.app.ui.theme.LocalMotionIntensity
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.util.VyxelHaptics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Islandcn Design System Tokens ─────────────────────────────────────────────
// Exact specs from opencoredev/islandcn (references/design.md & metrics.ts):
// Pure AMOLED black (#000000), 1.2dp white keyline at 0.16 alpha, continuous squircles.
private val IslandBlack = Color(0xFF000000)
private val IslandKeyline = Color.White.copy(alpha = 0.16f)
private val IslandAmber = Color(0xFFD4A853)
private val IslandGreen = Color(0xFF32D74B) // iOS ActivityKit green (#32d74b)
private val IslandTileBg = Color(0xFFD4A853).copy(alpha = 0.16f)
private val IslandKeyBg = Color.White.copy(alpha = 0.08f)
private val IslandExpandedRadius = 38.dp
private val IslandCompactRadius = 18.5.dp

/**
 * Authentic Dynamic Island implementation for KnowSpends based on opencoredev/islandcn.
 *
 * Presentations:
 * 1. Compact: A sleek, resting black pill pinned right below the camera notch displaying
 *    real-time daily spending. Touch-down swell spring animation transitions smoothly.
 * 2. Expanded: Ejects fluidly on tap, back-tap, or Quick Settings tile with authentic spring
 *    mechanics (response 0.387, dampingFraction 0.792), continuous squircle edges,
 *    concentric keypad keys, category carousel, and celebratory confirmation badge.
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scheme = MaterialTheme.colorScheme
    val motionIntensity = LocalMotionIntensity.current
    val isExpanded = uiState.isExpanded

    // Exact spring physics from islandcn (response 0.387, dampingFraction 0.792)
    val expandSpringFloat = if (motionIntensity > 0f) {
        spring<Float>(dampingRatio = 0.79f, stiffness = 280f)
    } else {
        spring(dampingRatio = 1.0f, stiffness = 1000f)
    }
    val expandSpringOffset = if (motionIntensity > 0f) {
        spring<IntOffset>(dampingRatio = 0.79f, stiffness = 280f)
    } else {
        spring(dampingRatio = 1.0f, stiffness = 1000f)
    }

    val collapseSpringFloat = spring<Float>(dampingRatio = 0.88f, stiffness = 320f)
    val collapseSpringOffset = spring<IntOffset>(dampingRatio = 0.88f, stiffness = 320f)

    // Hardware and gesture Back button handling to smoothly collapse island instead of exiting app
    BackHandler(enabled = isExpanded) {
        onCollapse()
    }

    // Interactive tactile compression swell when touched
    val islandSwellScale = remember { Animatable(1f) }
    fun triggerIslandSwell() {
        coroutineScope.launch {
            islandSwellScale.animateTo(1.08f, animationSpec = tween(60))
            islandSwellScale.animateTo(1f, animationSpec = spring(dampingRatio = 0.55f, stiffness = 420f))
        }
    }

    // Auto-dismiss after successful transaction save
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            delay(750)
            onCollapse()
        }
    }

    // Amount bounce animation when a digit is entered
    val amountScale = remember { Animatable(1f) }
    LaunchedEffect(uiState.amountInput) {
        if (isExpanded && uiState.amountMinor > 0L) {
            amountScale.animateTo(1.08f, animationSpec = tween(50))
            amountScale.animateTo(1f, animationSpec = spring(dampingRatio = 0.60f, stiffness = Spring.StiffnessMedium))
        }
    }

    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        // ── 1. Compact Resting Live Activity Pill (islandcn compact presentation) ─
        if (uiState.isPillVisible && !isExpanded) {
            Surface(
                modifier = Modifier
                    .padding(top = maxOf(statusBarHeight - 2.dp, 8.dp))
                    .width(196.dp)
                    .height(37.dp)
                    .graphicsLayer {
                        scaleX = islandSwellScale.value
                        scaleY = islandSwellScale.value
                    }
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        spotColor = Color.Black.copy(alpha = 0.40f)
                    )
                    .clip(CircleShape)
                    .clickable {
                        triggerIslandSwell()
                        VyxelHaptics.click(context)
                        onExpand()
                    },
                color = IslandBlack,
                border = BorderStroke(1.2.dp, IslandKeyline),
                shape = CircleShape
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Leading Slot: Amber icon tile concentric with left capsule arc
                    Box(
                        modifier = Modifier
                            .size(23.dp)
                            .clip(CircleShape)
                            .background(IslandTileBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Dynamic Island",
                            tint = IslandAmber,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    // Trailing Slot: Live status / daily spending formatted with tabular figures
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color.White.copy(alpha = 0.62f)
                        )
                        Text(
                            text = uiState.todaySpentFormatted,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }

        // ── 2. Expanded Modal Activity Presentation (islandcn expanded presentation)
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(animationSpec = tween(180)),
            exit = fadeOut(animationSpec = tween(150))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                // Dimmed translucent backdrop scrim (Tap outside to collapse)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.62f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            VyxelHaptics.tick(context)
                            onCollapse()
                        }
                )

                // Fluid Island Card with exact islandcn geometry & spring physics
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(animationSpec = tween(140)) +
                            scaleIn(
                                initialScale = 0.42f,
                                transformOrigin = TransformOrigin(0.5f, 0f),
                                animationSpec = expandSpringFloat
                            ) +
                            slideInVertically(
                                initialOffsetY = { -it / 2 },
                                animationSpec = expandSpringOffset
                            ),
                    exit = fadeOut(animationSpec = tween(120)) +
                            scaleOut(
                                targetScale = 0.42f,
                                transformOrigin = TransformOrigin(0.5f, 0f),
                                animationSpec = collapseSpringFloat
                            ) +
                            slideOutVertically(
                                targetOffsetY = { -it / 2 },
                                animationSpec = collapseSpringOffset
                            )
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(top = statusBarHeight + 6.dp)
                            .padding(horizontal = 14.dp)
                            .width(368.dp)
                            .wrapContentHeight()
                            .graphicsLayer {
                                scaleX = islandSwellScale.value
                                scaleY = islandSwellScale.value
                            }
                            .shadow(
                                elevation = 24.dp,
                                shape = RoundedCornerShape(IslandExpandedRadius),
                                spotColor = Color.Black.copy(alpha = 0.65f),
                                ambientColor = Color.Black.copy(alpha = 0.45f)
                            )
                            .clip(RoundedCornerShape(IslandExpandedRadius))
                            .pointerInput(Unit) {
                                detectVerticalDragGestures { change, dragAmount ->
                                    change.consume()
                                    if (dragAmount < -14f) {
                                        VyxelHaptics.tick(context)
                                        onCollapse()
                                    }
                                }
                            },
                        color = IslandBlack,
                        border = BorderStroke(
                            width = 1.2.dp,
                            color = if (uiState.isSuccess) IslandGreen else IslandKeyline
                        ),
                        shape = RoundedCornerShape(IslandExpandedRadius)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Top Drag Handle (islandcn sensor clearance)
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(4.dp)
                                    .clip(VyxelShapeTokens.Pill)
                                    .background(Color.White.copy(alpha = 0.22f))
                            )

                            Spacer(Modifier.height(10.dp))

                            // Top Sensor Row: IconTile + Context Label + Account Picker + Close Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Leading: Concentric 36dp IconTile with 12dp continuous corner
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(IslandTileBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBalanceWallet,
                                            contentDescription = null,
                                            tint = IslandAmber,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Quick Spend",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Today: ${uiState.todaySpentFormatted}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.60f)
                                        )
                                    }
                                }

                                // Trailing: Account Dropdown Capsule + Close Button
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    var showAccountMenu by remember { mutableStateOf(false) }
                                    Box {
                                        Surface(
                                            onClick = { showAccountMenu = true },
                                            shape = VyxelShapeTokens.Pill,
                                            color = Color.White.copy(alpha = 0.12f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = uiState.selectedAccount?.name ?: "Account",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Icon(
                                                    imageVector = Icons.Default.ExpandMore,
                                                    contentDescription = "Select account",
                                                    tint = Color.White.copy(alpha = 0.85f),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
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
                                    Surface(
                                        onClick = {
                                            VyxelHaptics.tick(context)
                                            onCollapse()
                                        },
                                        shape = CircleShape,
                                        color = Color.White.copy(alpha = 0.15f),
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Close Dynamic Island",
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Hero Amount Display with scale spring bounce on entry
                            Text(
                                text = uiState.formattedAmount,
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 34.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = when {
                                    uiState.isSuccess -> IslandGreen
                                    uiState.amountMinor > 0L -> IslandAmber
                                    else -> Color.White
                                },
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
                                    color = ExpenseColor,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            // Category Selector Concentric Carousel
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                uiState.categories.forEach { cat ->
                                    val isSelected = cat.id == uiState.selectedCategoryId

                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isSelected) IslandTileBg else Color.White.copy(alpha = 0.08f),
                                        border = if (isSelected) BorderStroke(1.2.dp, IslandAmber) else null,
                                        modifier = Modifier
                                            .bouncyClickable {
                                                triggerIslandSwell()
                                                VyxelHaptics.tick(context)
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
                                                size = 18.dp
                                            )
                                            Text(
                                                text = cat.name,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) IslandAmber else Color.White
                                            )
                                            AnimatedVisibility(visible = isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = IslandAmber,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            // Concentric Tactile 3x4 Keypad
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
                                                        triggerIslandSwell()
                                                        if (key == '⌫') {
                                                            VyxelHaptics.tick(context)
                                                            onBackspace()
                                                        } else {
                                                            VyxelHaptics.click(context)
                                                            onDigit(key)
                                                        }
                                                    },
                                                shape = RoundedCornerShape(14.dp),
                                                color = IslandKeyBg
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
                                                            style = MaterialTheme.typography.titleMedium.copy(
                                                                fontFamily = FontFamily.Monospace,
                                                                fontWeight = FontWeight.Bold
                                                            ),
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

                            // Bottom Full-Width Capsule Action CTA
                            Button(
                                onClick = {
                                    VyxelHaptics.success(context)
                                    onSaveExpense()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = VyxelShapeTokens.Pill,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (uiState.isSuccess) IslandGreen else IslandAmber,
                                    contentColor = if (uiState.isSuccess) Color.White else Color.Black
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
                                    if (uiState.isSaving) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                color = Color.Black,
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp
                                            )
                                            Text(
                                                text = "Saving...",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = "Log Expense",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

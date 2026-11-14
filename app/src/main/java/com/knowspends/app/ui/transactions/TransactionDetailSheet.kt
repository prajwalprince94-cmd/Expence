package com.knowspends.app.ui.transactions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.knowspends.app.domain.model.TransactionType
import com.knowspends.app.domain.model.TransactionWithDetails
import com.knowspends.app.domain.util.MoneyFormatter
import com.knowspends.app.ui.components.CategoryBadge
import com.knowspends.app.ui.theme.ExpenseColor
import com.knowspends.app.ui.theme.IncomeColor
import com.knowspends.app.ui.theme.VyxelShapeTokens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modal Bottom Sheet presenting full details of a single transaction.
 * Allows viewing complete metadata (amount, type, accounts, category, payee, notes, date/time)
 * with dedicated Edit and Delete actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailSheet(
    item: TransactionWithDetails,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scheme = MaterialTheme.colorScheme
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val tx = item.transaction
    val type = tx.type
    val amountMinor = tx.amountMinor
    val formattedAmount = MoneyFormatter.formatPaise(amountMinor)

    val (typeLabel, typeColor, typeIcon, amountPrefix) = when (type) {
        TransactionType.EXPENSE -> Quadruple(
            "Expense",
            ExpenseColor,
            Icons.Default.ArrowDownward,
            "- "
        )
        TransactionType.INCOME -> Quadruple(
            "Income",
            IncomeColor,
            Icons.Default.ArrowUpward,
            "+ "
        )
        TransactionType.TRANSFER -> Quadruple(
            "Transfer",
            Color(0xFF818CF8),
            Icons.Default.SwapHoriz,
            "⇄ "
        )
    }

    val fullDateFormat = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val dateObj = remember(tx.date) { Date(tx.date) }
    val formattedFullDate = remember(dateObj) { fullDateFormat.format(dateObj) }
    val formattedTime = remember(dateObj) { timeFormat.format(dateObj) }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Delete Transaction?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this $typeLabel transaction of $amountPrefix$formattedAmount? You can undo this immediately from the snackbar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ExpenseColor,
                        contentColor = Color.White
                    )
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = scheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        ) {
            // Header Bar with Title and Close button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transaction Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.onSurface
                )

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(scheme.surfaceContainerHighest)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = scheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Highlight Hero Card: Type chip + Large amount
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = VyxelShapeTokens.CardLarge,
                    color = scheme.surfaceContainerLowest,
                    border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Type Badge
                        Surface(
                            shape = VyxelShapeTokens.Pill,
                            color = typeColor.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, typeColor.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = typeIcon,
                                    contentDescription = null,
                                    tint = typeColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = typeLabel.uppercase(Locale.getDefault()),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = typeColor
                                )
                            }
                        }

                        // Large Tabular Amount
                        Text(
                            text = "$amountPrefix$formattedAmount",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = typeColor,
                            textAlign = TextAlign.Center
                        )

                        // Formatted Date & Time Subtitle
                        Text(
                            text = "$formattedFullDate • $formattedTime",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                // Metadata Rows Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = VyxelShapeTokens.CardLarge,
                    color = scheme.surfaceContainerLowest,
                    border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Category (or Account for Transfer)
                        if (type == TransactionType.TRANSFER) {
                            DetailMetadataRow(
                                icon = Icons.Default.SwapHoriz,
                                label = "Transfer Route",
                                value = "${item.accountName} → ${item.transferAccountName ?: "Account"}",
                                valueColor = scheme.onSurface
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CategoryBadge(
                                        categoryName = item.categoryName ?: "Other",
                                        iconKey = item.categoryIcon,
                                        color = item.categoryColour?.let { Color(it) } ?: scheme.primary,
                                        size = 32.dp
                                    )
                                    Column {
                                        Text(
                                            text = "Category",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = scheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = item.categoryName ?: "Uncategorized",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = scheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            color = scheme.outlineVariant.copy(alpha = 0.35f),
                            thickness = 1.dp
                        )

                        // Account
                        DetailMetadataRow(
                            icon = Icons.Default.AccountBalance,
                            label = if (type == TransactionType.TRANSFER) "Source Account" else "Account",
                            value = item.accountName,
                            valueColor = scheme.onSurface
                        )

                        // Destination Account if Transfer
                        if (type == TransactionType.TRANSFER && item.transferAccountName != null) {
                            HorizontalDivider(
                                color = scheme.outlineVariant.copy(alpha = 0.35f),
                                thickness = 1.dp
                            )
                            DetailMetadataRow(
                                icon = Icons.AutoMirrored.Filled.ArrowForward,
                                label = "Destination Account",
                                value = item.transferAccountName,
                                valueColor = scheme.onSurface
                            )
                        }

                        // Payee / Merchant
                        if (tx.payee.isNotBlank()) {
                            HorizontalDivider(
                                color = scheme.outlineVariant.copy(alpha = 0.35f),
                                thickness = 1.dp
                            )
                            DetailMetadataRow(
                                icon = Icons.Default.Storefront,
                                label = "Payee / Merchant",
                                value = tx.payee,
                                valueColor = scheme.onSurface
                            )
                        }

                        // Notes / Remark
                        if (tx.note.isNotBlank()) {
                            HorizontalDivider(
                                color = scheme.outlineVariant.copy(alpha = 0.35f),
                                thickness = 1.dp
                            )
                            DetailMetadataRow(
                                icon = Icons.Default.Description,
                                label = "Note",
                                value = tx.note,
                                valueColor = scheme.onSurface
                            )
                        }

                        // Date & Time
                        HorizontalDivider(
                            color = scheme.outlineVariant.copy(alpha = 0.35f),
                            thickness = 1.dp
                        )
                        DetailMetadataRow(
                            icon = Icons.Default.CalendarToday,
                            label = "Recorded Date",
                            value = "$formattedFullDate, $formattedTime",
                            valueColor = scheme.onSurface
                        )
                    }
                }

                // Action Buttons: Delete and Edit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Delete Button
                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = VyxelShapeTokens.Button,
                        border = BorderStroke(1.dp, ExpenseColor.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ExpenseColor
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Delete",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Edit Button
                    Button(
                        onClick = onEdit,
                        modifier = Modifier.weight(1.5f),
                        shape = VyxelShapeTokens.Button,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = scheme.primary,
                            contentColor = scheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Edit Transaction",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailMetadataRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(scheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = valueColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

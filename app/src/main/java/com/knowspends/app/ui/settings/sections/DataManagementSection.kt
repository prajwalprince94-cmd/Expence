package com.knowspends.app.ui.settings.sections

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knowspends.app.domain.model.UserPreferences
import com.knowspends.app.ui.theme.ExpenseColor
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.ui.theme.pressScale
import com.knowspends.app.util.VyxelHaptics

@Composable
fun DataManagementSection(
    userPreferences: UserPreferences,
    isLoading: Boolean,
    onCreateBackup: (Uri) -> Unit,
    onRestoreBackup: (Uri) -> Unit,
    onExportCsv: (Uri) -> Unit,
    onImportCsv: (Uri) -> Unit,
    onClearCache: () -> Unit,
    onResetAllData: () -> Unit,
    onSetAutoBackupFrequency: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme

    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val backupCreatePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { onCreateBackup(it) }
    }

    val csvExportPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let { onExportCsv(it) }
    }

    val restoreFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { onRestoreBackup(it) }
    }

    val csvImportPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { onImportCsv(it) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── 1. Backup & Restore (JSON) ─────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = VyxelShapeTokens.Card,
            colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
            border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "VAULT DATABASE BACKUP & RESTORE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary
                )

                Text(
                    text = "Create an encrypted full-fidelity JSON snapshot of your accounts, transactions, budgets, and subscriptions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            VyxelHaptics.tick(context)
                            backupCreatePicker.launch("knowspends_backup_${System.currentTimeMillis()}.json")
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .pressScale(0.96f),
                        shape = VyxelShapeTokens.Pill,
                        colors = ButtonDefaults.buttonColors(containerColor = scheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Create Backup", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            VyxelHaptics.tick(context)
                            restoreFilePicker.launch(arrayOf("application/json", "*/*"))
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .weight(1f)
                            .pressScale(0.96f),
                        shape = VyxelShapeTokens.Pill
                    ) {
                        Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Restore JSON")
                    }
                }

                Text(text = "SCHEDULED BACKUP FREQUENCY", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = scheme.primary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("off" to "Off", "daily" to "Daily", "weekly" to "Weekly").forEach { (freq, label) ->
                        val isSelected = userPreferences.autoBackupFrequency == freq
                        Button(
                            onClick = {
                                VyxelHaptics.tick(context)
                                onSetAutoBackupFrequency(freq)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .pressScale(0.96f),
                            shape = VyxelShapeTokens.Pill,
                            colors = if (isSelected) ButtonDefaults.buttonColors(containerColor = scheme.primary)
                            else ButtonDefaults.buttonColors(containerColor = scheme.surfaceContainerHigh)
                        ) {
                            Text(text = label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) scheme.onPrimary else scheme.onSurface)
                        }
                    }
                }
            }
        }

        // ── 2. Export & Import Reports ─────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = VyxelShapeTokens.Card,
            colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
            border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "REPORTS & STATEMENTS EXPORT",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = scheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            VyxelHaptics.tick(context)
                            csvExportPicker.launch("knowspends_transactions_${System.currentTimeMillis()}.csv")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .pressScale(0.97f),
                        shape = VyxelShapeTokens.Pill
                    ) {
                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Export All Transactions (CSV)")
                    }
                }

                OutlinedButton(
                    onClick = {
                        VyxelHaptics.tick(context)
                        csvImportPicker.launch(arrayOf("text/csv", "text/comma-separated-values", "*/*"))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressScale(0.97f),
                    shape = VyxelShapeTokens.Pill
                ) {
                    Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Import Transactions from CSV / Bank")
                }
            }
        }

        // ── 3. Cache Maintenance ──────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = VyxelShapeTokens.Card,
            colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow),
            border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.45f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Clear App Cache", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = scheme.onSurface)
                    Text(text = "Cleans temporary files and release downloads without altering ledger data.", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                }
                Spacer(Modifier.width(12.dp))
                OutlinedButton(
                    onClick = {
                        VyxelHaptics.click(context)
                        onClearCache()
                    },
                    shape = VyxelShapeTokens.Pill
                ) {
                    Icon(imageVector = Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Clean")
                }
            }
        }

        // ── 4. Danger Zone: Reset All Data ────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = VyxelShapeTokens.Card,
            colors = CardDefaults.cardColors(containerColor = ExpenseColor.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, ExpenseColor.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = ExpenseColor)
                    Text(text = "DANGER ZONE", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = ExpenseColor)
                }

                Text(
                    text = "Irreversibly delete all accounts, transactions, recurring subscriptions, and custom rules. This action cannot be undone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurface
                )

                Button(
                    onClick = {
                        VyxelHaptics.heavy(context)
                        showResetConfirmDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressScale(0.97f),
                    shape = VyxelShapeTokens.Pill,
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseColor)
                ) {
                    Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Erase All Ledger Data", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            icon = { Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = ExpenseColor) },
            title = { Text("Erase All Ledger Data?", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently wipe all local transactions, budgets, subscriptions, and custom accounts. Are you sure you want to proceed?") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetConfirmDialog = false
                        VyxelHaptics.heavy(context)
                        onResetAllData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseColor),
                    shape = VyxelShapeTokens.Pill
                ) {
                    Text("Yes, Erase Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = VyxelShapeTokens.Card
        )
    }
}

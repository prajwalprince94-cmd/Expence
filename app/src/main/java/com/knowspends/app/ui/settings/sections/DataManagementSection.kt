package com.knowspends.app.ui.settings.sections

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import com.knowspends.app.ui.components.VyxelNavBarBottomClearance
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.knowspends.app.domain.model.UserPreferences
import com.knowspends.app.ui.components.expressive.ExpressiveConnectedButtonGroup
import com.knowspends.app.ui.components.expressive.ExpressiveItemDivider
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsGroup
import com.knowspends.app.ui.components.expressive.ExpressiveSettingsItem
import com.knowspends.app.ui.theme.ExpenseColor
import com.knowspends.app.ui.theme.VyxelShapeTokens
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── 1. Backup & Restore ───────────────────────────────────────────────
        ExpressiveSettingsGroup(title = "Backup & Snapshot") {
            ExpressiveSettingsItem(
                title = "Create Full Backup (JSON)",
                subtitle = "Complete encrypted snapshot of accounts, budgets, and transactions",
                icon = Icons.Default.FileDownload,
                onClick = {
                    backupCreatePicker.launch("knowspends_backup_${System.currentTimeMillis()}.json")
                }
            )

            ExpressiveItemDivider()

            ExpressiveSettingsItem(
                title = "Restore Ledger from File",
                subtitle = "Load and restore data from a previous JSON backup file",
                icon = Icons.Default.Restore,
                onClick = {
                    restoreFilePicker.launch(arrayOf("application/json", "*/*"))
                }
            )

            ExpressiveItemDivider()

            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Scheduled Auto-Backup",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurface
                )
                Text(
                    text = "Automatically saves local snapshots to app internal storage",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant
                )

                val frequencies = listOf("off" to "Off", "daily" to "Daily", "weekly" to "Weekly")
                val freqIdx = frequencies.indexOfFirst { it.first == userPreferences.autoBackupFrequency }.coerceAtLeast(0)

                ExpressiveConnectedButtonGroup(
                    options = frequencies,
                    selectedIndex = freqIdx,
                    onSelectIndex = { idx ->
                        onSetAutoBackupFrequency(frequencies[idx].first)
                    },
                    labelProvider = { it.second },
                    iconProvider = { Icons.Default.Schedule }
                )
            }
        }

        // ── 2. CSV Import & Export ────────────────────────────────────────────
        ExpressiveSettingsGroup(title = "Statements & Spreadsheets") {
            ExpressiveSettingsItem(
                title = "Export All Transactions (CSV)",
                subtitle = "Save a comma-separated table compatible with Excel and Google Sheets",
                icon = Icons.Default.FileDownload,
                onClick = {
                    csvExportPicker.launch("knowspends_transactions_${System.currentTimeMillis()}.csv")
                }
            )

            ExpressiveItemDivider()

            ExpressiveSettingsItem(
                title = "Import Transactions from CSV",
                subtitle = "Import bank statements or exports from other budget apps",
                icon = Icons.Default.FileUpload,
                onClick = {
                    csvImportPicker.launch(arrayOf("text/csv", "text/comma-separated-values", "*/*"))
                }
            )
        }

        // ── 3. Cache Maintenance ──────────────────────────────────────────────
        ExpressiveSettingsGroup(title = "Maintenance") {
            ExpressiveSettingsItem(
                title = "Clear Application Cache",
                subtitle = "Cleans temporary release downloads without touching ledger data",
                icon = Icons.Default.CleaningServices,
                onClick = onClearCache
            )
        }

        // ── 4. Danger Zone ────────────────────────────────────────────────────
        ExpressiveSettingsGroup(
            title = "Danger Zone",
            containerColor = ExpenseColor.copy(alpha = 0.08f),
            borderColor = ExpenseColor.copy(alpha = 0.35f)
        ) {
            ExpressiveSettingsItem(
                title = "Erase All Ledger Data",
                subtitle = "Irreversibly wipe all accounts, transactions, and preferences",
                icon = Icons.Default.DeleteForever,
                iconTint = ExpenseColor,
                onClick = {
                    VyxelHaptics.heavy(context)
                    showResetConfirmDialog = true
                }
            )
        }

        Spacer(modifier = Modifier.height(VyxelNavBarBottomClearance).navigationBarsPadding())
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            icon = { Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = ExpenseColor) },
            title = { Text("Erase All Ledger Data?", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently delete all local transactions, budgets, subscriptions, and custom accounts. This cannot be undone.") },
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

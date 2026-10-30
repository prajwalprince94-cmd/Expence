package com.knowspends.app.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.knowspends.app.BuildConfig
import com.knowspends.app.ui.components.VyxelActionPill
import com.knowspends.app.ui.components.VyxelActionRow
import com.knowspends.app.ui.components.VyxelSettingsCard
import com.knowspends.app.ui.components.VyxelThemeSegmentedRow
import com.knowspends.app.ui.components.VyxelToggleRow
import com.knowspends.app.ui.premium.PremiumPaywallSheet
import com.knowspends.app.ui.theme.AppFont
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.ui.theme.fadingEdges
import com.knowspends.app.ui.theme.getFontFamily
import com.knowspends.app.ui.theme.pressScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val prefs = uiState.userPreferences
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    // SAF File Launchers
    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportBackup(it, context.contentResolver) }
    }

    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.requestRestore(it) }
    }

    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let { viewModel.exportCsv(it, context.contentResolver) }
    }

    val importCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importCsv(it, context.contentResolver) }
    }

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.updateProfile(
                name = prefs.profileName,
                tagline = prefs.profileTagline,
                avatarUri = uri.toString(),
                coverUri = prefs.profileCoverUri,
                currency = prefs.currencySymbol,
                savingsGoal = prefs.monthlySavingsGoal,
                financialYearStart = prefs.financialYearStart
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Settings & Vault",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .fadingEdges(topFade = 8.dp, bottomFade = 24.dp)
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Processing Indicator ──────────────────────────────────────────
            if (uiState.isLoading) {
                item(key = "loading") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = scheme.primaryContainer.copy(alpha = 0.6f)
                        ),
                        shape = VyxelShapeTokens.Card
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = "Processing data...",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // ── Undo Import Banner ────────────────────────────────────────────
            if (uiState.lastImportBatchId != null) {
                item(key = "undo_import") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = scheme.secondaryContainer),
                        shape = VyxelShapeTokens.Card
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Recent Import Active",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Imported ${uiState.lastImportCount ?: 0} transactions",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            OutlinedButton(onClick = { viewModel.undoLastImport() }) {
                                Icon(Icons.Default.Undo, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Undo")
                            }
                        }
                    }
                }
            }

            // ── 1. Integrated Profile Card (Vyxel Expressive Style) ───────────
            item(key = "profile_card") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, shape = VyxelShapeTokens.Card)
                        .clip(VyxelShapeTokens.Card)
                        .wrapContentHeight()
                ) {
                    // Cover Banner Background (Expressive gradient)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        scheme.primary.copy(alpha = 0.85f),
                                        scheme.secondary.copy(alpha = 0.70f),
                                        scheme.tertiary.copy(alpha = 0.65f)
                                    )
                                )
                            )
                    )

                    // Profile Content Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 70.dp)
                            .clip(VyxelShapeTokens.Card)
                            .background(scheme.surfaceContainerHigh)
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            // Circular Avatar
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(scheme.surfaceContainerHighest)
                                    .border(3.dp, scheme.surfaceContainerHigh, CircleShape)
                                    .clickable {
                                        avatarPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (prefs.profileAvatarUri.isNotBlank()) {
                                    AsyncImage(
                                        model = prefs.profileAvatarUri,
                                        contentDescription = "Avatar",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = scheme.primary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                            }

                            // Edit Button
                            VyxelActionPill(
                                text = "Edit Profile",
                                icon = Icons.Default.Edit,
                                onClick = { viewModel.showProfileDialog(true) }
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = prefs.profileName.ifBlank { "Personal Ledger" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface
                        )
                        Text(
                            text = prefs.profileTagline.ifBlank { "100% Offline Vault • Financial Privacy First" },
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(14.dp))

                        // Ledger Stats Auto-Layout Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Stat 1: Accounts
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(VyxelShapeTokens.CardSmall)
                                    .background(scheme.surfaceContainerHighest.copy(alpha = 0.5f))
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = scheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "${uiState.accountCount}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = scheme.onSurface
                                    )
                                }
                                Text(
                                    text = "Accounts",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = scheme.onSurfaceVariant
                                )
                            }

                            // Stat 2: Records
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(VyxelShapeTokens.CardSmall)
                                    .background(scheme.surfaceContainerHighest.copy(alpha = 0.5f))
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ReceiptLong,
                                        contentDescription = null,
                                        tint = scheme.secondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "${uiState.transactionCount}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = scheme.onSurface
                                    )
                                }
                                Text(
                                    text = "Records",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = scheme.onSurfaceVariant
                                )
                            }

                            // Stat 3: Monthly Goal
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(VyxelShapeTokens.CardSmall)
                                    .background(scheme.surfaceContainerHighest.copy(alpha = 0.5f))
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = scheme.tertiary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "${prefs.currencySymbol}${prefs.monthlySavingsGoal}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = scheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = "Savings Goal",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = scheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Secondary Info Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(VyxelShapeTokens.Pill)
                                    .background(scheme.primaryContainer.copy(alpha = 0.5f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Currency: ${prefs.currencySymbol}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = scheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(VyxelShapeTokens.Pill)
                                    .background(scheme.secondaryContainer.copy(alpha = 0.5f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "FY: ${prefs.financialYearStart}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = scheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(VyxelShapeTokens.Pill)
                                    .background(scheme.surfaceContainerHighest.copy(alpha = 0.6f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "100% Offline",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = scheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // ── 2. Appearance & Dynamic Color ─────────────────────────────────
            item(key = "appearance") {
                VyxelSettingsCard(title = "Appearance & Theme") {
                    Text(
                        text = "Theme Mode",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary
                    )
                    Spacer(Modifier.height(8.dp))

                    VyxelThemeSegmentedRow(
                        selectedTheme = prefs.themeMode,
                        onThemeSelected = { viewModel.setThemeMode(it) }
                    )

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(10.dp))

                    VyxelToggleRow(
                        title = "Material You Dynamic Colors",
                        subtitle = "Harmonize palette with your Android system wallpaper",
                        checked = prefs.dynamicColor,
                        onCheckedChange = { viewModel.setDynamicColor(it) },
                        icon = Icons.Default.Palette
                    )
                }
            }

            // ── 3. Multiple Fonts & Typography ────────────────────────────────
            item(key = "typography") {
                var fontsExpanded by remember { mutableStateOf(false) }

                VyxelSettingsCard(title = "Typography & Fonts") {
                    VyxelActionRow(
                        title = "Active Typeface",
                        subtitle = AppFont.entries.firstOrNull { it.id == prefs.fontId }?.displayName
                            ?: "Plus Jakarta Sans",
                        icon = Icons.Default.FontDownload,
                        onClick = { fontsExpanded = !fontsExpanded },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = scheme.onSurfaceVariant
                            )
                        }
                    )

                    AnimatedVisibility(visible = fontsExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AppFont.entries.forEach { font ->
                                val isSelected = prefs.fontId == font.id
                                val customFontFamily = getFontFamily(font.id)

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(VyxelShapeTokens.CardSmall)
                                        .background(
                                            if (isSelected) scheme.primaryContainer.copy(alpha = 0.6f)
                                            else Color.Transparent
                                        )
                                        .clickable {
                                            viewModel.setFontId(font.id)
                                            fontsExpanded = false
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = font.displayName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontFamily = customFontFamily,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) scheme.onPrimaryContainer else scheme.onSurface
                                        )
                                        Text(
                                            text = "₹12,450.00 • Groceries & Dining",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = customFontFamily,
                                            color = scheme.onSurfaceVariant
                                        )
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = scheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }


            // ── 5. Notifications & Daily Reminders ────────────────────────────
            item(key = "notifications") {
                var showTimePicker by remember { mutableStateOf(false) }

                VyxelSettingsCard(title = "Notifications & Reminders") {
                    VyxelToggleRow(
                        title = "Daily Review Reminder",
                        subtitle = "Alert to log daily expenses at ${prefs.dailyReminderTime}",
                        checked = prefs.dailyReminderEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.setDailyReminder(enabled, prefs.dailyReminderTime)
                        },
                        icon = Icons.Default.Notifications
                    )

                    if (prefs.dailyReminderEnabled) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Reminder Time:",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant
                            )
                            listOf("19:00", "20:00", "21:00", "22:00").forEach { presetTime ->
                                FilterChip(
                                    selected = prefs.dailyReminderTime == presetTime,
                                    onClick = {
                                        viewModel.setDailyReminder(true, presetTime)
                                    },
                                    label = { Text(presetTime) }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(10.dp))

                    VyxelToggleRow(
                        title = "Budget Threshold Alerts",
                        subtitle = "Warn when category spending approaches 80% or 100% of limits",
                        checked = prefs.budgetAlertEnabled,
                        onCheckedChange = { viewModel.setBudgetAlert(it) }
                    )
                }
            }

            // ── 6. Backup & Restore (Vyxel Style) ─────────────────────────────
            item(key = "backup_restore") {
                VyxelSettingsCard(title = "Backup & Data Safety") {
                    Text(
                        text = "Safeguard your entire offline ledger to local files or spreadsheets.",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        VyxelActionPill(
                            text = "Create Backup",
                            icon = Icons.Default.Backup,
                            onClick = {
                                val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
                                exportBackupLauncher.launch("knowspends-backup-$date.json")
                            },
                            isPrimary = true,
                            modifier = Modifier.weight(1f)
                        )

                        VyxelActionPill(
                            text = "Restore Data",
                            icon = Icons.Default.Restore,
                            onClick = {
                                restoreBackupLauncher.launch(
                                    arrayOf("application/json", "application/octet-stream", "*/*")
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        VyxelActionPill(
                            text = "Export CSV",
                            icon = Icons.Default.FileDownload,
                            onClick = {
                                val date = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US).format(java.util.Date())
                                exportCsvLauncher.launch("knowspends_transactions_$date.csv")
                            },
                            modifier = Modifier.weight(1f)
                        )

                        VyxelActionPill(
                            text = "Import CSV",
                            icon = Icons.Default.FileUpload,
                            onClick = {
                                importCsvLauncher.launch(
                                    arrayOf("text/csv", "text/comma-separated-values", "text/*", "*/*")
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── 7. Security & App Lock ────────────────────────────────────────
            item(key = "security") {
                VyxelSettingsCard(title = "Security & Privacy") {
                    VyxelToggleRow(
                        title = "Biometric / Device PIN Lock",
                        subtitle = "Require fingerprint, face, or system PIN to access ledger",
                        checked = uiState.securityConfig.isBiometricEnabled,
                        onCheckedChange = { viewModel.toggleBiometric(it) },
                        icon = Icons.Default.Security
                    )

                    if (uiState.securityConfig.isBiometricEnabled) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Auto-Lock:",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant
                            )
                            listOf(0 to "Immediate", 1 to "1m", 5 to "5m").forEach { (min, label) ->
                                FilterChip(
                                    selected = uiState.securityConfig.lockTimeoutMinutes == min,
                                    onClick = { viewModel.setLockTimeout(min) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(10.dp))

                    VyxelToggleRow(
                        title = "Hide in Recent Apps",
                        subtitle = "Prevent screenshots and mask screen in recent apps switcher (FLAG_SECURE)",
                        checked = uiState.securityConfig.isSecureScreenEnabled,
                        onCheckedChange = { viewModel.toggleSecureScreen(it) }
                    )
                }
            }

            // ── 8. Updates & Maintenance ──────────────────────────────────────
            item(key = "updates") {
                VyxelSettingsCard(title = "Updates & Version") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "KnowSpends Version",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurface
                            )
                            Text(
                                text = "Installed: ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant
                            )
                        }

                        if (uiState.isCheckingUpdate) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            VyxelActionPill(
                                text = "Check Now",
                                icon = Icons.Default.Update,
                                onClick = { viewModel.checkForUpdates() }
                            )
                        }
                    }
                }
            }

            // ── 9. Community & Support Group ──────────────────────────────────
            item(key = "community") {
                VyxelSettingsCard(title = "Support & Community") {
                    VyxelActionRow(
                        title = "Join Support Group",
                        subtitle = "Community discussions, feature ideas, and support",
                        icon = Icons.Default.Group,
                        onClick = {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://github.com/prajwalprince94-cmd/Expence/discussions")
                                )
                            )
                        },
                        trailingContent = {
                            Icon(Icons.Default.OpenInNew, null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    )

                    Spacer(Modifier.height(6.dp))
                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(6.dp))

                    VyxelActionRow(
                        title = "Report an Issue / Bug",
                        subtitle = "Submit issue report or feature request on GitHub",
                        icon = Icons.Default.BugReport,
                        onClick = {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://github.com/prajwalprince94-cmd/Expence/issues")
                                )
                            )
                        },
                        trailingContent = {
                            Icon(Icons.Default.OpenInNew, null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    )
                }
            }

            // ── 10. About & Architecture ──────────────────────────────────────
            item(key = "about") {
                VyxelSettingsCard(title = "About KnowSpends") {
                    Text(
                        text = "KnowSpends • Private Personal Ledger",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Strict financial privacy: 100% local-first database, Room Single Source of Truth (SSOT), and fluid Material 3 Expressive UI. Developed with passion by Prajwal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))

                    VyxelActionPill(
                        text = "GitHub Repository",
                        icon = Icons.Default.OpenInNew,
                        onClick = {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://github.com/prajwalprince94-cmd/Expence")
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item(key = "bottom_spacer") {
                Spacer(Modifier.height(100.dp))
            }
        }
    }

    // ── Edit Profile Dialog ───────────────────────────────────────────────────
    if (uiState.showProfileEditDialog) {
        var editName by remember { mutableStateOf(prefs.profileName) }
        var editTagline by remember { mutableStateOf(prefs.profileTagline) }
        var editCurrency by remember { mutableStateOf(prefs.currencySymbol) }
        var editSavingsGoal by remember { mutableStateOf(prefs.monthlySavingsGoal) }
        var editFyStart by remember { mutableStateOf(prefs.financialYearStart) }

        AlertDialog(
            onDismissRequest = { viewModel.showProfileDialog(false) },
            title = { Text("Edit Profile & Ledger") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Ledger / User Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editTagline,
                        onValueChange = { editTagline = it },
                        label = { Text("Tagline / Subtitle") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editCurrency,
                        onValueChange = { editCurrency = it },
                        label = { Text("Currency Symbol (₹, $, €, £)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editSavingsGoal,
                        onValueChange = { editSavingsGoal = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Monthly Savings Target") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editFyStart,
                        onValueChange = { editFyStart = it },
                        label = { Text("Financial Year Start (e.g. April)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfile(
                            name = editName.trim(),
                            tagline = editTagline.trim(),
                            avatarUri = prefs.profileAvatarUri,
                            coverUri = prefs.profileCoverUri,
                            currency = editCurrency.trim().ifBlank { "₹" },
                            savingsGoal = editSavingsGoal.trim().ifBlank { "15000" },
                            financialYearStart = editFyStart.trim().ifBlank { "April" }
                        )
                    }
                ) {
                    Text("Save Profile")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showProfileDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ── Update Alert Pop Dialog ───────────────────────────────────────────────
    if (uiState.showUpdateDialog) {
        val result = uiState.updateCheckResult
        val hasUpdate = result?.hasUpdate == true || prefs.updateAvailable
        val latestVer = result?.latestVersion?.ifBlank { prefs.latestVersionName } ?: prefs.latestVersionName
        val releaseNotes = result?.releaseNotes?.ifBlank { prefs.latestReleaseNotes } ?: prefs.latestReleaseNotes
        val downloadUrl = (result?.downloadUrl ?: prefs.latestReleaseUrl).ifBlank {
            "https://github.com/prajwalprince94-cmd/Expence/releases"
        }

        AlertDialog(
            onDismissRequest = { viewModel.dismissUpdateDialog() },
            icon = {
                Icon(
                    imageVector = if (hasUpdate) Icons.Default.CloudDownload else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (hasUpdate) scheme.primary else scheme.secondary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = if (hasUpdate) "Update Available!" else "You're Up to Date",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (hasUpdate) {
                        Text(
                            text = "A newer version of KnowSpends ($latestVer) is available! Your installed version is ${BuildConfig.VERSION_NAME}.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (releaseNotes.isNotBlank()) {
                            Text(
                                text = releaseNotes,
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant,
                                maxLines = 6,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Text(
                            text = "KnowSpends is running the latest version (${BuildConfig.VERSION_NAME}). Your personal ledger is optimized and secure.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                if (hasUpdate) {
                    Button(
                        onClick = {
                            viewModel.dismissUpdateDialog()
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)))
                        }
                    ) {
                        Text("Download $latestVer")
                    }
                } else {
                    Button(onClick = { viewModel.dismissUpdateDialog() }) {
                        Text("Got it")
                    }
                }
            },
            dismissButton = {
                if (hasUpdate) {
                    TextButton(onClick = { viewModel.dismissUpdateDialog() }) {
                        Text("Later")
                    }
                }
            }
        )
    }

    // ── Restore Confirmation Dialog ───────────────────────────────────────────
    if (uiState.showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelRestore() },
            title = { Text("Confirm Data Restore") },
            text = {
                Text("Restoring from this backup file will replace existing accounts, transactions, and categories with the backup data. Do you wish to proceed?")
            },
            confirmButton = {
                Button(onClick = { viewModel.confirmRestore(context.contentResolver) }) {
                    Text("Restore Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelRestore() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ── Pro Paywall Sheet ─────────────────────────────────────────────────────
    if (uiState.showPaywallSheet) {
        PremiumPaywallSheet(
            onDismiss = { viewModel.hidePaywall() },
            onUpgradeClicked = { viewModel.upgradeToPremium() }
        )
    }
}

package com.knowspends.app.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
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
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Vibration
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
import com.knowspends.app.ui.theme.GradientPresets
import com.knowspends.app.ui.theme.VyxelShapeTokens
import com.knowspends.app.ui.theme.fadingEdges
import com.knowspends.app.ui.theme.getFontFamily
import com.knowspends.app.ui.theme.pressScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateToBudgets: () -> Unit = {},
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

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val receiveGranted = permissions[Manifest.permission.RECEIVE_SMS] == true
        val readGranted = permissions[Manifest.permission.READ_SMS] == true
        if (receiveGranted || readGranted) {
            viewModel.setSmsSettings(true, prefs.smsDirectAutoAdd)
        }
    }

    val hasSmsPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.RECEIVE_SMS
    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
        context, Manifest.permission.READ_SMS
    ) == PackageManager.PERMISSION_GRANTED

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
                    val coverBrush = when (prefs.profileCoverUri) {
                        "gradient:ocean" -> GradientPresets.Ocean
                        "gradient:sunset" -> GradientPresets.Sunset
                        "gradient:amethyst" -> GradientPresets.Amethyst
                        "gradient:midnight" -> GradientPresets.Midnight
                        else -> Brush.linearGradient(
                            listOf(
                                scheme.primary.copy(alpha = 0.85f),
                                scheme.secondary.copy(alpha = 0.70f),
                                scheme.tertiary.copy(alpha = 0.65f)
                            )
                        )
                    }

                    // Cover Banner Background (Expressive gradient)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(128.dp)
                            .background(coverBrush)
                    )

                    // Profile Content Card
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 70.dp)
                            .clip(VyxelShapeTokens.Card)
                            .background(scheme.surfaceContainerLow)
                            .border(1.dp, scheme.outlineVariant.copy(alpha = 0.7f), VyxelShapeTokens.Card)
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
                                    .border(3.dp, scheme.surfaceContainerLow, CircleShape)
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

                        Spacer(Modifier.height(10.dp))

                        // Customization Attributes Row (Currency, Target, FY)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(scheme.surfaceContainerHighest)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = prefs.currencySymbol,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.primary
                                )
                                Text(
                                    text = "Currency",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = scheme.onSurfaceVariant
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(scheme.surfaceContainerHighest)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Goal:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = scheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${prefs.currencySymbol}${prefs.monthlySavingsGoal}/mo",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.secondary
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(scheme.surfaceContainerHighest)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "FY: ${prefs.financialYearStart}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = scheme.onSurfaceVariant
                                )
                            }
                        }

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
                                    .border(1.dp, scheme.outlineVariant.copy(alpha = 0.6f), VyxelShapeTokens.CardSmall)
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
                                    .border(1.dp, scheme.outlineVariant.copy(alpha = 0.6f), VyxelShapeTokens.CardSmall)
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
                                    .border(1.dp, scheme.outlineVariant.copy(alpha = 0.6f), VyxelShapeTokens.CardSmall)
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

            // ── 1.5 Budgets & Limits (Moved from bottom navigation) ───────────
            item(key = "budgets_limits") {
                VyxelSettingsCard(title = "Budgets & Limits") {
                    VyxelActionRow(
                        title = "Monthly Budgets & Category Limits",
                        subtitle = "Track envelope spending, monitor budget health, and rollover unspent savings",
                        icon = Icons.Default.AccountBalanceWallet,
                        onClick = onNavigateToBudgets
                    )
                }
            }

            // ── 1.8 SMS Bank & UPI Auto-Tracking ─────────────────────────────
            item(key = "sms_auto_tracking") {
                VyxelSettingsCard(title = "SMS Bank & UPI Auto-Tracking") {
                    VyxelToggleRow(
                        title = "Auto-Capture Incoming SMS",
                        subtitle = "Parse debit, credit, UPI, and wallet transactions in real-time from bank alerts",
                        checked = prefs.smsAutoCaptureEnabled && hasSmsPermission,
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                if (hasSmsPermission) {
                                    viewModel.setSmsSettings(true, prefs.smsDirectAutoAdd)
                                } else {
                                    smsPermissionLauncher.launch(
                                        arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
                                    )
                                }
                            } else {
                                viewModel.setSmsSettings(false, prefs.smsDirectAutoAdd)
                            }
                        },
                        icon = Icons.Default.Sms
                    )

                    if (prefs.smsAutoCaptureEnabled && hasSmsPermission) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Capture Mode:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = scheme.primary
                        )
                        Spacer(Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = !prefs.smsDirectAutoAdd,
                                onClick = { viewModel.setSmsSettings(true, false) },
                                label = { Text("Smart Review Inbox") },
                                shape = VyxelShapeTokens.Pill
                            )
                            FilterChip(
                                selected = prefs.smsDirectAutoAdd,
                                onClick = { viewModel.setSmsSettings(true, true) },
                                label = { Text("Direct Auto-Add") },
                                shape = VyxelShapeTokens.Pill
                            )
                        }

                        Text(
                            text = if (prefs.smsDirectAutoAdd)
                                "Transactions are recorded directly to ledger accounts immediately."
                            else
                                "Transactions arrive in the Review Inbox where you can verify payee and category before approving.",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.3f))
                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Scan Past SMS",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Import transactions from the past 30 days of SMS messages",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = scheme.onSurfaceVariant
                                )
                            }
                            if (uiState.isScanningSms) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                VyxelActionPill(
                                    text = "Scan 30 Days",
                                    icon = Icons.Default.Sync,
                                    onClick = { viewModel.scanPastSms(30) }
                                )
                            }
                        }
                    } else if (!hasSmsPermission) {
                        Spacer(Modifier.height(8.dp))
                        VyxelActionPill(
                            text = "Grant SMS Permission",
                            icon = Icons.Default.Security,
                            onClick = {
                                smsPermissionLauncher.launch(
                                    arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
                                )
                            },
                            isPrimary = true,
                            modifier = Modifier.fillMaxWidth()
                        )
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

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(10.dp))

                    VyxelActionRow(
                        title = "Default Ledger Currency",
                        subtitle = "Active: ${prefs.currencySymbol} • Tap to switch between INR (₹), USD ($), EUR (€), GBP (£), and more",
                        icon = Icons.Default.CurrencyExchange,
                        onClick = { viewModel.showCurrencyDialog(true) }
                    )

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(10.dp))

                    VyxelToggleRow(
                        title = "Tactile Haptic Feedback",
                        subtitle = "Subtle vibration sensations when tapping buttons, keypad inputs, and transactions",
                        checked = prefs.hapticsEnabled,
                        onCheckedChange = { viewModel.setHapticsEnabled(it) },
                        icon = Icons.Default.Vibration
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


            // ── 5. Notifications & Budget Alerts ────────────────────────────
            item(key = "notifications") {
                VyxelSettingsCard(title = "Notifications & Budget Alerts") {
                    VyxelToggleRow(
                        title = "Budget Threshold Alerts",
                        subtitle = "Warn when category spending approaches 80% or 100% of defined limits",
                        checked = prefs.budgetAlertEnabled,
                        onCheckedChange = { viewModel.setBudgetAlert(it) },
                        icon = Icons.Default.Notifications
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
                                    label = { Text(label) },
                                    shape = VyxelShapeTokens.Pill
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(12.dp))

                    VyxelToggleRow(
                        title = "Hide in Recent Apps",
                        subtitle = "Prevent screenshots and mask screen in recent apps switcher (FLAG_SECURE)",
                        checked = uiState.securityConfig.isSecureScreenEnabled,
                        onCheckedChange = { viewModel.toggleSecureScreen(it) }
                    )
                }
            }

            // ── Tap to Add & Dynamic Island ──────────────────────────
            item(key = "dynamic_island") {
                VyxelSettingsCard(title = "Tap to Add & Dynamic Island") {
                    VyxelToggleRow(
                        title = "Back-Tap Dynamic Island",
                        subtitle = "Double-tap physical back of phone to trigger iOS Dynamic Island quick spend popup (90fps Framer Motion)",
                        checked = uiState.userPreferences.backTapEnabled,
                        onCheckedChange = { viewModel.setBackTapSettings(it, uiState.userPreferences.backTapSensitivity) },
                        icon = Icons.Default.TouchApp
                    )

                    if (uiState.userPreferences.backTapEnabled) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sensitivity:",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant
                            )
                            listOf("high" to "High", "normal" to "Normal", "low" to "Low").forEach { (key, label) ->
                                FilterChip(
                                    selected = uiState.userPreferences.backTapSensitivity == key,
                                    onClick = { viewModel.setBackTapSettings(true, key) },
                                    label = { Text(label) },
                                    shape = VyxelShapeTokens.Pill
                                )
                            }
                        }
                    }
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
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
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

                    if (uiState.isDownloadingUpdate) {
                        Spacer(Modifier.height(12.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { uiState.downloadProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Downloading in-app update...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = scheme.primary
                                )
                                Text(
                                    text = "${(uiState.downloadProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.primary
                                )
                            }
                        }
                    } else if (uiState.downloadedApkUri != null) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Update downloaded & ready!",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                            Button(
                                onClick = { viewModel.installDownloadedApk(context) },
                                shape = VyxelShapeTokens.Pill
                            ) {
                                Text("Install Now")
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Temporary Storage & Cache",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurface
                            )
                            Text(
                                text = "Clean downloaded update APKs, cached receipts, and export files",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant
                            )
                        }
                        VyxelActionPill(
                            text = "Clean Cache",
                            icon = Icons.Default.CleaningServices,
                            onClick = { viewModel.clearAppCache(context) }
                        )
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

            // ── 11. Danger Zone ──────────────────────────────────────────────
            item(key = "danger_zone") {
                VyxelSettingsCard(title = "Danger Zone") {
                    Text(
                        text = "Irreversible ledger operations. Proceed with extreme caution.",
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.error
                    )
                    Spacer(Modifier.height(10.dp))
                    VyxelActionRow(
                        title = "Reset All Ledger Data",
                        subtitle = "Permanently wipe all transactions, accounts, and categories, restoring factory defaults",
                        icon = Icons.Default.DeleteForever,
                        onClick = { viewModel.showResetConfirmDialog(true) },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = scheme.error
                            )
                        }
                    )
                }
            }

            item(key = "bottom_spacer") {
                Spacer(Modifier.height(100.dp))
            }
        }
    }

    // ── Edit Profile Dialog (Rich Customization Features) ───────────────────
    if (uiState.showProfileEditDialog) {
        var editName by remember { mutableStateOf(prefs.profileName) }
        var editTagline by remember { mutableStateOf(prefs.profileTagline) }
        var editCurrency by remember { mutableStateOf(prefs.currencySymbol) }
        var editSavingsGoal by remember { mutableStateOf(prefs.monthlySavingsGoal) }
        var editFyStart by remember { mutableStateOf(prefs.financialYearStart) }
        var editCoverUri by remember { mutableStateOf(prefs.profileCoverUri.ifBlank { "gradient:emerald" }) }

        val gradientThemes = listOf(
            "gradient:emerald" to "Emerald",
            "gradient:ocean" to "Ocean",
            "gradient:sunset" to "Sunset",
            "gradient:amethyst" to "Amethyst",
            "gradient:midnight" to "Midnight"
        )
        val currencyPresets = listOf("₹", "$", "€", "£", "¥", "₩")
        val goalPresets = listOf("5000", "10000", "15000", "25000", "50000", "100000")

        AlertDialog(
            onDismissRequest = { viewModel.showProfileDialog(false) },
            containerColor = scheme.surfaceContainerLow,
            title = {
                Text(
                    text = "Edit Profile & Ledger",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Cover Theme Presets
                    Text(
                        text = "Cover Gradient Theme",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        gradientThemes.forEach { (key, label) ->
                            FilterChip(
                                selected = editCoverUri == key,
                                onClick = { editCoverUri = key },
                                label = { Text(label) },
                                shape = VyxelShapeTokens.Pill
                            )
                        }
                    }

                    // 2. Name & Tagline
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Ledger / User Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )
                    OutlinedTextField(
                        value = editTagline,
                        onValueChange = { editTagline = it },
                        label = { Text("Tagline / Subtitle") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )

                    // 3. Currency Quick-Selector
                    Text(
                        text = "Currency Symbol",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currencyPresets.forEach { symbol ->
                            FilterChip(
                                selected = editCurrency == symbol,
                                onClick = { editCurrency = symbol },
                                label = { Text(symbol, fontWeight = FontWeight.Bold) },
                                shape = VyxelShapeTokens.Pill
                            )
                        }
                    }
                    OutlinedTextField(
                        value = editCurrency,
                        onValueChange = { editCurrency = it },
                        label = { Text("Custom Currency Symbol") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    )

                    // 4. Monthly Savings Target Presets
                    Text(
                        text = "Monthly Savings Target",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = scheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        goalPresets.forEach { preset ->
                            FilterChip(
                                selected = editSavingsGoal == preset,
                                onClick = { editSavingsGoal = preset },
                                label = { Text(preset) },
                                shape = VyxelShapeTokens.Pill
                            )
                        }
                    }
                    OutlinedTextField(
                        value = editSavingsGoal,
                        onValueChange = { editSavingsGoal = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Target Amount") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    // 5. Financial Year Start
                    OutlinedTextField(
                        value = editFyStart,
                        onValueChange = { editFyStart = it },
                        label = { Text("Financial Year Start (e.g. April)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
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
                            coverUri = editCoverUri,
                            currency = editCurrency.trim().ifBlank { "₹" },
                            savingsGoal = editSavingsGoal.trim().ifBlank { "15000" },
                            financialYearStart = editFyStart.trim().ifBlank { "April" }
                        )
                    },
                    shape = VyxelShapeTokens.Pill
                ) {
                    Text("Save Profile", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showProfileDialog(false) }) {
                    Text("Cancel")
                }
            },
            shape = MaterialTheme.shapes.extraLarge
        )
    }

    // ── Update Alert Pop Dialog (100% In-App Download & Installation) ─────────
    if (uiState.showUpdateDialog) {
        val result = uiState.updateCheckResult
        val hasUpdate = result?.hasUpdate == true || prefs.updateAvailable
        val latestVer = result?.latestVersion?.ifBlank { prefs.latestVersionName } ?: prefs.latestVersionName
        val releaseNotes = result?.releaseNotes?.ifBlank { prefs.latestReleaseNotes } ?: prefs.latestReleaseNotes
        val downloadUrl = (result?.downloadUrl ?: prefs.latestReleaseUrl).ifBlank {
            "https://github.com/prajwalprince94-cmd/Expence/releases"
        }

        AlertDialog(
            onDismissRequest = {
                if (!uiState.isDownloadingUpdate) viewModel.dismissUpdateDialog()
            },
            containerColor = scheme.surfaceContainerLow,
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
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                maxLines = 5,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (uiState.isDownloadingUpdate) {
                            Spacer(Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { uiState.downloadProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Downloading in-app update...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = scheme.primary
                                )
                                Text(
                                    text = "${(uiState.downloadProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.primary
                                )
                            }
                        } else if (uiState.downloadedApkUri != null) {
                            Text(
                                text = "Update downloaded! Tap Install Now to update immediately.",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else if (uiState.downloadError != null) {
                            Text(
                                text = "Download failed: ${uiState.downloadError}",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.error
                            )
                        }
                    } else {
                        Text(
                            text = "KnowSpends is running the latest version (${BuildConfig.VERSION_NAME}). Your personal ledger is optimized, private, and up to date.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                if (hasUpdate) {
                    if (uiState.downloadedApkUri != null) {
                        Button(
                            onClick = {
                                viewModel.installDownloadedApk(context)
                            },
                            shape = VyxelShapeTokens.Pill
                        ) {
                            Text("Install Now", fontWeight = FontWeight.Bold)
                        }
                    } else if (uiState.isDownloadingUpdate) {
                        Button(
                            onClick = {},
                            enabled = false,
                            shape = VyxelShapeTokens.Pill
                        ) {
                            Text("Downloading ${(uiState.downloadProgress * 100).toInt()}%")
                        }
                    } else {
                        Button(
                            onClick = {
                                viewModel.downloadAndInstallUpdate(context, downloadUrl)
                            },
                            shape = VyxelShapeTokens.Pill
                        ) {
                            Text("In-App Update ($latestVer)", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = { viewModel.dismissUpdateDialog() },
                        shape = VyxelShapeTokens.Pill
                    ) {
                        Text("Got it")
                    }
                }
            },
            dismissButton = {
                if (hasUpdate && !uiState.isDownloadingUpdate) {
                    TextButton(onClick = { viewModel.dismissUpdateDialog() }) {
                        Text("Later")
                    }
                }
            },
            shape = MaterialTheme.shapes.extraLarge
        )
    }

    // ── Restore Confirmation Dialog ───────────────────────────────────────────
    if (uiState.showRestoreConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelRestore() },
            containerColor = scheme.surfaceContainerLow,
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

    // ── Currency Quick-Selector Dialog ────────────────────────────────────────
    if (uiState.showCurrencyDialog) {
        val currencies = listOf(
            "₹" to "INR - Indian Rupee",
            "$" to "USD - US Dollar",
            "€" to "EUR - Euro",
            "£" to "GBP - British Pound",
            "¥" to "JPY / CNY - Japanese Yen / Yuan",
            "C$" to "CAD - Canadian Dollar",
            "A$" to "AUD - Australian Dollar",
            "₩" to "KRW - Korean Won",
            "د.إ" to "AED - UAE Dirham",
            "﷼" to "SAR - Saudi Riyal",
            "S$" to "SGD - Singapore Dollar",
            "CHF" to "CHF - Swiss Franc"
        )
        var customCurrency by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { viewModel.showCurrencyDialog(false) },
            containerColor = scheme.surfaceContainerLow,
            title = {
                Text(
                    text = "Select Primary Currency",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currencies.forEach { (symbol, label) ->
                        val isSelected = prefs.currencySymbol == symbol
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(VyxelShapeTokens.CardSmall)
                                .background(
                                    if (isSelected) scheme.primaryContainer.copy(alpha = 0.6f)
                                    else Color.Transparent
                                )
                                .clickable { viewModel.setCurrency(symbol) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$symbol  $label",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) scheme.onPrimaryContainer else scheme.onSurface
                            )
                            if (isSelected) {
                                Icon(Icons.Default.Check, null, tint = scheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Or enter custom symbol:",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = customCurrency,
                            onValueChange = { customCurrency = it },
                            placeholder = { Text("Symbol") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                if (customCurrency.isNotBlank()) {
                                    viewModel.setCurrency(customCurrency.trim())
                                }
                            },
                            enabled = customCurrency.isNotBlank()
                        ) {
                            Text("Set")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.showCurrencyDialog(false) }) {
                    Text("Close")
                }
            },
            shape = MaterialTheme.shapes.extraLarge
        )
    }

    // ── Reset Ledger Confirmation Dialog ──────────────────────────────────────
    if (uiState.showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showResetConfirmDialog(false) },
            containerColor = scheme.surfaceContainerLow,
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = scheme.error,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Erase All Ledger Data?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = scheme.error
                )
            },
            text = {
                Text(
                    text = "This will permanently delete all recorded transactions, accounts, and categories, restoring default templates. This action CANNOT be undone.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.resetAllLedgerData() },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = scheme.error,
                        contentColor = scheme.onError
                    ),
                    shape = VyxelShapeTokens.Pill
                ) {
                    Text("Erase & Reset", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showResetConfirmDialog(false) }) {
                    Text("Cancel")
                }
            },
            shape = MaterialTheme.shapes.extraLarge
        )
    }
}

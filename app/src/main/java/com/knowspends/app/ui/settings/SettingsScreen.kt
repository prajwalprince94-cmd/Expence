package com.knowspends.app.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.core.content.ContextCompat
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Rule
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
import androidx.compose.material3.Surface
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

import com.knowspends.app.ui.components.VyxelExpressiveChip
import com.knowspends.app.ui.components.bouncyClickable
import com.knowspends.app.ui.theme.IncomeColor
import com.knowspends.app.ui.theme.progressiveScrollBlur

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateToBudgets: () -> Unit = {},
    onNavigateToSubscriptions: () -> Unit = {},
    onNavigateToMerchantRules: () -> Unit = {},
    onNavigateToInbox: () -> Unit = {},
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

    var hasSmsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        )
    }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val receiveGranted = permissions[Manifest.permission.RECEIVE_SMS] == true
        val readGranted = permissions[Manifest.permission.READ_SMS] == true
        if (receiveGranted || readGranted) {
            hasSmsPermission = true
            viewModel.setSmsSettings(true, prefs.smsDirectAutoAdd)
        } else {
            hasSmsPermission = false
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                hasSmsPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 92.dp, start = 16.dp, end = 16.dp)
            )
        },
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
                .progressiveScrollBlur(topBlur = 12.dp, bottomBlur = 48.dp, scrimColor = scheme.background)
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

            // ── 1. Modern Financial Profile Card (Copilot & Open-Source Style) ──
            item(key = "profile_card") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pressScale(0.99f),
                    shape = VyxelShapeTokens.Card,
                    colors = CardDefaults.cardColors(
                        containerColor = scheme.surfaceContainerLow
                    ),
                    border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // User Profile Header: Avatar + Identity + Edit Action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Circular Avatar with Tap to Pick Image
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(scheme.primaryContainer)
                                    .border(1.5.dp, scheme.primary.copy(alpha = 0.45f), CircleShape)
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
                                        contentDescription = "Profile Avatar",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    val initials = prefs.profileName.trim().take(1).uppercase().ifBlank { "P" }
                                    Text(
                                        text = initials,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = scheme.onPrimaryContainer
                                    )
                                }
                            }

                            // Identity details
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = prefs.profileName.ifBlank { "Personal Ledger" },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = prefs.profileTagline.ifBlank { "Offline Vault • Financial Privacy First" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = scheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Edit Action Button
                            Surface(
                                onClick = { viewModel.showProfileDialog(true) },
                                shape = VyxelShapeTokens.Pill,
                                color = scheme.surfaceContainerHighest,
                                border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.bouncyClickable { viewModel.showProfileDialog(true) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Profile",
                                        tint = scheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Edit",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = scheme.primary
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.35f))

                        // Ledger Financial Health Stats Ribbon (Auto-Layout 3 Columns)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Stat 1: Accounts
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(VyxelShapeTokens.CardSmall)
                                    .background(scheme.surfaceContainerLowest)
                                    .border(1.dp, scheme.outlineVariant.copy(alpha = 0.35f), VyxelShapeTokens.CardSmall)
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "${uiState.accountCount}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.onSurface
                                )
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
                                    .background(scheme.surfaceContainerLowest)
                                    .border(1.dp, scheme.outlineVariant.copy(alpha = 0.35f), VyxelShapeTokens.CardSmall)
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "${uiState.transactionCount}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = scheme.onSurface
                                )
                                Text(
                                    text = "Records",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = scheme.onSurfaceVariant
                                )
                            }

                            // Stat 3: Monthly Savings Target
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(VyxelShapeTokens.CardSmall)
                                    .background(scheme.surfaceContainerLowest)
                                    .border(1.dp, scheme.outlineVariant.copy(alpha = 0.35f), VyxelShapeTokens.CardSmall)
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "${prefs.currencySymbol}${prefs.monthlySavingsGoal}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = IncomeColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Target Goal",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = scheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // ── 1. Automation & Quick Capture ────────────────────────────────
            item(key = "automation_capture") {
                val canDrawOverlays = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)

                VyxelSettingsCard(title = "Automation & Quick Capture") {
                    // SMS Auto-Capture
                    VyxelToggleRow(
                        title = "Auto-Capture Incoming SMS",
                        subtitle = "Parse debit, credit, UPI, and bank alerts in real-time",
                        checked = prefs.smsAutoCaptureEnabled,
                        onCheckedChange = { isChecked ->
                            com.knowspends.app.util.VyxelHaptics.tick(context)
                            viewModel.setSmsSettings(isChecked, prefs.smsDirectAutoAdd)
                            if (isChecked && !hasSmsPermission) {
                                smsPermissionLauncher.launch(
                                    arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
                                )
                            }
                        },
                        icon = Icons.Default.Sms
                    )

                    if (!hasSmsPermission && prefs.smsAutoCaptureEnabled) {
                        Spacer(Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = scheme.errorContainer.copy(alpha = 0.35f)
                            ),
                            shape = VyxelShapeTokens.CardSmall
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SMS Permission Required",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = scheme.error,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                VyxelActionPill(
                                    text = "Grant",
                                    icon = Icons.Default.Security,
                                    onClick = {
                                        smsPermissionLauncher.launch(
                                            arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
                                        )
                                    },
                                    isPrimary = true
                                )
                            }
                        }
                    }

                    if (prefs.smsAutoCaptureEnabled) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mode:",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant
                            )
                            VyxelExpressiveChip(
                                selected = !prefs.smsDirectAutoAdd,
                                onClick = { viewModel.setSmsSettings(true, false) },
                                label = "Review Inbox"
                            )
                            VyxelExpressiveChip(
                                selected = prefs.smsDirectAutoAdd,
                                onClick = { viewModel.setSmsSettings(true, true) },
                                label = "Direct Auto-Add"
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                        VyxelActionRow(
                            title = "Review Inbox",
                            subtitle = "Verify and approve detected transactions",
                            icon = Icons.Default.Inbox,
                            onClick = onNavigateToInbox
                        )

                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sync Past 30 Days SMS",
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.onSurface
                            )
                            if (uiState.isScanningSms) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            } else {
                                VyxelActionPill(
                                    text = "Scan Now",
                                    icon = Icons.Default.Sync,
                                    onClick = {
                                        if (hasSmsPermission) {
                                            viewModel.scanPastSms(30)
                                        } else {
                                            smsPermissionLauncher.launch(
                                                arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(10.dp))

                    // Dynamic Island & Back-Tap Quick Add
                    VyxelToggleRow(
                        title = "Back-Tap Dynamic Island",
                        subtitle = "Double-tap device back to summon quick spend popup",
                        checked = prefs.backTapEnabled,
                        onCheckedChange = { viewModel.setBackTapSettings(it, prefs.backTapSensitivity) },
                        icon = Icons.Default.TouchApp
                    )

                    if (prefs.backTapEnabled) {
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
                                VyxelExpressiveChip(
                                    selected = prefs.backTapSensitivity == key,
                                    onClick = { viewModel.setBackTapSettings(true, key) },
                                    label = label
                                )
                            }
                        }

                        if (!canDrawOverlays) {
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                onClick = {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                },
                                shape = VyxelShapeTokens.CardSmall,
                                color = scheme.primaryContainer.copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, scheme.primary.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInNew,
                                        contentDescription = null,
                                        tint = scheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Enable 'Appear on top' permission to popup over other apps",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = scheme.onPrimaryContainer,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = scheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(10.dp))

                    // Merchant Rules & Memory
                    VyxelActionRow(
                        title = "Merchant Rules & Category Memory",
                        subtitle = "Auto-assign categories from payee history",
                        icon = Icons.Default.Rule,
                        onClick = onNavigateToMerchantRules
                    )

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(10.dp))

                    // Recurring Bills & Subscriptions
                    VyxelActionRow(
                        title = "Recurring Bills & Subscriptions",
                        subtitle = "Manage streaming, utilities, and renewals",
                        icon = Icons.Default.Repeat,
                        onClick = onNavigateToSubscriptions
                    )

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(10.dp))

                    // Monthly Budgets
                    VyxelActionRow(
                        title = "Budgets & Category Limits",
                        subtitle = "Envelope tracking and spending caps",
                        icon = Icons.Default.AccountBalanceWallet,
                        onClick = onNavigateToBudgets
                    )
                }
            }

            // ── 2. Preferences ────────────────────────────────────────────────
            item(key = "preferences") {
                VyxelSettingsCard(title = "Preferences") {
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

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(10.dp))

                    VyxelToggleRow(
                        title = "Material You Dynamic Colors",
                        subtitle = "Harmonize palette with device wallpaper",
                        checked = prefs.dynamicColor,
                        onCheckedChange = { viewModel.setDynamicColor(it) },
                        icon = Icons.Default.Palette
                    )

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(10.dp))

                    VyxelActionRow(
                        title = "Default Currency",
                        subtitle = "Active: ${prefs.currencySymbol} • Tap to switch currency symbol",
                        icon = Icons.Default.CurrencyExchange,
                        onClick = { viewModel.showCurrencyDialog(true) }
                    )

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(10.dp))

                    VyxelToggleRow(
                        title = "Tactile Haptic Feedback",
                        subtitle = "Subtle vibration sensations when interacting",
                        checked = prefs.hapticsEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.setHapticsEnabled(enabled)
                            if (enabled) com.knowspends.app.util.VyxelHaptics.success(context)
                        },
                        icon = Icons.Default.Vibration
                    )

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(10.dp))

                    VyxelToggleRow(
                        title = "Budget Threshold Alerts",
                        subtitle = "Warn when spending approaches 80% or 100% of limits",
                        checked = prefs.budgetAlertEnabled,
                        onCheckedChange = { viewModel.setBudgetAlert(it) },
                        icon = Icons.Default.Notifications
                    )
                }
            }

            // ── 3. Data & Security ────────────────────────────────────────────
            item(key = "data_security") {
                VyxelSettingsCard(title = "Data & Security") {
                    VyxelToggleRow(
                        title = "Biometric / Device PIN Lock",
                        subtitle = "Require fingerprint or system PIN to access ledger",
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

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(10.dp))

                    VyxelToggleRow(
                        title = "Hide in Recent Apps",
                        subtitle = "Mask ledger preview in task switcher (FLAG_SECURE)",
                        checked = uiState.securityConfig.isSecureScreenEnabled,
                        onCheckedChange = { viewModel.toggleSecureScreen(it) }
                    )

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "Backup & Export",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = scheme.primary
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        VyxelActionPill(
                            text = "Backup JSON",
                            icon = Icons.Default.Backup,
                            onClick = {
                                val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
                                exportBackupLauncher.launch("knowspends-backup-$date.json")
                            },
                            isPrimary = true,
                            modifier = Modifier.weight(1f)
                        )

                        VyxelActionPill(
                            text = "Restore JSON",
                            icon = Icons.Default.Restore,
                            onClick = {
                                restoreBackupLauncher.launch(
                                    arrayOf("application/json", "application/octet-stream", "*/*")
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

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

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(10.dp))

                    VyxelActionRow(
                        title = "Reset All Ledger Data",
                        subtitle = "Permanently wipe all records and restore factory defaults",
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

            // ── 4. About & Updates ────────────────────────────────────────────
            item(key = "about_updates") {
                VyxelSettingsCard(title = "About & Updates") {
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
                                text = "KnowSpends v${BuildConfig.VERSION_NAME}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurface
                            )
                            Text(
                                text = "Build ${BuildConfig.VERSION_CODE} • Local-First & Private",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant
                            )
                        }

                        if (uiState.isCheckingUpdate) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            VyxelActionPill(
                                text = "Check Updates",
                                icon = Icons.Default.Update,
                                onClick = { viewModel.checkForUpdates() }
                            )
                        }
                    }

                    if (uiState.isDownloadingUpdate) {
                        Spacer(Modifier.height(10.dp))
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
                            Text(
                                text = "Downloading update: ${(uiState.downloadProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.primary
                            )
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
                                text = "Temporary Cache",
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.onSurface
                            )
                            Text(
                                text = "Clean export files, receipts, and APK cache",
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant
                            )
                        }
                        VyxelActionPill(
                            text = "Clean",
                            icon = Icons.Default.CleaningServices,
                            onClick = { viewModel.clearAppCache(context) }
                        )
                    }

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = scheme.outlineVariant.copy(alpha = 0.3f))
                    Spacer(Modifier.height(10.dp))

                    VyxelActionRow(
                        title = "GitHub Repository & Community",
                        subtitle = "Source code, release notes, and discussions",
                        icon = Icons.Default.OpenInNew,
                        onClick = {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://github.com/prajwalprince94-cmd/Expence")
                                )
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
                    // 1. Name & Tagline
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

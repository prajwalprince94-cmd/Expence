package com.knowspends.app.ui.settings

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowspends.app.data.remote.UpdateCheckerService
import com.knowspends.app.domain.model.RestoreResult
import com.knowspends.app.domain.model.SecurityConfig
import com.knowspends.app.domain.model.UserPreferences
import com.knowspends.app.domain.repository.BackupRepository
import com.knowspends.app.domain.repository.CsvRepository
import com.knowspends.app.domain.repository.PremiumRepository
import com.knowspends.app.domain.repository.SecurityRepository
import com.knowspends.app.domain.repository.UserPreferencesRepository
import com.knowspends.app.util.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val userPreferences: UserPreferences = UserPreferences(),
    val securityConfig: SecurityConfig = SecurityConfig(),
    val isPremium: Boolean = false,
    val showPaywallSheet: Boolean = false,
    val isLoading: Boolean = false,
    val isCheckingUpdate: Boolean = false,
    val statusMessage: String? = null,
    val lastImportBatchId: String? = null,
    val lastImportCount: Int? = null,
    val showRestoreConfirmDialog: Boolean = false,
    val pendingRestoreUri: Uri? = null,
    val showProfileEditDialog: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
    private val csvRepository: CsvRepository,
    private val securityRepository: SecurityRepository,
    private val premiumRepository: PremiumRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val updateCheckerService: UpdateCheckerService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferencesFlow.collect { prefs ->
                _uiState.update { it.copy(userPreferences = prefs) }
            }
        }
        viewModelScope.launch {
            securityRepository.securityConfigFlow.collect { config ->
                _uiState.update { it.copy(securityConfig = config) }
            }
        }
        viewModelScope.launch {
            premiumRepository.premiumStateFlow.collect { premium ->
                _uiState.update { it.copy(isPremium = premium.isPremium) }
            }
        }
    }

    // Profile Settings
    fun showProfileDialog(show: Boolean) {
        _uiState.update { it.copy(showProfileEditDialog = show) }
    }

    fun updateProfile(name: String, tagline: String, avatarUri: String, coverUri: String, currency: String) {
        viewModelScope.launch {
            userPreferencesRepository.updateProfile(name, tagline, avatarUri, coverUri, currency)
            _uiState.update { it.copy(showProfileEditDialog = false, statusMessage = "Profile updated!") }
        }
    }

    // Appearance & Display
    fun setThemeMode(themeMode: String) {
        viewModelScope.launch {
            userPreferencesRepository.setThemeMode(themeMode)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setDynamicColor(enabled)
        }
    }

    fun setFontId(fontId: String) {
        viewModelScope.launch {
            userPreferencesRepository.setFontId(fontId)
        }
    }

    fun setDisplayScale(scale: Float) {
        viewModelScope.launch {
            userPreferencesRepository.setDisplayScale(scale)
        }
    }

    // Notifications
    fun setDailyReminder(enabled: Boolean, time: String) {
        viewModelScope.launch {
            userPreferencesRepository.setDailyReminder(enabled, time)
            if (enabled) {
                ReminderScheduler.scheduleDailyReminder(context, time)
            } else {
                ReminderScheduler.cancelDailyReminder(context)
            }
        }
    }

    fun setBudgetAlert(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setBudgetAlert(enabled)
        }
    }

    // Updates
    fun checkForUpdates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingUpdate = true, statusMessage = null) }
            val result = updateCheckerService.checkForUpdates()
            _uiState.update { it.copy(isCheckingUpdate = false) }

            if (result.isSuccess) {
                userPreferencesRepository.setUpdateInfo(
                    hasUpdate = result.hasUpdate,
                    version = result.latestVersion,
                    releaseUrl = result.downloadUrl,
                    notes = result.releaseNotes
                )
                val msg = if (result.hasUpdate) {
                    "Update available: ${result.latestVersion}!"
                } else {
                    "You are on the latest version (${com.knowspends.app.BuildConfig.VERSION_NAME})"
                }
                _uiState.update { it.copy(statusMessage = msg) }
            } else {
                _uiState.update { it.copy(statusMessage = result.errorMessage ?: "Failed to check updates") }
            }
        }
    }

    // Pro / Premium
    fun showPaywall() {
        _uiState.update { it.copy(showPaywallSheet = true) }
    }

    fun hidePaywall() {
        _uiState.update { it.copy(showPaywallSheet = false) }
    }

    fun upgradeToPremium() {
        viewModelScope.launch {
            premiumRepository.setPremiumStatus(true)
            _uiState.update { it.copy(showPaywallSheet = false, statusMessage = "Upgraded to KnowSpends Pro!") }
        }
    }

    fun togglePremiumStatus(isPremium: Boolean) {
        viewModelScope.launch {
            premiumRepository.setPremiumStatus(isPremium)
        }
    }

    // Security
    fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch {
            securityRepository.setBiometricEnabled(enabled)
        }
    }

    fun toggleSecureScreen(enabled: Boolean) {
        viewModelScope.launch {
            securityRepository.setSecureScreenEnabled(enabled)
        }
    }

    fun setLockTimeout(minutes: Int) {
        viewModelScope.launch {
            securityRepository.setLockTimeoutMinutes(minutes)
        }
    }

    // Backup & Restore
    fun exportBackup(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = null) }
            try {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    backupRepository.exportBackupJson(outputStream)
                }
                _uiState.update { it.copy(isLoading = false, statusMessage = "Backup exported successfully!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, statusMessage = "Export failed: ${e.localizedMessage}") }
            }
        }
    }

    fun requestRestore(uri: Uri) {
        _uiState.update { it.copy(showRestoreConfirmDialog = true, pendingRestoreUri = uri) }
    }

    fun cancelRestore() {
        _uiState.update { it.copy(showRestoreConfirmDialog = false, pendingRestoreUri = null) }
    }

    fun confirmRestore(contentResolver: ContentResolver) {
        val uri = _uiState.value.pendingRestoreUri ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showRestoreConfirmDialog = false, pendingRestoreUri = null) }
            try {
                val result = contentResolver.openInputStream(uri)?.use { inputStream ->
                    backupRepository.restoreBackupJson(inputStream)
                } ?: RestoreResult.Error("Unable to open file stream")

                val msg = when (result) {
                    is RestoreResult.Success -> "Restored ${result.transactionsRestored} transactions, ${result.accountsRestored} accounts, ${result.categoriesRestored} categories, ${result.budgetsRestored} budgets."
                    is RestoreResult.Error -> "Restore failed: ${result.message}"
                }
                _uiState.update { it.copy(isLoading = false, statusMessage = msg) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, statusMessage = "Restore failed: ${e.localizedMessage}") }
            }
        }
    }

    fun exportCsv(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = null) }
            try {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    csvRepository.exportTransactionsCsv(outputStream)
                }
                _uiState.update { it.copy(isLoading = false, statusMessage = "CSV exported successfully!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, statusMessage = "CSV export failed: ${e.localizedMessage}") }
            }
        }
    }

    fun importCsv(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, statusMessage = null) }
            try {
                val result = contentResolver.openInputStream(uri)?.use { inputStream ->
                    csvRepository.importTransactionsCsv(inputStream)
                }
                if (result == null || result.errorMessage != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            statusMessage = "Import failed: ${result?.errorMessage ?: "Could not read file"}"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            lastImportBatchId = result.batchId,
                            lastImportCount = result.importedCount,
                            statusMessage = "Imported ${result.importedCount} transactions (${result.skippedDuplicatesCount} duplicates skipped)."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, statusMessage = "Import error: ${e.localizedMessage}") }
            }
        }
    }

    fun undoLastImport() {
        val batchId = _uiState.value.lastImportBatchId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val deletedCount = csvRepository.undoImportBatch(batchId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        lastImportBatchId = null,
                        lastImportCount = null,
                        statusMessage = "Undid import: removed $deletedCount transactions."
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, statusMessage = "Undo failed: ${e.localizedMessage}") }
            }
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}

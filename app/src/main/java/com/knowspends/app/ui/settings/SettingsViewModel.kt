package com.knowspends.app.ui.settings

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.knowspends.app.domain.model.RestoreResult
import com.knowspends.app.domain.repository.BackupRepository
import com.knowspends.app.domain.repository.CsvRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val securityConfig: com.knowspends.app.domain.model.SecurityConfig = com.knowspends.app.domain.model.SecurityConfig(),
    val isPremium: Boolean = false,
    val showPaywallSheet: Boolean = false,
    val isLoading: Boolean = false,
    val statusMessage: String? = null,
    val lastImportBatchId: String? = null,
    val lastImportCount: Int? = null,
    val showRestoreConfirmDialog: Boolean = false,
    val pendingRestoreUri: Uri? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val backupRepository: BackupRepository,
    private val csvRepository: CsvRepository,
    private val securityRepository: com.knowspends.app.domain.repository.SecurityRepository,
    private val premiumRepository: com.knowspends.app.domain.repository.PremiumRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
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

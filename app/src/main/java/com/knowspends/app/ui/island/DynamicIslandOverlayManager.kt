package com.knowspends.app.ui.island

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.setViewTreeOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.knowspends.app.domain.model.Transaction
import com.knowspends.app.domain.model.TransactionType
import com.knowspends.app.domain.model.UserPreferences
import com.knowspends.app.domain.repository.AccountRepository
import com.knowspends.app.domain.repository.CategoryRepository
import com.knowspends.app.domain.repository.TransactionRepository
import com.knowspends.app.domain.repository.UserPreferencesRepository
import com.knowspends.app.ui.theme.KnowSpendsTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DynamicIslandOverlayManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    companion object {
        private const val TAG = "DynamicIslandOverlay"
    }

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val overlayJob = SupervisorJob()
    private val overlayScope = CoroutineScope(Dispatchers.Main + overlayJob)

    private val _uiState = MutableStateFlow(DynamicIslandUiState())
    private var overlayView: View? = null
    private var overlayLifecycleOwner: OverlayLifecycleOwner? = null

    @Volatile
    var isShowing: Boolean = false
        private set

    fun showOverlay() {
        if (!Settings.canDrawOverlays(context)) {
            Log.w(TAG, "Cannot draw overlay: SYSTEM_ALERT_WINDOW permission not granted")
            return
        }

        Handler(Looper.getMainLooper()).post {
            if (isShowing && overlayView != null) {
                _uiState.update { it.copy(isExpanded = true, amountInput = "0", isSuccess = false) }
                return@post
            }

            overlayScope.launch {
                val accounts = accountRepository.getActiveAccountsWithBalances().first()
                val categories = categoryRepository.getActiveCategories().first()
                val prefs = userPreferencesRepository.userPreferencesFlow.first()

                _uiState.update {
                    DynamicIslandUiState(
                        isExpanded = true,
                        isPillVisible = false,
                        amountInput = "0",
                        selectedAccountId = accounts.firstOrNull()?.id,
                        selectedCategoryId = categories.firstOrNull()?.id,
                        accounts = accounts,
                        categories = categories,
                        currencySymbol = prefs.currencySymbol,
                        isSuccess = false,
                        isSaving = false,
                        errorMessage = null
                    )
                }

                attachOverlayView()
            }
        }
    }

    private fun attachOverlayView() {
        if (overlayView != null) return

        val lifecycleOwner = OverlayLifecycleOwner()
        overlayLifecycleOwner = lifecycleOwner

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        }

        val composeView = ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            setViewTreeOnBackPressedDispatcherOwner(lifecycleOwner)

            setContent {
                val uiState by _uiState.collectAsState()
                val prefs by userPreferencesRepository.userPreferencesFlow.collectAsState(initial = UserPreferences())

                KnowSpendsTheme(
                    themeMode = prefs.themeMode,
                    dynamicColor = prefs.dynamicColor,
                    fontId = prefs.fontId
                ) {
                    DynamicIslandQuickSpend(
                        uiState = uiState,
                        onExpand = { _uiState.update { it.copy(isExpanded = true) } },
                        onCollapse = { hideOverlay() },
                        onDigit = { onDigit(it) },
                        onBackspace = { onBackspace() },
                        onClear = { onClear() },
                        onCategorySelected = { onCategorySelected(it) },
                        onAccountSelected = { onAccountSelected(it) },
                        onSaveExpense = { saveExpense() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        val container = object : FrameLayout(context) {
            override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    hideOverlay()
                    return true
                }
                return super.dispatchKeyEvent(event)
            }
        }.apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            setViewTreeOnBackPressedDispatcherOwner(lifecycleOwner)
            addView(
                composeView,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )
        }

        try {
            windowManager.addView(container, params)
            overlayView = container
            isShowing = true
            Log.d(TAG, "Dynamic Island overlay window successfully attached to WindowManager")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to attach overlay window to WindowManager", e)
            lifecycleOwner.destroy()
            overlayLifecycleOwner = null
            overlayView = null
            isShowing = false
        }
    }

    fun hideOverlay() {
        if (!isShowing && overlayView == null) return
        isShowing = false

        Handler(Looper.getMainLooper()).post {
            try {
                overlayView?.let { windowManager.removeView(it) }
                overlayLifecycleOwner?.destroy()
            } catch (e: Exception) {
                Log.e(TAG, "Error removing overlay from WindowManager", e)
            } finally {
                overlayView = null
                overlayLifecycleOwner = null
                _uiState.update { it.copy(isExpanded = false, isSuccess = false) }
            }
        }
    }

    private fun onDigit(char: Char) {
        _uiState.update { current ->
            val curr = current.amountInput
            val next = when {
                char == '.' && curr.contains('.') -> curr
                char == '.' && curr == "0" -> "0."
                char != '.' && curr.contains('.') && curr.substringAfter('.').length >= 2 -> curr
                curr == "0" && char != '.' -> char.toString()
                curr.length >= 8 -> curr
                else -> curr + char
            }
            current.copy(amountInput = next, errorMessage = null)
        }
    }

    private fun onBackspace() {
        _uiState.update { current ->
            val curr = current.amountInput
            val next = when {
                curr.length <= 1 -> "0"
                else -> curr.dropLast(1)
            }
            current.copy(amountInput = next, errorMessage = null)
        }
    }

    private fun onClear() {
        _uiState.update { it.copy(amountInput = "0", errorMessage = null) }
    }

    private fun onCategorySelected(categoryId: Long) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    private fun onAccountSelected(accountId: Long) {
        _uiState.update { it.copy(selectedAccountId = accountId) }
    }

    private fun saveExpense() {
        val state = _uiState.value
        val amountPaise = state.amountMinor
        if (amountPaise <= 0L) return
        val categoryId = state.selectedCategoryId ?: state.categories.firstOrNull()?.id ?: return
        val accountId = state.selectedAccountId ?: state.accounts.firstOrNull()?.id ?: return

        overlayScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val category = state.categories.find { it.id == categoryId }
            val tx = Transaction(
                accountId = accountId,
                categoryId = categoryId,
                type = TransactionType.EXPENSE,
                amountMinor = amountPaise,
                date = System.currentTimeMillis(),
                payee = category?.name ?: "Quick Expense",
                note = "Logged via Dynamic Island",
                createdAt = System.currentTimeMillis()
            )
            transactionRepository.saveTransaction(tx)
            _uiState.update { it.copy(isSaving = false, isSuccess = true) }
            delay(750)
            hideOverlay()
        }
    }
}

private class OverlayLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner, OnBackPressedDispatcherOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val onBackPressedDispatcher = OnBackPressedDispatcher()

    init {
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    fun destroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}

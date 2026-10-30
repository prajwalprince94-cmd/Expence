package com.knowspends.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.knowspends.app.domain.model.SecurityConfig
import com.knowspends.app.domain.repository.SecurityRepository
import com.knowspends.app.ui.navigation.KnowSpendsAppRoot
import com.knowspends.app.ui.security.AppLockScreen
import com.knowspends.app.ui.security.BiometricAuthManager
import com.knowspends.app.ui.theme.KnowSpendsTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var securityRepository: SecurityRepository

    @Inject
    lateinit var userPreferencesRepository: com.knowspends.app.domain.repository.UserPreferencesRepository

    private var securityConfig by mutableStateOf(SecurityConfig())
    private var userPreferences by mutableStateOf(com.knowspends.app.domain.model.UserPreferences())
    private var isAppLocked by mutableStateOf(false)
    private var backgroundTimestamp: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    securityRepository.securityConfigFlow.collectLatest { config ->
                        securityConfig = config
                        applySecureFlag(config.isSecureScreenEnabled)

                        // Lock initially on launch if biometric is enabled
                        if (config.isBiometricEnabled && backgroundTimestamp == 0L) {
                            isAppLocked = true
                            promptUnlock()
                        }
                    }
                }
                launch {
                    userPreferencesRepository.userPreferencesFlow.collectLatest { prefs ->
                        userPreferences = prefs
                    }
                }
            }
        }

        setContent {
            KnowSpendsTheme(
                themeMode = userPreferences.themeMode,
                dynamicColor = userPreferences.dynamicColor,
                fontId = userPreferences.fontId
            ) {
                if (isAppLocked && securityConfig.isBiometricEnabled) {
                    AppLockScreen(
                        onUnlockClicked = { promptUnlock() }
                    )
                } else {
                    KnowSpendsAppRoot(initialRoute = intent?.getStringExtra("ROUTE"))
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        backgroundTimestamp = System.currentTimeMillis()
    }

    override fun onStart() {
        super.onStart()
        if (backgroundTimestamp != 0L && securityConfig.isBiometricEnabled) {
            val elapsedMinutes = (System.currentTimeMillis() - backgroundTimestamp) / 1000 / 60
            if (elapsedMinutes >= securityConfig.lockTimeoutMinutes) {
                isAppLocked = true
                promptUnlock()
            }
        }
    }

    private fun promptUnlock() {
        if (!BiometricAuthManager.canAuthenticate(this)) {
            // Device does not support biometrics/credentials, bypass
            isAppLocked = false
            return
        }

        BiometricAuthManager.authenticate(
            activity = this,
            title = "Unlock KnowSpends",
            subtitle = "Authenticate to access your accounts",
            onSuccess = {
                isAppLocked = false
            }
        )
    }

    private fun applySecureFlag(secure: Boolean) {
        if (secure) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}

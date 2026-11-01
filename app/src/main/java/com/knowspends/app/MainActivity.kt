package com.knowspends.app

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.knowspends.app.domain.model.SecurityConfig
import com.knowspends.app.domain.repository.SecurityRepository
import com.knowspends.app.sensor.BackTapDetector
import com.knowspends.app.ui.navigation.KnowSpendsAppRoot
import com.knowspends.app.ui.security.AppLockScreen
import com.knowspends.app.ui.security.BiometricAuthManager
import com.knowspends.app.ui.splash.SplashScreen
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

    private var triggerDynamicIsland by mutableStateOf(false)
    private var backTapDetector: BackTapDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)

        backTapDetector = BackTapDetector(this) {
            runOnUiThread {
                if (!isAppLocked) {
                    triggerDynamicIsland = true
                }
            }
        }

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
                        updateBackTapSensitivity()
                    }
                }
            }
        }

        setContent {
            var showSplash by remember { mutableStateOf(true) }

            KnowSpendsTheme(
                themeMode = userPreferences.themeMode,
                dynamicColor = userPreferences.dynamicColor,
                fontId = userPreferences.fontId
            ) {
                Crossfade(
                    targetState = showSplash,
                    animationSpec = tween(durationMillis = 400),
                    label = "splashCrossfade"
                ) { isSplashActive ->
                    if (isSplashActive) {
                        SplashScreen(onSplashFinished = { showSplash = false })
                    } else {
                        if (isAppLocked && securityConfig.isBiometricEnabled) {
                            AppLockScreen(
                                onUnlockClicked = { promptUnlock() }
                            )
                        } else {
                            KnowSpendsAppRoot(
                                initialRoute = intent?.getStringExtra("ROUTE") ?: intent?.getStringExtra("initial_route"),
                                triggerDynamicIsland = triggerDynamicIsland,
                                onDynamicIslandDismissed = { triggerDynamicIsland = false }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (userPreferences.backTapEnabled) {
            updateBackTapSensitivity()
            backTapDetector?.start()
        }
    }

    override fun onPause() {
        super.onPause()
        backTapDetector?.stop()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("TRIGGER_DYNAMIC_ISLAND", false) == true ||
            intent?.getStringExtra("TRIGGER_DYNAMIC_ISLAND") == "true" ||
            intent?.data?.host == "dynamic-island"
        ) {
            triggerDynamicIsland = true
        }
    }

    private fun updateBackTapSensitivity() {
        val detector = backTapDetector ?: return
        detector.sensitivityThreshold = when (userPreferences.backTapSensitivity) {
            "high" -> BackTapDetector.SENSITIVITY_HIGH
            "low" -> BackTapDetector.SENSITIVITY_LOW
            else -> BackTapDetector.SENSITIVITY_NORMAL
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

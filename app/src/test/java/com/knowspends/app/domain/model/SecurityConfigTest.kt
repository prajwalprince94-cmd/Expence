package com.knowspends.app.domain.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityConfigTest {

    private fun isAppLockTriggered(
        config: SecurityConfig,
        backgroundTimestamp: Long,
        currentTimestamp: Long
    ): Boolean {
        if (!config.isBiometricEnabled) return false
        if (backgroundTimestamp == 0L) return true
        val elapsedMinutes = (currentTimestamp - backgroundTimestamp) / 1000 / 60
        return elapsedMinutes >= config.lockTimeoutMinutes
    }

    @Test
    fun appLock_immediateTimeout_locksInstantly() {
        val config = SecurityConfig(
            isBiometricEnabled = true,
            lockTimeoutMinutes = 0
        )

        val backgroundTime = 1000000L
        val resumeTime = backgroundTime + 5000L // 5 seconds later

        assertTrue(isAppLockTriggered(config, backgroundTime, resumeTime))
    }

    @Test
    fun appLock_fiveMinuteTimeout_doesNotLockBeforeTimeout() {
        val config = SecurityConfig(
            isBiometricEnabled = true,
            lockTimeoutMinutes = 5
        )

        val backgroundTime = 1000000L
        val resumeTimeBeforeTimeout = backgroundTime + (3 * 60 * 1000L) // 3 minutes later
        val resumeTimeAfterTimeout = backgroundTime + (6 * 60 * 1000L)  // 6 minutes later

        assertFalse(isAppLockTriggered(config, backgroundTime, resumeTimeBeforeTimeout))
        assertTrue(isAppLockTriggered(config, backgroundTime, resumeTimeAfterTimeout))
    }

    @Test
    fun appLock_disabled_neverLocks() {
        val config = SecurityConfig(
            isBiometricEnabled = false,
            lockTimeoutMinutes = 0
        )

        val backgroundTime = 1000000L
        val resumeTime = backgroundTime + 3600000L // 1 hour later

        assertFalse(isAppLockTriggered(config, backgroundTime, resumeTime))
    }
}

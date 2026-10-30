package com.knowspends.app.domain.model

data class SecurityConfig(
    val isBiometricEnabled: Boolean = false,
    val isSecureScreenEnabled: Boolean = false,
    val lockTimeoutMinutes: Int = 0
)

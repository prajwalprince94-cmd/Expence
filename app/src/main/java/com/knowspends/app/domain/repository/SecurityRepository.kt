package com.knowspends.app.domain.repository

import com.knowspends.app.domain.model.SecurityConfig
import kotlinx.coroutines.flow.Flow

interface SecurityRepository {
    val securityConfigFlow: Flow<SecurityConfig>
    suspend fun setBiometricEnabled(enabled: Boolean)
    suspend fun setSecureScreenEnabled(enabled: Boolean)
    suspend fun setLockTimeoutMinutes(minutes: Int)
}

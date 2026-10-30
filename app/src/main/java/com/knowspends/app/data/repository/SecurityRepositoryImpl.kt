package com.knowspends.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.knowspends.app.di.IoDispatcher
import com.knowspends.app.domain.model.SecurityConfig
import com.knowspends.app.domain.repository.SecurityRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.securityDataStore: DataStore<Preferences> by preferencesDataStore(name = "security_prefs")

@Singleton
class SecurityRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : SecurityRepository {

    private object PreferencesKeys {
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val SECURE_SCREEN_ENABLED = booleanPreferencesKey("secure_screen_enabled")
        val LOCK_TIMEOUT_MINUTES = intPreferencesKey("lock_timeout_minutes")
    }

    override val securityConfigFlow: Flow<SecurityConfig> = context.securityDataStore.data.map { prefs ->
        SecurityConfig(
            isBiometricEnabled = prefs[PreferencesKeys.BIOMETRIC_ENABLED] ?: false,
            isSecureScreenEnabled = prefs[PreferencesKeys.SECURE_SCREEN_ENABLED] ?: false,
            lockTimeoutMinutes = prefs[PreferencesKeys.LOCK_TIMEOUT_MINUTES] ?: 0
        )
    }.flowOn(ioDispatcher)

    override suspend fun setBiometricEnabled(enabled: Boolean) = withContext(ioDispatcher) {
        context.securityDataStore.edit { prefs ->
            prefs[PreferencesKeys.BIOMETRIC_ENABLED] = enabled
        }
        Unit
    }

    override suspend fun setSecureScreenEnabled(enabled: Boolean) = withContext(ioDispatcher) {
        context.securityDataStore.edit { prefs ->
            prefs[PreferencesKeys.SECURE_SCREEN_ENABLED] = enabled
        }
        Unit
    }

    override suspend fun setLockTimeoutMinutes(minutes: Int) = withContext(ioDispatcher) {
        context.securityDataStore.edit { prefs ->
            prefs[PreferencesKeys.LOCK_TIMEOUT_MINUTES] = minutes
        }
        Unit
    }
}

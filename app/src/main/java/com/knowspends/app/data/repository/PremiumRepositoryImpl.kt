package com.knowspends.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.knowspends.app.di.IoDispatcher
import com.knowspends.app.domain.model.FeatureGate
import com.knowspends.app.domain.model.PremiumState
import com.knowspends.app.domain.repository.PremiumRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.billingDataStore: DataStore<Preferences> by preferencesDataStore(name = "billing_prefs")

@Singleton
class PremiumRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PremiumRepository {

    private object PreferencesKeys {
        val IS_PREMIUM = booleanPreferencesKey("is_premium_active")
    }

    override val premiumStateFlow: Flow<PremiumState> = context.billingDataStore.data.map { prefs ->
        PremiumState(isPremium = prefs[PreferencesKeys.IS_PREMIUM] ?: false)
    }.flowOn(ioDispatcher)

    override suspend fun isFeatureUnlocked(feature: FeatureGate): Boolean = withContext(ioDispatcher) {
        val state = premiumStateFlow.first()
        state.isPremium
    }

    override suspend fun setPremiumStatus(isPremium: Boolean) = withContext(ioDispatcher) {
        context.billingDataStore.edit { prefs ->
            prefs[PreferencesKeys.IS_PREMIUM] = isPremium
        }
        Unit
    }
}

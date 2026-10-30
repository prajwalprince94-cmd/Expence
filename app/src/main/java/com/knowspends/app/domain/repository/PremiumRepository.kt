package com.knowspends.app.domain.repository

import com.knowspends.app.domain.model.FeatureGate
import com.knowspends.app.domain.model.PremiumState
import kotlinx.coroutines.flow.Flow

interface PremiumRepository {
    val premiumStateFlow: Flow<PremiumState>
    suspend fun isFeatureUnlocked(feature: FeatureGate): Boolean
    suspend fun setPremiumStatus(isPremium: Boolean)
}

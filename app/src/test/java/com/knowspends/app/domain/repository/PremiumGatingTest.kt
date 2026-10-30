package com.knowspends.app.domain.repository

import com.knowspends.app.domain.model.FeatureGate
import com.knowspends.app.domain.model.PremiumState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakePremiumRepository(initialPremium: Boolean = false) : PremiumRepository {
    private val _state = MutableStateFlow(PremiumState(isPremium = initialPremium))
    override val premiumStateFlow: Flow<PremiumState> = _state.asStateFlow()

    override suspend fun isFeatureUnlocked(feature: FeatureGate): Boolean {
        return _state.first().isPremium
    }

    override suspend fun setPremiumStatus(isPremium: Boolean) {
        _state.value = PremiumState(isPremium = isPremium)
    }
}

class PremiumGatingTest {

    @Test
    fun `free tier user has premium features locked`() = runBlocking {
        val repository = FakePremiumRepository(initialPremium = false)

        assertFalse(repository.premiumStateFlow.first().isPremium)
        assertFalse(repository.isFeatureUnlocked(FeatureGate.UNLIMITED_BUDGETS))
        assertFalse(repository.isFeatureUnlocked(FeatureGate.AUTO_CAPTURE_INBOX))
        assertFalse(repository.isFeatureUnlocked(FeatureGate.ADVANCED_REPORTS_EXPORT))
    }

    @Test
    fun `purchasing premium unlocks all power features`() = runBlocking {
        val repository = FakePremiumRepository(initialPremium = false)

        // Upgrade to Pro
        repository.setPremiumStatus(true)

        assertTrue(repository.premiumStateFlow.first().isPremium)
        assertTrue(repository.isFeatureUnlocked(FeatureGate.UNLIMITED_BUDGETS))
        assertTrue(repository.isFeatureUnlocked(FeatureGate.AUTO_CAPTURE_INBOX))
        assertTrue(repository.isFeatureUnlocked(FeatureGate.ADVANCED_REPORTS_EXPORT))
    }
}

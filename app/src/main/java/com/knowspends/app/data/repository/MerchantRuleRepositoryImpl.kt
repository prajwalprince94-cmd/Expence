package com.knowspends.app.data.repository

import android.content.Context
import com.knowspends.app.di.IoDispatcher
import com.knowspends.app.domain.model.MerchantRule
import com.knowspends.app.domain.repository.MerchantRuleRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MerchantRuleRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : MerchantRuleRepository {

    private val rulesFile: File by lazy {
        File(context.filesDir, "merchant_rules.json")
    }

    private val mutex = Mutex()
    private val _rulesState = MutableStateFlow<List<MerchantRule>>(emptyList())

    init {
        loadRulesFromDisk()
    }

    private fun loadRulesFromDisk() {
        try {
            if (rulesFile.exists()) {
                val jsonStr = rulesFile.readText()
                val array = JSONArray(jsonStr)
                val list = mutableListOf<MerchantRule>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        MerchantRule(
                            id = obj.optString("id", UUID.randomUUID().toString()),
                            merchantPattern = obj.getString("merchantPattern"),
                            categoryId = obj.getLong("categoryId"),
                            categoryName = obj.optString("categoryName", "Category"),
                            isExactMatch = obj.optBoolean("isExactMatch", false),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                _rulesState.value = list
            } else {
                // Pre-seed popular fintech merchants for instant intelligent categorization
                val defaults = listOf(
                    MerchantRule(merchantPattern = "swiggy", categoryId = 1L, categoryName = "Food & Dining"),
                    MerchantRule(merchantPattern = "zomato", categoryId = 1L, categoryName = "Food & Dining"),
                    MerchantRule(merchantPattern = "starbucks", categoryId = 1L, categoryName = "Food & Dining"),
                    MerchantRule(merchantPattern = "blinkit", categoryId = 2L, categoryName = "Groceries"),
                    MerchantRule(merchantPattern = "zepto", categoryId = 2L, categoryName = "Groceries"),
                    MerchantRule(merchantPattern = "instamart", categoryId = 2L, categoryName = "Groceries"),
                    MerchantRule(merchantPattern = "uber", categoryId = 3L, categoryName = "Transportation"),
                    MerchantRule(merchantPattern = "ola", categoryId = 3L, categoryName = "Transportation"),
                    MerchantRule(merchantPattern = "rapido", categoryId = 3L, categoryName = "Transportation"),
                    MerchantRule(merchantPattern = "amazon", categoryId = 5L, categoryName = "Shopping"),
                    MerchantRule(merchantPattern = "flipkart", categoryId = 5L, categoryName = "Shopping"),
                    MerchantRule(merchantPattern = "netflix", categoryId = 6L, categoryName = "Entertainment"),
                    MerchantRule(merchantPattern = "spotify", categoryId = 6L, categoryName = "Entertainment")
                )
                _rulesState.value = defaults
                writeRulesToDisk(defaults)
            }
        } catch (_: Exception) {
            _rulesState.value = emptyList()
        }
    }

    private fun writeRulesToDisk(rules: List<MerchantRule>) {
        try {
            val array = JSONArray()
            rules.forEach { rule ->
                val obj = JSONObject().apply {
                    put("id", rule.id)
                    put("merchantPattern", rule.merchantPattern)
                    put("categoryId", rule.categoryId)
                    put("categoryName", rule.categoryName)
                    put("isExactMatch", rule.isExactMatch)
                    put("createdAt", rule.createdAt)
                }
                array.put(obj)
            }
            rulesFile.writeText(array.toString())
        } catch (_: Exception) { }
    }

    override fun getRules(): Flow<List<MerchantRule>> = _rulesState.asStateFlow()

    override suspend fun saveRule(
        pattern: String,
        categoryId: Long,
        categoryName: String,
        isExactMatch: Boolean
    ): MerchantRule = withContext(ioDispatcher) {
        val trimmedPattern = pattern.trim()
        val rule = MerchantRule(
            id = UUID.randomUUID().toString(),
            merchantPattern = trimmedPattern,
            categoryId = categoryId,
            categoryName = categoryName,
            isExactMatch = isExactMatch
        )
        mutex.withLock {
            val current = _rulesState.value.toMutableList()
            val existingIndex = current.indexOfFirst {
                it.merchantPattern.equals(trimmedPattern, ignoreCase = true)
            }
            if (existingIndex >= 0) {
                current[existingIndex] = rule
            } else {
                current.add(0, rule)
            }
            _rulesState.value = current
            writeRulesToDisk(current)
        }
        rule
    }

    override suspend fun deleteRule(id: String) = withContext(ioDispatcher) {
        mutex.withLock {
            val updated = _rulesState.value.filter { it.id != id }
            _rulesState.value = updated
            writeRulesToDisk(updated)
        }
    }

    override suspend fun matchCategory(merchantOrPayee: String): MerchantRule? = withContext(ioDispatcher) {
        val candidate = merchantOrPayee.trim()
        if (candidate.isBlank()) return@withContext null
        val rules = _rulesState.value

        // 1. Exact match priority
        val exact = rules.firstOrNull { it.isExactMatch && it.merchantPattern.equals(candidate, ignoreCase = true) }
        if (exact != null) return@withContext exact

        // 2. Candidate contains pattern (e.g. candidate "Swiggy Bangalore" contains "swiggy")
        val containsPattern = rules.firstOrNull {
            !it.isExactMatch && candidate.contains(it.merchantPattern, ignoreCase = true)
        }
        if (containsPattern != null) return@withContext containsPattern

        // 3. Pattern contains candidate (e.g. pattern "Uber India" contains "Uber")
        rules.firstOrNull {
            !it.isExactMatch && it.merchantPattern.contains(candidate, ignoreCase = true)
        }
    }
}

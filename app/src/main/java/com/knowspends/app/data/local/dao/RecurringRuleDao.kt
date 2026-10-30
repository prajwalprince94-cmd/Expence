package com.knowspends.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.knowspends.app.data.local.entity.RecurringExecutionEntity
import com.knowspends.app.data.local.entity.RecurringRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringRuleDao {

    @Query("SELECT * FROM recurring_rules WHERE isActive = 1 ORDER BY nextDueDate ASC")
    fun getActiveRulesFlow(): Flow<List<RecurringRuleEntity>>

    @Query("SELECT * FROM recurring_rules ORDER BY isActive DESC, nextDueDate ASC")
    fun getAllRulesFlow(): Flow<List<RecurringRuleEntity>>

    @Query("SELECT * FROM recurring_rules WHERE id = :id")
    suspend fun getRuleById(id: Long): RecurringRuleEntity?

    @Query("SELECT * FROM recurring_rules WHERE isActive = 1 AND nextDueDate <= :currentTime")
    suspend fun getDueRules(currentTime: Long): List<RecurringRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: RecurringRuleEntity): Long

    @Update
    suspend fun updateRule(rule: RecurringRuleEntity)

    @Query("UPDATE recurring_rules SET nextDueDate = :nextDueDate WHERE id = :id")
    suspend fun updateNextDueDate(id: Long, nextDueDate: Long)

    @Query("UPDATE recurring_rules SET isActive = :isActive WHERE id = :id")
    suspend fun setRuleActive(id: Long, isActive: Boolean)

    @Query("DELETE FROM recurring_rules WHERE id = :id")
    suspend fun deleteRule(id: Long)

    // Execution Tracking
    @Query("SELECT COUNT(*) FROM recurring_executions WHERE ruleId = :ruleId AND dueDate = :dueDate")
    suspend fun hasExecuted(ruleId: Long, dueDate: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertExecution(execution: RecurringExecutionEntity): Long

    @Query("SELECT * FROM recurring_rules ORDER BY id ASC")
    suspend fun getAllRulesForBackup(): List<RecurringRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRules(rules: List<RecurringRuleEntity>)
}

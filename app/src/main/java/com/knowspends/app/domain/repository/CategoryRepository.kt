package com.knowspends.app.domain.repository

import com.knowspends.app.domain.model.Category
import com.knowspends.app.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getActiveCategories(): Flow<List<Category>>
    fun getCategoriesByKind(kind: TransactionType): Flow<List<Category>>
    fun getCategory(id: Long): Flow<Category?>
    suspend fun saveCategory(category: Category): Long
    suspend fun archiveCategory(id: Long, archived: Boolean)
}

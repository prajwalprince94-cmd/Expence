package com.knowspends.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.knowspends.app.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories WHERE archived = 0 ORDER BY name ASC")
    fun getActiveCategoriesFlow(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE kind = :kind AND archived = 0 ORDER BY name ASC")
    fun getCategoriesByKindFlow(kind: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY archived ASC, name ASC")
    fun getAllCategoriesFlow(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    fun getCategoryById(id: Long): Flow<CategoryEntity?>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryByIdOnce(id: Long): CategoryEntity?

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("UPDATE categories SET archived = :archived WHERE id = :id")
    suspend fun setCategoryArchived(id: Long, archived: Boolean)
}

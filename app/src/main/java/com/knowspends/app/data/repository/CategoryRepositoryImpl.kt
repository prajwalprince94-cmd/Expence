package com.knowspends.app.data.repository

import com.knowspends.app.data.local.dao.CategoryDao
import com.knowspends.app.data.local.entity.CategoryEntity
import com.knowspends.app.di.IoDispatcher
import com.knowspends.app.domain.model.Category
import com.knowspends.app.domain.model.TransactionType
import com.knowspends.app.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CategoryRepository {

    override fun getActiveCategories(): Flow<List<Category>> {
        return categoryDao.getActiveCategoriesFlow().map { list ->
            list.map { it.toDomain() }
        }.flowOn(ioDispatcher)
    }

    override fun getCategoriesByKind(kind: TransactionType): Flow<List<Category>> {
        return categoryDao.getCategoriesByKindFlow(kind.name).map { list ->
            list.map { it.toDomain() }
        }.flowOn(ioDispatcher)
    }

    override fun getCategory(id: Long): Flow<Category?> {
        return categoryDao.getCategoryById(id).map { it?.toDomain() }.flowOn(ioDispatcher)
    }

    override suspend fun saveCategory(category: Category): Long = withContext(ioDispatcher) {
        val entity = CategoryEntity(
            id = category.id,
            name = category.name,
            kind = category.kind.name,
            iconKey = category.iconKey,
            colour = category.colour,
            parentId = category.parentId,
            archived = category.archived
        )
        if (category.id == 0L) {
            categoryDao.insertCategory(entity)
        } else {
            categoryDao.updateCategory(entity)
            category.id
        }
    }

    override suspend fun archiveCategory(id: Long, archived: Boolean) = withContext(ioDispatcher) {
        categoryDao.setCategoryArchived(id, archived)
    }

    private fun CategoryEntity.toDomain() = Category(
        id = id,
        name = name,
        kind = runCatching { TransactionType.valueOf(kind) }.getOrDefault(TransactionType.EXPENSE),
        iconKey = iconKey,
        colour = colour,
        parentId = parentId,
        archived = archived
    )
}

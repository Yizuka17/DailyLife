package com.yizuka17.dailylife.core.data.repository

import android.content.Context
import com.yizuka17.dailylife.core.data.local.dao.TransactionCategoryDao
import com.yizuka17.dailylife.core.data.local.entity.TransactionCategoryEntity
import com.yizuka17.dailylife.core.ui.model.CategoryFlow
import com.yizuka17.dailylife.core.ui.model.TransactionCategory
import com.yizuka17.dailylife.core.ui.model.TransactionCategoryRepository
import com.yizuka17.dailylife.core.ui.model.TransactionCategoryType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import java.util.UUID

@Singleton
class TransactionCategoryDataRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val categoryDao: TransactionCategoryDao,
) {
    fun observeEnabledCategories(type: CategoryFlow): Flow<List<TransactionCategory>> =
        categoryDao.observeEnabledCategories(type.storageValue)
            .map { entities ->
                ensureSeededIfNeeded()
                entities.map { entity ->
                    with(TransactionCategoryRepository) { entity.toUiModel(context) }
                }
            }

    fun observeAllCategories(type: CategoryFlow): Flow<List<TransactionCategory>> =
        categoryDao.observeAllCategories(type.storageValue)
            .map { entities ->
                ensureSeededIfNeeded()
                entities.map { entity ->
                    with(TransactionCategoryRepository) { entity.toUiModel(context) }
                }
            }

    fun observeCategories(type: CategoryFlow): Flow<List<TransactionCategory>> =
        observeEnabledCategories(type)

    suspend fun addCategory(name: String, type: CategoryFlow): Result<Unit> = runCatching {
        val trimmedName = name.trim()
        require(trimmedName.isNotBlank()) { "Category name cannot be empty" }
        val sortOrder = categoryDao.nextSortOrder(type.storageValue)
        val id = "custom_${type.storageValue}_${UUID.randomUUID().toString().replace("-", "")}" 
        categoryDao.insertCategory(
            TransactionCategoryEntity(
                id = id,
                name = trimmedName,
                type = type.storageValue,
                iconKey = TransactionCategoryRepository.DEFAULT_CUSTOM_ICON_KEY,
                sortOrder = sortOrder,
                isBuiltin = false,
                isDeleted = false,
            )
        )
    }

    suspend fun renameCategory(categoryId: String, newName: String): Result<Unit> = runCatching {
        val trimmedName = newName.trim()
        require(trimmedName.isNotBlank()) { "Category name cannot be empty" }
        val category = categoryDao.getCategoryById(categoryId) ?: error("Category not found")
        categoryDao.updateCategory(category.copy(name = trimmedName))
    }

    suspend fun setCategoryEnabled(categoryId: String, enabled: Boolean): CategoryEnabledResult {
        val category = categoryDao.getCategoryById(categoryId) ?: return CategoryEnabledResult.NotFound
        if (!enabled) {
            val transactionCount = categoryDao.countTransactionsByCategory(categoryId)
            if (transactionCount > 0) return CategoryEnabledResult.HasTransactions(transactionCount)
        }
        categoryDao.updateCategory(category.copy(isDeleted = !enabled))
        return if (enabled) CategoryEnabledResult.Enabled else CategoryEnabledResult.Disabled
    }

    suspend fun deleteCategory(categoryId: String): DeleteCategoryResult {
        val category = categoryDao.getCategoryById(categoryId) ?: return DeleteCategoryResult.NotFound
        if (category.isBuiltin) return DeleteCategoryResult.BuiltinCategory
        val transactionCount = categoryDao.countTransactionsByCategory(categoryId)
        if (transactionCount > 0) return DeleteCategoryResult.HasTransactions(transactionCount)
        categoryDao.deleteCategoryById(categoryId)
        return DeleteCategoryResult.Success
    }

    suspend fun reorderCategories(categoryIds: List<String>) {
        val reorderedCategories = categoryIds.mapIndexedNotNull { index, categoryId ->
            categoryDao.getCategoryById(categoryId)?.copy(sortOrder = index)
        }
        categoryDao.updateCategories(reorderedCategories)
    }

    suspend fun getAllCategoriesSnapshot(): List<TransactionCategoryEntity> {
        ensureSeededIfNeeded()
        return categoryDao.getAllCategoriesSnapshot()
    }

    suspend fun ensureSeededIfNeeded() {
        if (categoryDao.countCategories() > 0) return
        categoryDao.insertCategories(buildBuiltinEntities())
    }

    private fun buildBuiltinEntities(): List<TransactionCategoryEntity> {
        val expenseIds = TransactionCategoryRepository.expenseCategoryIds
        val incomeIds = TransactionCategoryRepository.incomeCategoryIds
        return buildList {
            expenseIds.forEachIndexed { index, id ->
                TransactionCategoryType.fromValue(id)?.let { type ->
                    add(type.toEntity(context, CategoryFlow.EXPENSE, index))
                }
            }
            incomeIds.forEachIndexed { index, id ->
                TransactionCategoryType.fromValue(id)?.let { type ->
                    add(type.toEntity(context, CategoryFlow.INCOME, index))
                }
            }
        }
    }

    private fun TransactionCategoryType.toEntity(
        context: Context,
        categoryFlow: CategoryFlow,
        index: Int,
    ): TransactionCategoryEntity = TransactionCategoryEntity(
        id = id,
        name = context.getString(labelRes),
        type = categoryFlow.storageValue,
        iconKey = id,
        sortOrder = index,
        isBuiltin = true,
        isDeleted = false,
    )

    companion object {
        val CategoryFlow.storageValue: String
            get() = name.lowercase(Locale.ROOT)
    }
}

sealed interface DeleteCategoryResult {
    data object Success : DeleteCategoryResult
    data object NotFound : DeleteCategoryResult
    data object BuiltinCategory : DeleteCategoryResult
    data class HasTransactions(val count: Int) : DeleteCategoryResult
}

sealed interface CategoryEnabledResult {
    data object Enabled : CategoryEnabledResult
    data object Disabled : CategoryEnabledResult
    data object NotFound : CategoryEnabledResult
    data class HasTransactions(val count: Int) : CategoryEnabledResult
}

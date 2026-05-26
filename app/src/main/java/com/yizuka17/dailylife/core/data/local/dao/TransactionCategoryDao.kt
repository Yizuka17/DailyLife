package com.yizuka17.dailylife.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yizuka17.dailylife.core.data.local.entity.TransactionCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionCategoryDao {

    @Query("SELECT * FROM transaction_categories WHERE type = :type AND isDeleted = 0 ORDER BY sortOrder ASC, name ASC")
    fun observeEnabledCategories(type: String): Flow<List<TransactionCategoryEntity>>

    @Query("SELECT * FROM transaction_categories WHERE type = :type ORDER BY sortOrder ASC, name ASC")
    fun observeAllCategories(type: String): Flow<List<TransactionCategoryEntity>>

    @Query("SELECT * FROM transaction_categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: String): TransactionCategoryEntity?

    @Query("SELECT * FROM transaction_categories WHERE id IN (:ids)")
    suspend fun getCategoriesByIds(ids: List<String>): List<TransactionCategoryEntity>

    @Query("SELECT * FROM transaction_categories WHERE id IN (:ids)")
    fun observeCategoriesByIds(ids: List<String>): Flow<List<TransactionCategoryEntity>>

    @Query("SELECT * FROM transaction_categories ORDER BY type ASC, sortOrder ASC, name ASC")
    suspend fun getAllCategoriesSnapshot(): List<TransactionCategoryEntity>

    @Query("DELETE FROM transaction_categories")
    suspend fun deleteAllCategories()

    @Query("SELECT COUNT(*) FROM transaction_categories")
    suspend fun countCategories(): Int

    @Query("SELECT COUNT(*) FROM transactions WHERE category = :categoryId AND isDeleted = 0")
    suspend fun countTransactionsByCategory(categoryId: String): Int

    @Query("SELECT COALESCE(MAX(sortOrder), -1) + 1 FROM transaction_categories WHERE type = :type")
    suspend fun nextSortOrder(type: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: TransactionCategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<TransactionCategoryEntity>)

    @Update
    suspend fun updateCategory(category: TransactionCategoryEntity)

    @Update
    suspend fun updateCategories(categories: List<TransactionCategoryEntity>)

    @Query("DELETE FROM transaction_categories WHERE id = :id")
    suspend fun deleteCategoryById(id: String)
}

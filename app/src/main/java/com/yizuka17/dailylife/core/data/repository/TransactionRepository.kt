package com.yizuka17.dailylife.core.data.repository

import androidx.room.withTransaction
import com.yizuka17.dailylife.core.data.local.dao.AssetAccountDao
import com.yizuka17.dailylife.core.data.local.dao.TransactionCategoryDao
import com.yizuka17.dailylife.core.data.local.dao.TransactionDao
import com.yizuka17.dailylife.core.data.local.database.AppDatabase
import com.yizuka17.dailylife.core.data.local.entity.AssetAccountEntity
import com.yizuka17.dailylife.core.data.local.entity.TransactionCategoryEntity
import com.yizuka17.dailylife.core.data.local.entity.TransactionEntity
import com.yizuka17.dailylife.core.data.local.model.DailyTransactionSummary
import com.yizuka17.dailylife.core.data.local.model.TransactionWithDay
import com.yizuka17.dailylife.core.di.ApplicationScope
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TransactionRepository @Inject constructor(
    private val database: AppDatabase,
    private val transactionDao: TransactionDao,
    private val assetAccountDao: AssetAccountDao,
    private val transactionCategoryDao: TransactionCategoryDao,
    @ApplicationScope private val applicationScope: CoroutineScope
) {

    private val allTransactionsState = MutableStateFlow(emptyList<TransactionEntity>())

    init {
        applicationScope.launch(Dispatchers.IO) {
            val snapshot = transactionDao.getAllTransactionsSnapshot()
                .sortedBy(TransactionEntity::date)
            allTransactionsState.value = snapshot

            transactionDao.getAllTransactions()
                .map { entities -> entities.sortedBy(TransactionEntity::date) }
                .collect { entities ->
                    allTransactionsState.value = entities
                }
        }
    }

    fun observeAllTransactions(): StateFlow<List<TransactionEntity>> {
        return allTransactionsState.asStateFlow()
    }

    fun getAllTransactions(): Flow<List<TransactionEntity>> {
        return transactionDao.getAllTransactions()
    }

    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByDateRange(startDate, endDate)
    }

    fun getTransactionsWithDayRange(
        startDate: Long,
        endDate: Long
    ): Flow<List<TransactionWithDay>> {
        return transactionDao.getTransactionsWithDayRange(startDate, endDate)
    }

    fun getDailySummaries(
        startDate: Long,
        endDate: Long
    ): Flow<List<DailyTransactionSummary>> {
        return transactionDao.getDailySummaries(startDate, endDate)
    }

    fun getTransactionById(id: Int): Flow<TransactionEntity?> {
        return transactionDao.getTransactionById(id)
    }

    suspend fun getTransactionsSnapshot(): List<TransactionEntity> {
        return transactionDao.getAllTransactionsSnapshot()
            .sortedBy(TransactionEntity::date)
    }

    suspend fun getTransactionsByDateRangeSnapshot(startDate: Long, endDate: Long): List<TransactionEntity> {
        return transactionDao.getTransactionsByDateRangeSnapshot(startDate, endDate)
            .sortedBy(TransactionEntity::date)
    }

    suspend fun replaceAllBackupData(
        transactions: List<TransactionEntity>,
        categories: List<TransactionCategoryEntity>,
        accounts: List<AssetAccountEntity>,
    ) {
        database.withTransaction {
            transactionDao.deleteAllTransactions()
            transactionCategoryDao.deleteAllCategories()
            assetAccountDao.deleteAllAccounts()
            if (categories.isNotEmpty()) {
                transactionCategoryDao.insertCategories(categories)
            }
            if (accounts.isNotEmpty()) {
                assetAccountDao.insertAccounts(accounts)
            }
            if (transactions.isNotEmpty()) {
                transactionDao.insertTransactions(transactions)
            }
        }
    }

    suspend fun insertTransaction(transaction: TransactionEntity) {
        database.withTransaction {
            val insertedId = transactionDao.insertTransaction(transaction).toInt()
            applyAccountEffect(transaction)
            insertedId
        }
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        database.withTransaction {
            val oldTransaction = transactionDao.getTransactionByIdSnapshot(transaction.id)
            transactionDao.updateTransaction(transaction)
            revertAccountEffect(oldTransaction)
            applyAccountEffect(transaction)
        }
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        database.withTransaction {
            transactionDao.updateTransaction(transaction.copy(isDeleted = true))
            revertAccountEffect(transaction)
        }
    }

    suspend fun pruneDeletedTransactions(olderThan: Long) {
        transactionDao.pruneDeletedTransactions(olderThan)
    }

    private suspend fun applyAccountEffect(transaction: TransactionEntity?) {
        if (transaction == null || transaction.isDeleted) return
        val accountId = transaction.accountId ?: return
        assetAccountDao.adjustBalance(accountId, transaction.amount)
    }

    private suspend fun revertAccountEffect(transaction: TransactionEntity?) {
        if (transaction == null || transaction.isDeleted) return
        val accountId = transaction.accountId ?: return
        assetAccountDao.adjustBalance(accountId, -transaction.amount)
    }
}

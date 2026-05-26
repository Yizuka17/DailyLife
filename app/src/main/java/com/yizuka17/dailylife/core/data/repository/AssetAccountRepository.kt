package com.yizuka17.dailylife.core.data.repository

import androidx.room.withTransaction
import com.yizuka17.dailylife.core.data.local.dao.AssetAccountDao
import com.yizuka17.dailylife.core.data.local.database.AppDatabase
import com.yizuka17.dailylife.core.data.local.entity.AssetAccountEntity
import com.yizuka17.dailylife.core.data.local.entity.AssetAccountType
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class AssetAccountRepository @Inject constructor(
    private val database: AppDatabase,
    private val assetAccountDao: AssetAccountDao,
) {

    fun observeAccounts(): Flow<List<AssetAccountEntity>> = assetAccountDao.observeActiveAccounts()

    suspend fun getAccount(accountId: Int): AssetAccountEntity? = assetAccountDao.getActiveAccountById(accountId)

    suspend fun getAccountIncludingDeleted(accountId: Int): AssetAccountEntity? = assetAccountDao.getAccountById(accountId)

    suspend fun createAccount(
        name: String,
        type: AssetAccountType,
        balance: Double,
        isDefault: Boolean = false,
    ) {
        val trimmedName = name.trim()
        require(trimmedName.isNotBlank()) { "Account name cannot be empty" }

        database.withTransaction {
            if (isDefault) {
                assetAccountDao.clearDefaultAccounts()
            }
            assetAccountDao.insertAccount(
                AssetAccountEntity(
                    name = trimmedName,
                    type = type,
                    balance = balance,
                    sortOrder = assetAccountDao.nextSortOrder(),
                    isDefault = isDefault,
                )
            )
        }
    }

    suspend fun updateAccount(
        accountId: Int,
        name: String,
        type: AssetAccountType,
        balance: Double,
        isDefault: Boolean,
    ) {
        val trimmedName = name.trim()
        require(trimmedName.isNotBlank()) { "Account name cannot be empty" }

        database.withTransaction {
            val account = assetAccountDao.getActiveAccountById(accountId) ?: return@withTransaction
            if (isDefault) {
                assetAccountDao.clearDefaultAccounts()
            }
            assetAccountDao.updateAccount(
                account.copy(
                    name = trimmedName,
                    type = type,
                    balance = balance,
                    isDefault = isDefault,
                )
            )
        }
    }

    suspend fun reorderAccounts(accountIds: List<Int>) {
        database.withTransaction {
            val reorderedAccounts = accountIds.mapIndexedNotNull { index, accountId ->
                assetAccountDao.getActiveAccountById(accountId)?.copy(sortOrder = index)
            }
            assetAccountDao.updateAccounts(reorderedAccounts)
        }
    }

    suspend fun softDeleteAccount(accountId: Int) {
        val account = assetAccountDao.getActiveAccountById(accountId) ?: return
        assetAccountDao.updateAccount(account.copy(isDeleted = true, isDefault = false))
    }

    suspend fun getAllAccountsSnapshot(): List<AssetAccountEntity> {
        ensureDefaultAccountsIfNeeded()
        return assetAccountDao.getAllAccountsSnapshot()
    }

    suspend fun ensureDefaultAccountsIfNeeded() {
        val existingDefault = assetAccountDao.getDefaultActiveAccount()
        if (existingDefault != null) return

        database.withTransaction {
            val recheckedDefault = assetAccountDao.getDefaultActiveAccount()
            if (recheckedDefault != null) return@withTransaction

            val defaults = listOf(
                AssetAccountEntity(
                    name = "银行卡",
                    type = AssetAccountType.BANK_CARD,
                    balance = 0.0,
                    sortOrder = 0,
                    isDefault = true,
                ),
                AssetAccountEntity(
                    name = "现金",
                    type = AssetAccountType.CASH,
                    balance = 0.0,
                    sortOrder = 1,
                ),
                AssetAccountEntity(
                    name = "支付宝",
                    type = AssetAccountType.ALIPAY,
                    balance = 0.0,
                    sortOrder = 2,
                ),
                AssetAccountEntity(
                    name = "微信",
                    type = AssetAccountType.WECHAT,
                    balance = 0.0,
                    sortOrder = 3,
                ),
            )
            defaults.forEach { assetAccountDao.insertAccount(it) }
        }
    }
}

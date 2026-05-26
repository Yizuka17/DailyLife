package com.yizuka17.dailylife.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yizuka17.dailylife.core.data.local.entity.AssetAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetAccountDao {

    @Query("SELECT * FROM asset_accounts WHERE isDeleted = 0 ORDER BY sortOrder ASC, id ASC")
    fun observeActiveAccounts(): Flow<List<AssetAccountEntity>>

    @Query("SELECT * FROM asset_accounts WHERE id = :id AND isDeleted = 0")
    suspend fun getActiveAccountById(id: Int): AssetAccountEntity?

    @Query("SELECT * FROM asset_accounts WHERE isDeleted = 0 ORDER BY isDefault DESC, sortOrder ASC, id ASC LIMIT 1")
    suspend fun getDefaultActiveAccount(): AssetAccountEntity?

    @Query("SELECT * FROM asset_accounts ORDER BY sortOrder ASC, id ASC")
    suspend fun getAllAccountsSnapshot(): List<AssetAccountEntity>

    @Query("DELETE FROM asset_accounts")
    suspend fun deleteAllAccounts()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AssetAccountEntity>)

    @Query("SELECT COALESCE(MAX(sortOrder), -1) + 1 FROM asset_accounts")
    suspend fun nextSortOrder(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AssetAccountEntity): Long

    @Update
    suspend fun updateAccount(account: AssetAccountEntity)

    @Update
    suspend fun updateAccounts(accounts: List<AssetAccountEntity>)

    @Query("UPDATE asset_accounts SET balance = balance + :delta WHERE id = :accountId AND isDeleted = 0")
    suspend fun adjustBalance(accountId: Int, delta: Double)

    @Query("UPDATE asset_accounts SET balance = :balance WHERE id = :accountId AND isDeleted = 0")
    suspend fun setBalance(accountId: Int, balance: Double)

    @Query("UPDATE asset_accounts SET isDefault = 0")
    suspend fun clearDefaultAccounts()
}

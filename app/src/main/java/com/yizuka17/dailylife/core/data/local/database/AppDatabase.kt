package com.yizuka17.dailylife.core.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yizuka17.dailylife.core.data.local.dao.AssetAccountDao
import com.yizuka17.dailylife.core.data.local.dao.TransactionCategoryDao
import com.yizuka17.dailylife.core.data.local.dao.TransactionDao
import com.yizuka17.dailylife.core.data.local.entity.AssetAccountEntity
import com.yizuka17.dailylife.core.data.local.entity.TransactionCategoryEntity
import com.yizuka17.dailylife.core.data.local.entity.TransactionEntity
import java.util.concurrent.Executors

@Database(
    entities = [
        TransactionEntity::class,
        TransactionCategoryEntity::class,
        AssetAccountEntity::class,
    ],
    version = 5,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao

    abstract fun transactionCategoryDao(): TransactionCategoryDao

    abstract fun assetAccountDao(): AssetAccountDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val databaseExecutor by lazy {
            Executors.newFixedThreadPool(
                maxOf(2, Runtime.getRuntime().availableProcessors() / 2)
            )
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "daily_life_database"
                )
                    .setQueryExecutor(databaseExecutor)
                    .setTransactionExecutor(databaseExecutor)
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

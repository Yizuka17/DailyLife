package com.yizuka17.dailylife.core.di

import android.content.Context
import com.yizuka17.dailylife.core.data.analytics.TransactionAnalyticsRepository
import com.yizuka17.dailylife.core.data.local.dao.AssetAccountDao
import com.yizuka17.dailylife.core.data.local.dao.TransactionCategoryDao
import com.yizuka17.dailylife.core.data.local.dao.TransactionDao
import com.yizuka17.dailylife.core.data.local.database.AppDatabase
import com.yizuka17.dailylife.core.data.repository.TransactionRepository
import com.yizuka17.dailylife.core.common.StringProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Singleton
    @Provides
    fun provideTransactionDao(appDatabase: AppDatabase): TransactionDao {
        return appDatabase.transactionDao()
    }

    @Singleton
    @Provides
    fun provideTransactionCategoryDao(appDatabase: AppDatabase): TransactionCategoryDao {
        return appDatabase.transactionCategoryDao()
    }

    @Singleton
    @Provides
    fun provideAssetAccountDao(appDatabase: AppDatabase): AssetAccountDao {
        return appDatabase.assetAccountDao()
    }

    @Singleton
    @Provides
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @Singleton
    @Provides
    fun provideTransactionRepository(
        appDatabase: AppDatabase,
        transactionDao: TransactionDao,
        assetAccountDao: AssetAccountDao,
        transactionCategoryDao: TransactionCategoryDao,
        @ApplicationScope applicationScope: CoroutineScope
    ): TransactionRepository {
        return TransactionRepository(appDatabase, transactionDao, assetAccountDao, transactionCategoryDao, applicationScope)
    }

    @Singleton
    @Provides
    fun provideTransactionAnalyticsRepository(
        transactionRepository: TransactionRepository,
        stringProvider: StringProvider,
        @ApplicationScope applicationScope: CoroutineScope
    ): TransactionAnalyticsRepository {
        return TransactionAnalyticsRepository(
            transactionRepository = transactionRepository,
            stringProvider = stringProvider,
            applicationScope = applicationScope
        )
    }
}

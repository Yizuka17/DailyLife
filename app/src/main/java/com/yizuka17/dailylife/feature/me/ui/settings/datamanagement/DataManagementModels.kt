package com.yizuka17.dailylife.feature.me.ui.settings.datamanagement

import android.net.Uri
import androidx.annotation.StringRes
import com.yizuka17.dailylife.core.data.local.entity.AssetAccountType
import com.yizuka17.dailylife.core.data.preferences.UserPreferencesSnapshot
import java.time.LocalDate

const val DATA_MANAGEMENT_BACKUP_VERSION = 2

enum class DataManagementMode {
    BACKUP,
    RESTORE,
}

data class DataManagementUiState(
    val mode: DataManagementMode = DataManagementMode.BACKUP,
    val startDate: LocalDate = LocalDate.now().minusMonths(1),
    val endDate: LocalDate = LocalDate.now(),
    val includePreferences: Boolean = true,
    val encryptionEnabled: Boolean = false,
    val password: String = "",
    val confirmPassword: String = "",
    val isProcessing: Boolean = false,
    val processingMessageRes: Int? = null,
    val selectionCount: Int = 0,
    val selectionValid: Boolean = true,
    val lastBackupTimestamp: Long = 0L,
    val restoreUri: Uri? = null,
    val restoreFileName: String? = null,
    val restorePassword: String = "",
)

sealed interface DataManagementMessage {
    data class Resource(@StringRes val resId: Int, val args: List<Any> = emptyList()) : DataManagementMessage
    data class Plain(val value: String) : DataManagementMessage
}

sealed interface DataManagementEvent {
    data class ShowMessage(val message: DataManagementMessage) : DataManagementEvent
}

data class BackupMetadata(
    val startDateEpochMillis: Long,
    val endDateEpochMillis: Long,
    val generatedAtEpochMillis: Long,
    val appVersion: String,
    val itemCount: Int,
    val transactionCount: Int = itemCount,
    val categoryCount: Int = 0,
    val accountCount: Int = 0,
    val encrypted: Boolean,
)

data class BackupPayload(
    val metadata: BackupMetadata,
    val transactions: List<BackupTransaction>,
    val categories: List<BackupTransactionCategory> = emptyList(),
    val accounts: List<BackupAssetAccount> = emptyList(),
    val preferences: UserPreferencesSnapshot?,
)

data class BackupEnvelope(
    val version: Int = DATA_MANAGEMENT_BACKUP_VERSION,
    val encrypted: Boolean,
    val payload: EncryptedPayload? = null,
    val data: BackupPayload? = null,
)

data class EncryptedPayload(
    val cipherAlgorithm: String,
    val salt: String,
    val iv: String,
    val cipherText: String,
    val iterations: Int,
    val kdfAlgorithm: String,
)

data class BackupTransaction(
    val id: Int,
    val category: String,
    val description: String,
    val amount: Double,
    val mood: Int?,
    val source: String,
    val date: Long,
    val accountId: Int? = null,
    val isDeleted: Boolean = false,
)

data class BackupTransactionCategory(
    val id: String,
    val name: String,
    val type: String,
    val iconKey: String,
    val sortOrder: Int,
    val isBuiltin: Boolean,
    val isDeleted: Boolean,
)

data class BackupAssetAccount(
    val id: Int,
    val name: String,
    val type: AssetAccountType,
    val balance: Double,
    val sortOrder: Int,
    val isDefault: Boolean,
    val isDeleted: Boolean,
)

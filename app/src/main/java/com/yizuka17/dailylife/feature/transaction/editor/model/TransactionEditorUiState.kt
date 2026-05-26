package com.yizuka17.dailylife.feature.transaction.editor.model

import com.yizuka17.dailylife.core.data.local.entity.AssetAccountEntity
import com.yizuka17.dailylife.core.ui.model.TransactionCategory

data class TransactionEditorUiState(
    val amount: String = "",
    val categoryId: String = "",
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val isExpense: Boolean = true,
    val mood: String = "",
    val transactionId: Int? = null,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val categories: List<TransactionCategory> = emptyList(),
    val accounts: List<AssetAccountEntity> = emptyList(),
    val selectedAccountId: Int? = null,
)

sealed interface TransactionEditorEvent {
    data class ShowMessage(val message: String) : TransactionEditorEvent
    data class SaveSuccess(val savedAt: Long) : TransactionEditorEvent
}

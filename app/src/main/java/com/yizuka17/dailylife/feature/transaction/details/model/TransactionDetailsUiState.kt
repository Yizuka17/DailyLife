package com.yizuka17.dailylife.feature.transaction.details.model

import com.yizuka17.dailylife.core.data.local.entity.TransactionEntity

data class TransactionDetailsUiState(
    val isLoading: Boolean = true,
    val transaction: TransactionEntity? = null,
    val categoryNamesById: Map<String, String> = emptyMap(),
    val accountName: String? = null,
    val error: String? = null,
)

package com.yizuka17.dailylife.core.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.yizuka17.dailylife.core.data.local.entity.TransactionEntity

data class TransactionWithDay(
    @Embedded(prefix = "transaction_")
    val transaction: TransactionEntity,
    @ColumnInfo(name = "day_start_millis")
    val dayStartMillis: Long
)

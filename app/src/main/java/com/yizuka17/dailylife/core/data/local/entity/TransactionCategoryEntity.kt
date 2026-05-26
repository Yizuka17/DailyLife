package com.yizuka17.dailylife.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transaction_categories",
    indices = [
        Index(value = ["type", "isDeleted"]),
        Index(value = ["sortOrder"]),
    ]
)
data class TransactionCategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val iconKey: String = "more_horiz",
    val sortOrder: Int = 0,
    val isBuiltin: Boolean = false,
    val isDeleted: Boolean = false,
)

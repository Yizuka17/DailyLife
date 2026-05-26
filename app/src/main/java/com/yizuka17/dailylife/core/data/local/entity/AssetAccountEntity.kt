package com.yizuka17.dailylife.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.yizuka17.dailylife.core.data.local.converter.Converters

enum class AssetAccountType {
    BANK_CARD,
    CASH,
    ALIPAY,
    WECHAT,
    OTHER,
}

@Entity(
    tableName = "asset_accounts",
    indices = [
        Index(value = ["type", "sortOrder"]),
        Index(value = ["isDeleted"]),
    ],
)
@TypeConverters(Converters::class)
data class AssetAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val type: AssetAccountType,
    val balance: Double,
    val sortOrder: Int,
    val isDefault: Boolean = false,
    val isDeleted: Boolean = false,
)

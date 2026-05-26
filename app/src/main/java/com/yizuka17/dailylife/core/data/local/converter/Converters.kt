package com.yizuka17.dailylife.core.data.local.converter

import androidx.room.TypeConverter
import com.yizuka17.dailylife.core.data.local.entity.AssetAccountType
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromAssetAccountType(value: AssetAccountType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toAssetAccountType(value: String?): AssetAccountType? {
        return value?.let { AssetAccountType.valueOf(it) }
    }
}
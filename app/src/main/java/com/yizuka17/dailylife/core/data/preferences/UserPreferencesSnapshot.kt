package com.yizuka17.dailylife.core.data.preferences

data class UserPreferencesSnapshot(
    val themeMode: String,
    val dynamicColor: Boolean,
    val fingerprintLockEnabled: Boolean,
    val uiScale: Float,
    val fontScale: Float,
    val customFontEnabled: Boolean,
    val quickUsageReminderEnabled: Boolean,
    val quickUsageReminderTimeMinutes: Int,
    val languageCode: String,
    val profileDisplayName: String = "",
    val profileSignature: String = "",
    val profileAvatarUri: String = "",
    val profileAvatarBase64: String = "",
    val profileAvatarMimeType: String = "image/jpeg",
)

package com.yizuka17.dailylife.core.model

/**
 * Centralizes the default source metadata so it stays locale-agnostic.
 */
object TransactionSource {
    const val DEFAULT = "dailylife"

    private val legacyValues = setOf("dailylife", "DailyLife", "日簿记")

    fun isAppSource(value: String): Boolean {
        return legacyValues.any { it.equals(value, ignoreCase = true) }
    }
}

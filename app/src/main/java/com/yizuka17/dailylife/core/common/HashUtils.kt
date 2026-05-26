package com.yizuka17.dailylife.core.common

import java.security.MessageDigest
import java.util.Locale

/**
 * Common hash algorithm helpers.
 */
object HashUtils {

    fun md5(input: String, length: Int = 32, toUpperCase: Boolean = false): String {
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(input.toByteArray())
        val md5String = digest.joinToString("") {
            String.format("%02x", it)
        }

        val result = when (length) {
            16 -> md5String.substring(8, 24)
            else -> md5String
        }

        return if (toUpperCase) result.uppercase(Locale.ROOT) else result
    }
}

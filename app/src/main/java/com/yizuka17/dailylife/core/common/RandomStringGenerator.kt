package com.yizuka17.dailylife.core.common

import kotlin.random.Random

/**
 * Random string generator with configurable character sets.
 */
object RandomStringGenerator {

    fun generate(
        length: Int,
        includeLowerCase: Boolean = false,
        includeUpperCase: Boolean = false,
        includeDigits: Boolean = false,
        includePunctuation: Boolean = false
    ): String {
        val lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz"
        val upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val digits = "0123456789"
        val punctuation = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~"

        var allowedChars = ""
        if (includeLowerCase) allowedChars += lowerCaseLetters
        if (includeUpperCase) allowedChars += upperCaseLetters
        if (includeDigits) allowedChars += digits
        if (includePunctuation) allowedChars += punctuation

        return (1..length)
            .map { allowedChars.random(Random) }
            .joinToString("")
    }
}

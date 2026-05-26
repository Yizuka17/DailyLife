package com.yizuka17.dailylife.core.common

/**
 * Formats exception stack traces into readable text.
 */
object ExceptionFormatter {
    fun format(e: Exception): String {
        val sb = StringBuilder()
        sb.append(e.toString()).append('\n')
        for (element in e.stackTrace) {
            sb.append(element.toString()).append('\n')
        }
        return sb.toString()
    }
}

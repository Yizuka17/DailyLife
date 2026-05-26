package com.yizuka17.dailylife.core.common

/**
 * Semantic version comparison helper.
 */
object VersionComparator {

    fun isNewer(current: String, candidate: String): Boolean {
        return versionStringToDouble(current) < versionStringToDouble(candidate)
    }

    private fun versionStringToDouble(version: String): Double {
        val versionArray = version.split(".")
        val integerPart = versionArray[0]
        val decimalPart = versionArray.drop(1).joinToString("")
        return "$integerPart.$decimalPart".toDouble()
    }
}

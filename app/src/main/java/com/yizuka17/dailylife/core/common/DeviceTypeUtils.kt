package com.yizuka17.dailylife.core.common

import android.util.DisplayMetrics
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Device type helper methods.
 */
object DeviceTypeUtils {
    fun isTablet(displayMetrics: DisplayMetrics): Boolean {
        val screenWidthInches = displayMetrics.widthPixels / displayMetrics.xdpi
        val screenHeightInches = displayMetrics.heightPixels / displayMetrics.ydpi
        val diagonalInches = sqrt(
            screenWidthInches.toDouble().pow(2.0) + screenHeightInches.toDouble().pow(2.0)
        )
        return diagonalInches >= 7
    }
}

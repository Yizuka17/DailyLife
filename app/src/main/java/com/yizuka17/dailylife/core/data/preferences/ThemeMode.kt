package com.yizuka17.dailylife.core.data.preferences

import androidx.annotation.StringRes
import com.yizuka17.dailylife.R

enum class ThemeMode(@StringRes val resId: Int) {
    SYSTEM(R.string.theme_mode_system),
    LIGHT(R.string.theme_mode_light),
    DARK(R.string.theme_mode_dark)
}
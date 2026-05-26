package com.yizuka17.dailylife.core.ui.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Extended color values not covered by MaterialTheme.colorScheme.
 */
@Immutable
data class ExtendedColorScheme(
    val headerContainer: Color,
    val onHeaderContainer: Color,
    val success: Color
)

/**
 * Provides extended colors through CompositionLocal.
 */
val LocalExtendedColorScheme = staticCompositionLocalOf {
    ExtendedColorScheme(
        headerContainer = Color.Unspecified,
        onHeaderContainer = Color.Unspecified,
        success = Color.Unspecified
    )
}

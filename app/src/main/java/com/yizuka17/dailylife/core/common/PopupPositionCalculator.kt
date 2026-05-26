package com.yizuka17.dailylife.core.common

/**
 * Calculates horizontal popup offsets to keep popups inside the screen.
 */
object PopupPositionCalculator {

    fun calculateOffset(
        density: Float,
        clickOffsetX: Float,
        popupWidth: Int,
        screenWidthDp: Int
    ): Float {
        val estimatedPosition = clickOffsetX / (density + 0.245f)
        return when {
            estimatedPosition < 42 + popupWidth / 2 -> 16f
            estimatedPosition > (screenWidthDp - popupWidth) -> (screenWidthDp - popupWidth - 42).toFloat()
            else -> estimatedPosition - popupWidth + 84
        }
    }
}

@Deprecated(
    message = "Use PopupPositionCalculator.calculateOffset instead",
    replaceWith = ReplaceWith("PopupPositionCalculator.calculateOffset(density, clickOffsetX, popupWidth, screenWidthDp)")
)
fun calPopupLocation(
    density: Float,
    clickOffsetX: Float,
    popupWidth: Int,
    screenWidthDp: Int
): Float = PopupPositionCalculator.calculateOffset(density, clickOffsetX, popupWidth, screenWidthDp)

fun calculatePopupOffset(
    density: Float,
    clickOffsetX: Float,
    popupWidth: Int,
    screenWidthDp: Int
): Float = PopupPositionCalculator.calculateOffset(density, clickOffsetX, popupWidth, screenWidthDp)

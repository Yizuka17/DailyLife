package com.yizuka17.dailylife.core.common

import android.content.Context
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides string resources outside @Composable functions.
 */
@Singleton
class StringProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getString(@StringRes resId: Int): String = context.getString(resId)

    fun getString(@StringRes resId: Int, vararg args: Any): String =
        context.getString(resId, *args)
}

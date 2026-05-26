package com.yizuka17.dailylife.app.main.intent

import android.content.Intent

object NavigationIntent {
    private const val EXTRA_NAVIGATE_ROUTE = "extra_navigate_route"

    fun resolveRoute(intent: Intent?): String? {
        if (intent == null) return null
        return intent.getStringExtra(EXTRA_NAVIGATE_ROUTE)
    }

    fun clearExtras(intent: Intent) {
        intent.removeExtra(EXTRA_NAVIGATE_ROUTE)
    }
}

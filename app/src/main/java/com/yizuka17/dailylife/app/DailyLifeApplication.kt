package com.yizuka17.dailylife.app

import android.app.Application
import android.content.Context
import com.yizuka17.dailylife.core.common.readPersistedLanguageCode
import com.yizuka17.dailylife.core.common.wrapContextWithLanguage
import com.yizuka17.dailylife.core.domain.language.LanguageUseCase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class DailyLifeApplication : Application() {

    @Inject lateinit var languageUseCase: LanguageUseCase

    override fun attachBaseContext(base: Context) {
        val languageCode = readPersistedLanguageCode(base)
        val wrapped = wrapContextWithLanguage(base, languageCode)
        super.attachBaseContext(wrapped)
    }

    override fun onCreate() {
        super.onCreate()
        applyPersistedLocale()
    }

    private fun applyPersistedLocale() {
        languageUseCase.reapplyPersistedLanguage()
    }
}

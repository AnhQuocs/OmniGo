package com.example.omnigo.utils

import android.content.Context
import android.os.Build
import android.os.LocaleList
import com.example.omnigo.features.language.domain.model.AppLanguage
import java.util.Locale

object LanguageManager {
    fun setAppLocale(context: Context, language: AppLanguage): Context {
        val locale = Locale(language.code)
        Locale.setDefault(locale)

        val config = context.resources.configuration

        config.setLocales(LocaleList(locale))

        return context.createConfigurationContext(config)
    }
}
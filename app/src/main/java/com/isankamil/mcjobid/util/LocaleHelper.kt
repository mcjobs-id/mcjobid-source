package com.isankamil.mcjobid.util

import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

object LocaleHelper {

    /**
     * Apply locale change at runtime using AppCompatDelegate (API 13+).
     * This is the modern, recommended approach — no Activity recreation needed.
     * The change is automatically persisted by AppCompat across app restarts.
     */
    fun applyLocale(context: Context, languageCode: String) {
        val locale = Locale.forLanguageTag(languageCode)
        val localeList = LocaleListCompat.create(locale)
        // This call triggers a UI recomposition automatically on Android 13+
        // and falls back to Activity recreation on older versions
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    /**
     * Legacy fallback — updates configuration context directly.
     * Use this only for wrapping context in attachBaseContext if needed.
     */
    fun wrapContext(context: Context, languageCode: String): Context {
        val locale = Locale.forLanguageTag(languageCode)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            config.setLocales(LocaleList(locale))
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
}

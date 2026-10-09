package com.rejowan.pdfreaderpro.util

import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * The app's own language setting. AppCompat applies it on every Android
 * version and, from Android 13, keeps it in step with the system's per-app
 * language screen.
 */
object AppLanguage {

    /** Languages the app is translated into; English is the default resources. */
    val supported = listOf("en", "bn", "de", "es", "fr", "ga", "it", "ja", "pl", "pt", "ru", "tr")

    /** The chosen language tag, or null when the app follows the system. */
    fun current(): String? = AppCompatDelegate.getApplicationLocales()[0]?.language

    /** Switches the app to [tag], or back to the system language when null. Open screens restart in it. */
    fun set(tag: String?) {
        AppCompatDelegate.setApplicationLocales(
            if (tag == null) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(tag)
        )
    }

    /** The language's name in itself, e.g. "Deutsch" or "日本語", as people look for their own. */
    fun nativeName(tag: String): String {
        val locale = Locale.forLanguageTag(tag)
        return locale.getDisplayLanguage(locale).replaceFirstChar { it.titlecase(locale) }
    }

    /** The language's name in [inLanguage], e.g. "German" while the app is in English. */
    fun localizedName(tag: String, inLanguage: Locale = Locale.getDefault()): String =
        Locale.forLanguageTag(tag).getDisplayLanguage(inLanguage).replaceFirstChar { it.titlecase(inLanguage) }

    /**
     * Before Android 13, AppCompat changes the language of the screens but not
     * the process default locale, which dates and relative times read. This
     * brings the default in line with the chosen language.
     */
    fun syncDefaultLocale() {
        val chosen = AppCompatDelegate.getApplicationLocales()[0]
        Locale.setDefault(chosen ?: Resources.getSystem().configuration.locales[0])
    }
}

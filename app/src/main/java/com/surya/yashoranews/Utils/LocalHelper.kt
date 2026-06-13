package com.surya.yashoranews.Utils

import android.content.Context

object LocaleHelper {
    private const val PREFS_NAME = "YashoraPrefs"
    private const val KEY_LANG = "selected_lang"

    fun setLanguage(context: Context, lang: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, lang).apply()
    }

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Default "hi" hi rahega, par niche hum list se change karenge
        return prefs.getString(KEY_LANG, "hi") ?: "hi"
    }
}
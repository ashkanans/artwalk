package io.ashkanans.artwalk


import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeUtils {
    private const val THEME_PREFERENCE = "theme_preference"
    private const val THEME_KEY = "theme_key"

    const val THEME_SYSTEM_DEFAULT = 0
    const val THEME_LIGHT = 1
    const val THEME_DARK = 2

    fun applyTheme(theme: Int) {
        when (theme) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun saveThemePreference(context: Context, theme: Int) {
        val sharedPreferences = context.getSharedPreferences(THEME_PREFERENCE, Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt(THEME_KEY, theme).apply()
    }

    fun getThemePreference(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences(THEME_PREFERENCE, Context.MODE_PRIVATE)
        return sharedPreferences.getInt(THEME_KEY, THEME_SYSTEM_DEFAULT)
    }
}

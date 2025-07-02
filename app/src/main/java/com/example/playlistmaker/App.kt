package com.example.playlistmaker

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class App : Application() {
    var darkTheme = false
        private set

    override fun onCreate() {
        super.onCreate()
        val sharedPreferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        darkTheme = sharedPreferences.getBoolean(DARK_THEME_KEY, false)

        switchTheme(darkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
        val sharedPreferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean(DARK_THEME_KEY, darkThemeEnabled)
            apply()
        }
    }
    companion object {
        const val APP_PREFERENCES = "app_preferences"
        const val DARK_THEME_KEY = "dark_theme"
    }
}
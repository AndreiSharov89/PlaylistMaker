package com.example.playlistmaker.settings.data

import android.content.SharedPreferences
import com.example.playlistmaker.PrefsKeys
import com.example.playlistmaker.settings.domain.SettingsRepository
import com.example.playlistmaker.settings.domain.ThemeSettings

class SettingsRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : SettingsRepository {

    override fun getThemeSettings(): ThemeSettings {
        return ThemeSettings(isDarkTheme = sharedPreferences.getBoolean(PrefsKeys.THEME, false))
    }

    override fun updateThemeSetting(settings: ThemeSettings) {
        sharedPreferences.edit().putBoolean(PrefsKeys.THEME, settings.isDarkTheme).apply()
    }
}

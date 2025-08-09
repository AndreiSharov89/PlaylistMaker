package com.example.playlistmaker.data.settings

import android.content.SharedPreferences
import com.example.playlistmaker.PrefsKeys
import com.example.playlistmaker.domain.repositories.ThemeRepository

class ThemeRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : ThemeRepository {

    override fun isDarkThemeEnabled(): Boolean {
        return sharedPreferences.getBoolean(PrefsKeys.THEME, false)
    }

    override fun setDarkThemeEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(PrefsKeys.THEME, enabled).apply()
    }
}
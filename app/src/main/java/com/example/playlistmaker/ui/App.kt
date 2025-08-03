package com.example.playlistmaker.ui

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.Creator
import com.example.playlistmaker.PrefsKeys

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Creator.initApplication(this)
        val sharedPrefs = Creator.provideSharedPreferences(PrefsKeys.THEME)
        val isDarkTheme = sharedPrefs.getBoolean(PrefsKeys.THEME, false)

        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
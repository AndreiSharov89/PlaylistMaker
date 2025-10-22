package com.example.playlistmaker.settings.domain

class SettingsInteractorImpl(private val settingsRepository: SettingsRepository) :
    SettingsInteractor {
    override fun isDarkThemeEnabled(): Boolean {
        return settingsRepository.getThemeSettings().isDarkTheme
    }

    override fun setDarkThemeEnabled(enabled: Boolean) {
        val settings = settingsRepository.getThemeSettings().copy(isDarkTheme = enabled)
        settingsRepository.updateThemeSetting(settings)
    }
}

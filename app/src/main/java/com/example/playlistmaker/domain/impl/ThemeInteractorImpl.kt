package com.example.playlistmaker.domain.impl

import com.example.playlistmaker.domain.interactors.ThemeInteractor
import com.example.playlistmaker.domain.repositories.ThemeRepository

class ThemeInteractorImpl(
    private val repository: ThemeRepository
) : ThemeInteractor {

    override fun isDarkThemeEnabled(): Boolean {
        return repository.isDarkThemeEnabled()
    }

    override fun setDarkThemeEnabled(enabled: Boolean) {
        repository.setDarkThemeEnabled(enabled)
    }
}
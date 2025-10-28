package com.example.playlistmaker.settings.ui

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.settings.domain.SettingsInteractor
import com.example.playlistmaker.sharing.domain.SharingInteractor

class SettingsViewModel(
    private val sharingInteractor: SharingInteractor,
    private val settingsInteractor: SettingsInteractor,
) : ViewModel() {

    private val isDarkThemeLiveData = MutableLiveData<Boolean>()
    fun observeIsDarkTheme(): LiveData<Boolean> = isDarkThemeLiveData

    init {
        isDarkThemeLiveData.value = settingsInteractor.isDarkThemeEnabled()
    }

    fun onThemeSwitchToggled(isChecked: Boolean) {
        settingsInteractor.setDarkThemeEnabled(isChecked)
        isDarkThemeLiveData.value = isChecked
    }

    fun shareApp() {
        sharingInteractor.shareApp()
    }

    fun openSupport() {
        sharingInteractor.openSupport()
    }

    fun openTerms() {
        sharingInteractor.openTerms()
    }

    companion object {
        fun getViewModelFactory(): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                Creator.initApplication(application)
                SettingsViewModel(
                    sharingInteractor = Creator.provideSharingInteractor(),
                    settingsInteractor = Creator.provideSettingsInteractor()
                )
            }
        }
    }
}

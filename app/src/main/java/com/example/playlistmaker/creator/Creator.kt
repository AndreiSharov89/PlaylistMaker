package com.example.playlistmaker.creator

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import com.example.playlistmaker.PrefsKeys
import com.example.playlistmaker.data.HistoryRepositoryImpl
import com.example.playlistmaker.data.TrackSearchRepositoryImpl
import com.example.playlistmaker.data.network.RetrofitNetworkClient
import com.example.playlistmaker.data.player.PlayerMediaPlayer
import com.example.playlistmaker.domain.impl.HistoryInteractorImpl
import com.example.playlistmaker.domain.impl.PlayerInteractorImpl
import com.example.playlistmaker.domain.impl.TrackSearchInteractorImpl
import com.example.playlistmaker.domain.interactors.HistoryInteractor
import com.example.playlistmaker.domain.interactors.PlayerInteractor
import com.example.playlistmaker.domain.interactors.TrackSearchInteractor
import com.example.playlistmaker.domain.repositories.PlayerRepository
import com.example.playlistmaker.domain.repositories.SearchHistoryRepository
import com.example.playlistmaker.domain.repositories.TrackSearchRepository
import com.example.playlistmaker.settings.data.SettingsRepositoryImpl
import com.example.playlistmaker.settings.domain.SettingsInteractor
import com.example.playlistmaker.settings.domain.SettingsInteractorImpl
import com.example.playlistmaker.settings.domain.SettingsRepository
import com.example.playlistmaker.sharing.data.ExternalNavigatorImpl
import com.example.playlistmaker.sharing.data.SharingRepositoryImpl
import com.example.playlistmaker.sharing.domain.ExternalNavigatorRepository
import com.example.playlistmaker.sharing.domain.SharingInteractor
import com.example.playlistmaker.sharing.domain.SharingInteractorImpl
import com.example.playlistmaker.sharing.domain.SharingRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Creator {
    private lateinit var application: Application

    fun initApplication(application: Application) {
        this.application = application
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun provideNetworkClient(): RetrofitNetworkClient {
        return RetrofitNetworkClient(retrofit)
    }

    private fun getTrackSearchRepository(): TrackSearchRepository {
        return TrackSearchRepositoryImpl(provideNetworkClient())
    }

    fun provideTrackSearchInteractor(): TrackSearchInteractor {
        return TrackSearchInteractorImpl(getTrackSearchRepository())
    }

    private fun provideMediaPlayer(): MediaPlayer {
        return MediaPlayer()
    }

    private fun getPlayerRepository(): PlayerRepository {
        return PlayerMediaPlayer(provideMediaPlayer())
    }

    fun providePlayerInteractor(): PlayerInteractor {
        return PlayerInteractorImpl(getPlayerRepository())
    }

    private fun getSettingsRepository(): SettingsRepository {
        val sharedPreferences =
            application.getSharedPreferences(PrefsKeys.THEME, Context.MODE_PRIVATE)
        return SettingsRepositoryImpl(sharedPreferences)
    }

    fun provideSettingsInteractor(): SettingsInteractor {
        return SettingsInteractorImpl(getSettingsRepository())
    }

    private fun getHistoryRepository(): SearchHistoryRepository {
        val sharedPreferences =
            application.getSharedPreferences(PrefsKeys.HISTORY, Context.MODE_PRIVATE)
        return HistoryRepositoryImpl(sharedPreferences)
    }

    fun provideHistoryInteractor(): HistoryInteractor {
        return HistoryInteractorImpl(getHistoryRepository())
    }
    fun provideSharingInteractor(): SharingInteractor {
        return SharingInteractorImpl(
            externalNavigator = getExternalNavigator(),
            sharingRepository = getSharingRepository()
        )
    }

    private fun getSharingRepository(): SharingRepository {
        return SharingRepositoryImpl(application)
    }

    private fun getExternalNavigator(): ExternalNavigatorRepository {
        return ExternalNavigatorImpl(application)
    }

}
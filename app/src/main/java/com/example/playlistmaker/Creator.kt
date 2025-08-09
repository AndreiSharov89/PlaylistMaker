package com.example.playlistmaker

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import com.example.playlistmaker.data.HistoryRepositoryImpl
import com.example.playlistmaker.data.TrackSearchRepositoryImpl
import com.example.playlistmaker.data.network.RetrofitNetworkClient
import com.example.playlistmaker.data.player.PlayerMediaPlayer
import com.example.playlistmaker.data.settings.ThemeRepositoryImpl
import com.example.playlistmaker.domain.impl.HistoryInteractorImpl
import com.example.playlistmaker.domain.impl.PlayerInteractorImpl
import com.example.playlistmaker.domain.impl.ThemeInteractorImpl
import com.example.playlistmaker.domain.impl.TrackSearchInteractorImpl
import com.example.playlistmaker.domain.interactors.HistoryInteractor
import com.example.playlistmaker.domain.interactors.PlayerInteractor
import com.example.playlistmaker.domain.interactors.ThemeInteractor
import com.example.playlistmaker.domain.interactors.TrackSearchInteractor
import com.example.playlistmaker.domain.repositories.PlayerRepository
import com.example.playlistmaker.domain.repositories.SearchHistoryRepository
import com.example.playlistmaker.domain.repositories.ThemeRepository
import com.example.playlistmaker.domain.repositories.TrackSearchRepository
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

    private fun getThemeRepository(): ThemeRepository {
        val sharedPreferences =
            application.getSharedPreferences(PrefsKeys.THEME, Context.MODE_PRIVATE)
        return ThemeRepositoryImpl(sharedPreferences)
    }

    fun provideThemeInteractor(): ThemeInteractor {
        return ThemeInteractorImpl(getThemeRepository())
    }

    private fun getHistoryRepository(): SearchHistoryRepository {
        val sharedPreferences =
            application.getSharedPreferences(PrefsKeys.HISTORY, Context.MODE_PRIVATE)
        return HistoryRepositoryImpl(sharedPreferences)
    }

    fun provideHistoryInteractor(): HistoryInteractor {
        return HistoryInteractorImpl(getHistoryRepository())
    }

}
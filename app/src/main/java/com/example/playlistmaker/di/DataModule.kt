package com.example.playlistmaker.di

import android.content.Context
import com.example.playlistmaker.settings.data.SettingsRepositoryImpl
import com.example.playlistmaker.settings.domain.SettingsRepository
import com.example.playlistmaker.sharing.data.ExternalNavigatorImpl
import com.example.playlistmaker.sharing.data.SharingRepositoryImpl
import com.example.playlistmaker.sharing.domain.ExternalNavigatorRepository
import com.example.playlistmaker.sharing.domain.SharingRepository
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    single {
        androidContext().getSharedPreferences("playlist_maker_prefs", Context.MODE_PRIVATE)
    }

    single<SettingsRepository> {
        SettingsRepositoryImpl(get())
    }

    single<SharingRepository> {
        SharingRepositoryImpl(androidContext())
    }

    single<ExternalNavigatorRepository> {
        ExternalNavigatorImpl(androidContext())
    }

    single {
        Gson()
    }
}

package com.example.playlistmaker.di

import android.content.Context
import android.media.MediaPlayer
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.playlistmaker.createplaylist.data.CreatePlaylistRepositoryImpl
import com.example.playlistmaker.createplaylist.domain.CreatePlaylistRepository
import com.example.playlistmaker.db.AppDatabase
import com.example.playlistmaker.db.PlaylistDbConverter
import com.example.playlistmaker.db.TrackDbConverter
import com.example.playlistmaker.library.data.FavoritesRepositoryImpl
import com.example.playlistmaker.library.domain.FavoritesRepository
import com.example.playlistmaker.player.data.PlayerMediaPlayer
import com.example.playlistmaker.player.domain.PlayerRepository
import com.example.playlistmaker.search.data.HistoryRepositoryImpl
import com.example.playlistmaker.search.data.NetworkClient
import com.example.playlistmaker.search.data.RetrofitNetworkClient
import com.example.playlistmaker.search.data.TrackSearchRepositoryImpl
import com.example.playlistmaker.search.domain.SearchHistoryRepository
import com.example.playlistmaker.search.domain.TrackSearchRepository
import com.example.playlistmaker.settings.data.SettingsRepositoryImpl
import com.example.playlistmaker.settings.domain.SettingsRepository
import com.example.playlistmaker.sharing.data.ExternalNavigatorImpl
import com.example.playlistmaker.sharing.data.SharingRepositoryImpl
import com.example.playlistmaker.sharing.domain.ExternalNavigatorRepository
import com.example.playlistmaker.sharing.domain.SharingRepository
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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

    factory {
        MediaPlayer()
    }

    factory<PlayerRepository> {
        PlayerMediaPlayer(get())
    }

    single {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single<NetworkClient> {
        RetrofitNetworkClient(get())
    }

    factory<TrackSearchRepository> {
        TrackSearchRepositoryImpl(get(), get())
    }

    factory<SearchHistoryRepository> {
        HistoryRepositoryImpl(get(), get(), get())
    }

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
        }
    }
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
        CREATE TABLE IF NOT EXISTS
        `track_in_playlist` (
           `id` TEXT NOT NULL, 
           `title` TEXT NOT NULL,`artist` TEXT NOT NULL, 
           `duration` TEXT NOT NULL, 
           `album` TEXT, 
           `releaseYear` INTEGER NOT NULL, 
           `genre` TEXT NOT NULL, 
           `country` TEXT NOT NULL, 
           `coverUrl` TEXT NOT NULL, 
           `fileUrl` TEXT NOT NULL, 
            PRIMARY KEY(`id`)
        )
                    """
            )
        }
    }

    single {
        Room.databaseBuilder(androidContext(), AppDatabase::class.java, "playlist_maker.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }

    single {
        get<AppDatabase>().favoritesDao()
    }

    single { TrackDbConverter() }

    single<FavoritesRepository> {
        FavoritesRepositoryImpl(get(), get())
    }

    single {
        get<AppDatabase>().playlistDao()
    }

    single { PlaylistDbConverter(get()) }

    single<CreatePlaylistRepository> {
        CreatePlaylistRepositoryImpl(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    single {
        get<AppDatabase>().trackInPlaylistDao()
    }

}

package com.example.playlistmaker.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavoritesEntity::class,
        PlaylistEntity::class,
        TrackInPlaylistEntity::class],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun trackInPlaylistDao(): TrackIntPlaylistDao
}
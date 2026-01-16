package com.example.playlistmaker.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.playlistmaker.createplaylist.data.PlaylistDao
import com.example.playlistmaker.createplaylist.data.PlaylistEntity
import com.example.playlistmaker.library.data.FavoritesDao
import com.example.playlistmaker.library.data.FavoritesEntity
import com.example.playlistmaker.player.data.TrackInPlaylistEntity
import com.example.playlistmaker.player.data.TrackIntPlaylistDao

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
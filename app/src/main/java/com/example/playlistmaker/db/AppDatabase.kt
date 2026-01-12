package com.example.playlistmaker.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FavoritesEntity::class, PlaylistEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
    abstract fun playlistDao(): PlaylistDao
}
package com.example.playlistmaker.library.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: FavoritesEntity)

    @Delete
    suspend fun deleteTrack(track: FavoritesEntity)

    @Query("SELECT * FROM favorites ORDER BY rowid DESC")
    fun getAllTracks(): Flow<List<FavoritesEntity>>

    @Query("SELECT id FROM favorites")
    suspend fun getAllTracksId(): List<String>
}
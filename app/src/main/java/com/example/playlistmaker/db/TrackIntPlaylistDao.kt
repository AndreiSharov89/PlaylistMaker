package com.example.playlistmaker.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TrackIntPlaylistDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrackToPlaylist(track: TrackInPlaylistEntity)

    @Query("SELECT * FROM track_in_playlist WHERE id = :id")
    suspend fun getTrackById(id: String): TrackInPlaylistEntity?

    @Query("SELECT * FROM track_in_playlist")
    suspend fun getAllTracks(): List<TrackInPlaylistEntity>

    @Query("DELETE FROM track_in_playlist WHERE id = :id")
    suspend fun deleteTrack(id: String)
}
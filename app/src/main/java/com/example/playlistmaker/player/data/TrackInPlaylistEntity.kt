package com.example.playlistmaker.player.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "track_in_playlist")
data class TrackInPlaylistEntity(
    @PrimaryKey
    val id: String,
    val coverUrl: String,
    val title: String,
    val artist: String,
    val album: String?,
    val releaseYear: Int,
    val genre: String,
    val country: String,
    val duration: String,
    val fileUrl: String
)
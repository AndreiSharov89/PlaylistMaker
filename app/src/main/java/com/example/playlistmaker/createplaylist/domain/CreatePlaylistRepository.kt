package com.example.playlistmaker.createplaylist.domain

import android.net.Uri
import com.example.playlistmaker.db.PlaylistEntity
import kotlinx.coroutines.flow.Flow

interface CreatePlaylistRepository {
    suspend fun createPlaylist(playlist: PlaylistEntity)
    suspend fun updatePlaylist(playlist: PlaylistEntity)
    fun getAllPlaylists(): Flow<List<Playlist>>

    // Методы для треков
    /*    suspend fun addTrackToPlaylist(track: PlaylistTrackEntity)
        suspend fun getTrackById(id: String): PlaylistTrackEntity?
        suspend fun getAllTracks(): List<PlaylistTrackEntity>*/

    suspend fun saveImage(uri: Uri, fileName: String): Uri
}
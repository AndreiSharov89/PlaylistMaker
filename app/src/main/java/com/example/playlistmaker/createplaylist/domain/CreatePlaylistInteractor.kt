package com.example.playlistmaker.createplaylist.domain

import android.net.Uri
import com.example.playlistmaker.db.PlaylistEntity
import kotlinx.coroutines.flow.Flow

interface CreatePlaylistInteractor {
    suspend fun createPlaylist(name: String, description: String, coverImagePath: String)
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    /*
        suspend fun addTrackToPlaylist(track: PlaylistTrackEntity)
        suspend fun getTrackById(id: String): PlaylistTrackEntity?
        suspend fun getAllTracks(): List<PlaylistTrackEntity>*/

    suspend fun saveImage(uri: Uri): Uri
}
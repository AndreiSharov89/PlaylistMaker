package com.example.playlistmaker.createplaylist.domain

import android.net.Uri
import com.example.playlistmaker.db.PlaylistEntity
import com.example.playlistmaker.search.domain.Track
import kotlinx.coroutines.flow.Flow

interface CreatePlaylistRepository {
    suspend fun createPlaylist(playlist: PlaylistEntity)
    suspend fun updatePlaylist(playlist: PlaylistEntity)
    fun getAllPlaylists(): Flow<List<Playlist>>

    suspend fun addTrackToPlaylist(track: Track)
    suspend fun getTrackById(id: String): Track?
    suspend fun getAllTracks(): List<Track>

    suspend fun addTrackAndUpdatePlaylist(track: Track, playlist: Playlist)

    suspend fun saveImage(uri: Uri, fileName: String): Uri
}
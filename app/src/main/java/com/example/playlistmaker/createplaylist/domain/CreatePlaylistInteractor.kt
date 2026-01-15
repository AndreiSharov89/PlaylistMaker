package com.example.playlistmaker.createplaylist.domain

import android.net.Uri
import com.example.playlistmaker.search.domain.Track
import kotlinx.coroutines.flow.Flow

interface CreatePlaylistInteractor {
    suspend fun createPlaylist(name: String, description: String?, coverImagePath: String)
    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun updatePlaylist(playlist: Playlist)


    suspend fun addTrackToPlaylist(track: Track)
    suspend fun getTrackById(id: String): Track?
    suspend fun getAllTracks(): List<Track>

    suspend fun addTracksAndUpdatePlaylist(track: Track, playlist: Playlist)

    suspend fun saveImage(uri: Uri): Uri
}
package com.example.playlistmaker.createplaylist.domain

import android.net.Uri
import com.example.playlistmaker.search.domain.Track
import kotlinx.coroutines.flow.Flow

interface CreatePlaylistRepository {
    suspend fun createPlaylist(playlist: Playlist)
    suspend fun updatePlaylist(playlist: Playlist)
    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun getAllPlaylistsNonFlow(): List<Playlist>

    suspend fun addTrackToPlaylist(track: Track)
    suspend fun getTrackById(id: String): Track?
    fun getAllTracks(): Flow<List<Track>>
    suspend fun getPlaylistById(id: Long): Playlist

    suspend fun addTrackAndUpdatePlaylist(track: Track, playlist: Playlist)

    suspend fun saveImage(uri: Uri, fileName: String): Uri

    suspend fun deleteTrack(trackId: String)
}
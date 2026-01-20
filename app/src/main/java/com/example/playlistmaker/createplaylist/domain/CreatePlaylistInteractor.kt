package com.example.playlistmaker.createplaylist.domain

import android.net.Uri
import com.example.playlistmaker.search.domain.Track
import kotlinx.coroutines.flow.Flow

interface CreatePlaylistInteractor {
    suspend fun createPlaylist(name: String, description: String?, coverImagePath: String)
    fun getAllPlaylists(): Flow<List<Playlist>>
    suspend fun updatePlaylist(playlist: Playlist)
    suspend fun deleteTrackFromPlaylist(trackId: String, playlistId: Long)


    suspend fun addTrackToPlaylist(track: Track)
    suspend fun getTrackById(id: String): Track?
    fun getAllTracks(): Flow<List<Track>>
    suspend fun getPlaylistById(id: Long): Playlist

    suspend fun addTracksAndUpdatePlaylist(track: Track, playlist: Playlist)

    suspend fun saveImage(uri: Uri): Uri

    suspend fun deletePlaylist(playlistId: Long)

}
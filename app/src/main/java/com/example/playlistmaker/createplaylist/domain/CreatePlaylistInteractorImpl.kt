package com.example.playlistmaker.createplaylist.domain

import android.net.Uri
import com.example.playlistmaker.db.PlaylistEntity
import com.example.playlistmaker.search.domain.Track
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow

class CreatePlaylistInteractorImpl(
    private val playlistRepository: CreatePlaylistRepository,
) : CreatePlaylistInteractor {

    override suspend fun createPlaylist(name: String, description: String, coverImagePath: String) {
        val playlist = PlaylistEntity(
            name = name,
            description = description,
            coverImagePath = coverImagePath,
            trackIds = Gson().toJson(emptyList<Long>()),
            trackCount = 0
        )
        playlistRepository.createPlaylist(playlist)
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistRepository.getAllPlaylists()
    }

    override suspend fun updatePlaylist(playlist: PlaylistEntity) {
        playlistRepository.updatePlaylist(playlist)
    }

    override suspend fun addTrackToPlaylist(track: Track) {
        playlistRepository.addTrackToPlaylist(track)
    }

    override suspend fun getTrackById(id: String): Track? {
        return playlistRepository.getTrackById(id)
    }

    override suspend fun getAllTracks(): List<Track> {
        return playlistRepository.getAllTracks()
    }

    override suspend fun addTracksAndUpdatePlaylist(
        track: Track,
        playlist: Playlist
    ) {
        playlistRepository.addTrackAndUpdatePlaylist(track, playlist)
    }

    override suspend fun saveImage(uri: Uri): Uri {
        val fileName = "cover_${System.currentTimeMillis()}.jpg"
        return playlistRepository.saveImage(uri, fileName)
    }
}
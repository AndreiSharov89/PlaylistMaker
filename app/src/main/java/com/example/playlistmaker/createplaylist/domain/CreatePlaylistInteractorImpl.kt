package com.example.playlistmaker.createplaylist.domain

import android.net.Uri
import com.example.playlistmaker.search.domain.Track
import kotlinx.coroutines.flow.Flow

class CreatePlaylistInteractorImpl(
    private val playlistRepository: CreatePlaylistRepository,
) : CreatePlaylistInteractor {

    override suspend fun createPlaylist(
        name: String,
        description: String?,
        coverImagePath: String
    ) {
        val playlist = Playlist(
            name = name,
            description = description,
            coverImagePath = coverImagePath
        )
        playlistRepository.createPlaylist(playlist)
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistRepository.getAllPlaylists()
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        playlistRepository.updatePlaylist(playlist)
    }

    override suspend fun addTrackToPlaylist(track: Track) {
        playlistRepository.addTrackToPlaylist(track)
    }
    override suspend fun deleteTrackFromPlaylist(trackId: String, playlistId: Long) {
        val playlist = playlistRepository.getPlaylistById(playlistId)
        val updatedTrackIds = playlist.trackIds.toMutableList()
        if (!updatedTrackIds.remove(trackId)) {
            return
        }
        val updatedPlaylist = playlist.copy(
            trackIds = updatedTrackIds,
            trackCount = updatedTrackIds.size
        )
        playlistRepository.updatePlaylist(updatedPlaylist)
        val allPlaylists = playlistRepository.getAllPlaylistsNonFlow()
        val isOrphaned = allPlaylists.none { p -> p.trackIds.contains(trackId) }
        if (isOrphaned) {
            playlistRepository.deleteTrack(trackId)
        }
    }

    override suspend fun getTrackById(id: String): Track? {
        return playlistRepository.getTrackById(id)
    }

    override fun getAllTracks(): Flow<List<Track>> {
        return playlistRepository.getAllTracks()
    }

    override suspend fun getPlaylistById(id: Long): Playlist {
        return playlistRepository.getPlaylistById(id)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistRepository.deletePlaylist(playlistId)
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

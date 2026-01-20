package com.example.playlistmaker.playlist.ui

import com.example.playlistmaker.createplaylist.domain.Playlist
import com.example.playlistmaker.search.domain.Track

sealed class PlaylistUiState {

    object Loading : PlaylistUiState()

    object Empty : PlaylistUiState()

    data class Content(
        val playlist: Playlist,
        val tracks: List<Track>,
        val totalMinutes: Int
    ) : PlaylistUiState()
}
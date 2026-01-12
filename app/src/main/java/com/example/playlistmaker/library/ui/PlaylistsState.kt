package com.example.playlistmaker.library.ui

import com.example.playlistmaker.createplaylist.domain.Playlist

sealed class PlaylistsState {
    object Empty : PlaylistsState()
    data class Content(val playlists: List<Playlist>) : PlaylistsState()
}
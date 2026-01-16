package com.example.playlistmaker.player.ui

sealed class AddToPlaylistResult {
    data class Added(val playlistName: String) : AddToPlaylistResult()
    data class AlreadyAdded(val playlistName: String) : AddToPlaylistResult()
}
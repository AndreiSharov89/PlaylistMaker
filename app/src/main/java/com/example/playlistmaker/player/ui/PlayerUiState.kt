package com.example.playlistmaker.player.ui

sealed class PlayerUiState {
    object Preparing : PlayerUiState()
    data class Content(
        val isPlaying: Boolean,
        val progressText: String,
        //val isFavorite: Boolean
    ) : PlayerUiState()
}
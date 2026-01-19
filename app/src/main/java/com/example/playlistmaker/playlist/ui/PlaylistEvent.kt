package com.example.playlistmaker.playlist.ui

sealed class PlaylistEvent {
    object EmptyShareError : PlaylistEvent()
    data class Share(val text: String) : PlaylistEvent()
}
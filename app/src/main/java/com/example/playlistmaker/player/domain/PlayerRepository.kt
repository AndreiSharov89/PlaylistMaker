package com.example.playlistmaker.player.domain

interface PlayerRepository {
    fun preparePlayer(url: String, onReady: () -> Unit, onCompletion: () -> Unit)
    fun start()
    fun pause()
    fun getCurrentPosition(): Int
    fun release()
    fun isPlaying(): Boolean
}
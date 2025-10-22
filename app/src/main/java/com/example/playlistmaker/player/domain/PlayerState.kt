package com.example.playlistmaker.player.domain

sealed class PlayerState {
    object Default : PlayerState()
    object Prepared : PlayerState()
    object Playing : PlayerState()
    object Paused : PlayerState()
}
package com.example.playlistmaker.createplaylist.domain

data class Playlist(
    val id: Long,
    val name: String,
    val description: String,
    val coverImagePath: String,
    val trackIds: String,
    val trackCount: Int = 0
)

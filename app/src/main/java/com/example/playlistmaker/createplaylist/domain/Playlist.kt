package com.example.playlistmaker.createplaylist.domain

data class Playlist(
    val id: Long? = null,
    val name: String,
    val description: String?,
    val coverImagePath: String,
    val trackIds: List<String> = emptyList(),
    val trackCount: Int = 0
)

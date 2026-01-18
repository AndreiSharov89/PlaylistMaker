package com.example.playlistmaker.createplaylist.domain

data class Playlist(
    val id: Long? = null,
    var name: String,
    var description: String?,
    var coverImagePath: String,
    val trackIds: List<String> = emptyList(),
    val trackCount: Int = 0
)

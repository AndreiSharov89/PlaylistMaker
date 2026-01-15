package com.example.playlistmaker.db

import com.example.playlistmaker.createplaylist.domain.Playlist
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PlaylistDbConverter(private val gson: Gson = Gson()) {
    fun map(playlist: PlaylistEntity): Playlist {
        val type = object : TypeToken<List<String>>() {}.type
        return Playlist(
            id = playlist.id,
            name = playlist.name,
            description = playlist.description,
            coverImagePath = playlist.coverImagePath,
            trackIds = gson.fromJson(playlist.trackIds, type) ?: emptyList(),
            trackCount = playlist.trackCount
        )
    }

    fun map(playlist: Playlist): PlaylistEntity {
        return PlaylistEntity(
            id = playlist.id ?: 0,
            name = playlist.name,
            description = playlist.description ?: "",
            coverImagePath = playlist.coverImagePath,
            trackIds = gson.toJson(playlist.trackIds),
            trackCount = playlist.trackIds.size
        )
    }
}
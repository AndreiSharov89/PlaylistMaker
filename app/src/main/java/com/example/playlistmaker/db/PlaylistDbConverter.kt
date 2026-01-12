package com.example.playlistmaker.db

import com.example.playlistmaker.createplaylist.domain.Playlist
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PlaylistDbConverter {
    fun map(playlist: Playlist): PlaylistEntity {
        return PlaylistEntity(
            name = playlist.name,
            description = playlist.description,
            coverImagePath = playlist.coverImagePath,
            trackCount = playlist.trackCount,
            trackIds = Gson().toJson(playlist.trackIds)
        )
    }

    fun map(entity: PlaylistEntity): Playlist {
        return Playlist(
            id = entity.id,
            name = entity.name,
            description = entity.description,
            coverImagePath = entity.coverImagePath,
            trackCount = entity.trackCount,
            trackIds = Gson().fromJson(entity.trackIds, object : TypeToken<List<Long>>() {}.type)
        )
    }
}
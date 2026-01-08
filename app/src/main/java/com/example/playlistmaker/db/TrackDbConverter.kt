package com.example.playlistmaker.db

import com.example.playlistmaker.search.domain.Track

class TrackDbConverter {
    fun map(track: Track): FavoritesEntity {
        return FavoritesEntity(
            id = track.trackId.toString(),
            coverUrl = track.artworkUrl100,
            title = track.trackName,
            artist = track.artistName,
            album = track.collectionName,
            releaseYear = track.releaseDate?.take(4)?.toIntOrNull() ?: 0,
            genre = track.primaryGenreName.orEmpty(),
            country = track.country.orEmpty(),
            duration = track.trackTimeMillis.toString(),
            fileUrl = track.previewUrl.orEmpty()
        )
    }

    fun map(entity: FavoritesEntity): Track {
        return Track(
            trackId = entity.id.toLongOrNull() ?: 0L,
            trackName = entity.title,
            artistName = entity.artist,
            trackTimeMillis = entity.duration.toInt(),
            artworkUrl100 = entity.coverUrl,
            collectionName = entity.album,
            releaseDate = entity.releaseYear.toString(),
            primaryGenreName = entity.genre,
            country = entity.country,
            previewUrl = entity.fileUrl,
            isFavorite = true
        )
    }
}
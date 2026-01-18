package com.example.playlistmaker.library.domain

import com.example.playlistmaker.search.domain.Track
import kotlinx.coroutines.flow.Flow

class FavoritesInteractorImpl(
    private val repository: FavoritesRepository
) : FavoritesInteractor {

    override suspend fun addTrack(track: Track) {
        repository.addTrackToFavorites(track)
    }

    override suspend fun removeTrack(track: Track) {
        repository.removeTrackFromFavorites(track)
    }

    override fun getFavoriteTracks(): Flow<List<Track>> {
        return repository.getFavoriteTracks()
    }

    override suspend fun isFavorite(id: String): Boolean {
        return repository.isFavorite(id)
    }
}
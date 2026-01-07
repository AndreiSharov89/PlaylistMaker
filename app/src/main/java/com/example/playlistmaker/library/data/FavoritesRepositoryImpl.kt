package com.example.playlistmaker.library.data

import com.example.playlistmaker.db.FavoritesDao
import com.example.playlistmaker.db.TrackDbConverter
import com.example.playlistmaker.library.domain.FavoritesRepository
import com.example.playlistmaker.search.domain.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoritesRepositoryImpl(
    private val favoritesDao: FavoritesDao,
    private val trackDbConverter: TrackDbConverter
) : FavoritesRepository {
    override suspend fun addTrackToFavorites(track: Track) {
        favoritesDao.insertTrack(trackDbConverter.map(track))
    }

    override suspend fun removeTrackFromFavorites(track: Track) {
        favoritesDao.deleteTrack(trackDbConverter.map(track))
    }

    override fun getFavoriteTracks(): Flow<List<Track>> {
        return favoritesDao.getAllTracks().map { entities ->
            entities.map { trackDbConverter.map(it) }
        }
    }
}
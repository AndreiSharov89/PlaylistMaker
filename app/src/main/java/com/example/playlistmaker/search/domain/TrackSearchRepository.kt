package com.example.playlistmaker.search.domain

import kotlinx.coroutines.flow.Flow

interface TrackSearchRepository {
    suspend fun searchTrack(expression: String): Flow<TrackSearchInteractor.Resource<List<Track>>>
}
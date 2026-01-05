package com.example.playlistmaker.search.domain

import kotlinx.coroutines.flow.Flow

interface TrackSearchInteractor {
    suspend fun searchTrack(expression: String): Flow<Resource<List<Track>>>

    sealed interface Resource<T> {
        class Success<T>(val data: T) : Resource<T>
        class Error<T>(val errorCode: Int) : Resource<T>
    }
}
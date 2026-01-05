package com.example.playlistmaker.search.data

import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.search.domain.TrackSearchInteractor
import com.example.playlistmaker.search.domain.TrackSearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class TrackSearchRepositoryImpl(private val networkClient: NetworkClient) : TrackSearchRepository {
    override suspend fun searchTrack(expression: String): Flow<TrackSearchInteractor.Resource<List<Track>>> =
        flow {
            val response = networkClient.doRequset(TrackSearchRequest(expression))
            when (response.resultCode) {
                200 -> {
                    val trackList = (response as TrackSearchResponse).results.map { trackDto ->
                        Track(
                            trackId = trackDto.trackId,
                            trackName = trackDto.trackName,
                            artistName = trackDto.artistName,
                            trackTimeMillis = trackDto.trackTimeMillis,
                            artworkUrl100 = trackDto.artworkUrl100,
                            collectionName = trackDto.collectionName,
                            releaseDate = trackDto.releaseDate,
                            primaryGenreName = trackDto.primaryGenreName,
                            country = trackDto.country,
                            previewUrl = trackDto.previewUrl
                        )
                    }
                    emit(TrackSearchInteractor.Resource.Success(trackList))
                }

                else -> {
                    emit(TrackSearchInteractor.Resource.Error(response.resultCode))
                }
            }
        }.flowOn(Dispatchers.IO)
}
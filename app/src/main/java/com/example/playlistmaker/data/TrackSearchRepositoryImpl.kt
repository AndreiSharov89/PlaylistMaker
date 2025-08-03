package com.example.playlistmaker.data

import com.example.playlistmaker.data.dto.TrackSearchRequest
import com.example.playlistmaker.data.dto.TrackSearchResponse
import com.example.playlistmaker.domain.interactors.TrackSearchInteractor
import com.example.playlistmaker.domain.repositories.TrackSearchRepository
import com.example.playlistmaker.domain.models.Track

class TrackSearchRepositoryImpl(private val networkClient: NetworkClient) : TrackSearchRepository {
    override fun searchTrack(expression: String): TrackSearchInteractor.Resource<List<Track>> {
        val response = networkClient.doRequset(TrackSearchRequest(expression))
        return if (response is TrackSearchResponse) {
            val trackList = response.results.map { trackDto ->
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
            TrackSearchInteractor.Resource.Success(trackList)
        } else {
            TrackSearchInteractor.Resource.Error(response.resultCode)
        }
    }
}
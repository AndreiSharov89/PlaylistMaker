package com.example.playlistmaker.search.domain

interface TrackSearchRepository {
    fun searchTrack(expression: String): TrackSearchInteractor.Resource<List<Track>>
}
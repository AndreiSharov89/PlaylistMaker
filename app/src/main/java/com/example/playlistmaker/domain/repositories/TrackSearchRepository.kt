package com.example.playlistmaker.domain.repositories

import com.example.playlistmaker.domain.interactors.TrackSearchInteractor
import com.example.playlistmaker.domain.models.Track

interface TrackSearchRepository {
    fun searchTrack(expression: String): TrackSearchInteractor.Resource<List<Track>>
}
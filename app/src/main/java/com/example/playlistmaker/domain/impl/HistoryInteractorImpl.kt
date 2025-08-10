package com.example.playlistmaker.domain.impl

import com.example.playlistmaker.domain.interactors.HistoryInteractor
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repositories.SearchHistoryRepository

class HistoryInteractorImpl(
    private val repository: SearchHistoryRepository
) : HistoryInteractor {
    override fun getHistory(): List<Track> = repository.getHistory()

    override fun saveTrack(track: Track) = repository.addTrack(track)

    override fun clearHistory() = repository.clear()
}
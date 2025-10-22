package com.example.playlistmaker.search.domain

class HistoryInteractorImpl(
    private val repository: SearchHistoryRepository
) : HistoryInteractor {
    override fun getHistory(): List<Track> = repository.getHistory()

    override fun saveTrack(track: Track) = repository.addTrack(track)

    override fun clearHistory() = repository.clear()
}
package com.example.playlistmaker.search.domain

class HistoryInteractorImpl(
    private val repository: SearchHistoryRepository
) : HistoryInteractor {
    override suspend fun getHistory(): List<Track> = repository.getHistory()

    override suspend fun saveTrack(track: Track) = repository.addTrack(track)

    override fun clearHistory() = repository.clear()
}
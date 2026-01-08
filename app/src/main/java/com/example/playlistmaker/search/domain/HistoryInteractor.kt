package com.example.playlistmaker.search.domain

interface HistoryInteractor {
    suspend fun getHistory(): List<Track>
    suspend fun saveTrack(track: Track)
    fun clearHistory()
}
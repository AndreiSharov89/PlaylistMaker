package com.example.playlistmaker.search.domain

interface SearchHistoryRepository {
    suspend fun getHistory(): List<Track>
    suspend fun addTrack(track: Track)
    fun clear()
}
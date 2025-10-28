package com.example.playlistmaker.search.domain

interface SearchHistoryRepository {
    fun getHistory(): List<Track>
    fun addTrack(track: Track)
    fun clear()
}
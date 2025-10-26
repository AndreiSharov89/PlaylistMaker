package com.example.playlistmaker.search.ui

import com.example.playlistmaker.search.domain.Track

sealed class SearchState {
    object Loading : SearchState()
    data class Content(val tracks: List<Track>) : SearchState()
    object Error : SearchState()
    object Empty : SearchState()
    data class History(val tracks: List<Track>) : SearchState()
    object EmptyHistory : SearchState()
}

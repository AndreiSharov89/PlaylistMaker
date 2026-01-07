package com.example.playlistmaker.library.ui

import com.example.playlistmaker.search.domain.Track

sealed interface FavoritesState {
    object Empty : FavoritesState
    data class Content(val tracks: List<Track>) : FavoritesState
}
package com.example.playlistmaker.search.domain

import kotlinx.coroutines.flow.Flow

class TrackSearchInteractorImpl(private val repository: TrackSearchRepository) :
    TrackSearchInteractor {

    override suspend fun searchTrack(expression: String): Flow<TrackSearchInteractor.Resource<List<Track>>> {
        return repository.searchTrack(sanitizeText(expression))
    }

    private fun sanitizeText(text: String): String {
        return text.replace(Regex("[^\\p{L}\\p{N}.&\\-'/ ]+"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
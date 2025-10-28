package com.example.playlistmaker.search.data

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.search.domain.SearchHistoryRepository
import com.google.gson.Gson

class HistoryRepositoryImpl(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson = Gson()
) : SearchHistoryRepository {

    companion object {
        private const val HISTORY_KEY = "search_history"
        private const val MAX_HISTORY_SIZE = 10
    }

    override fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(HISTORY_KEY, null) ?: return emptyList()
        return try {
            gson.fromJson(json, Array<Track>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun addTrack(track: Track) {
        val tracks = getHistory().filterNot { it.trackId == track.trackId }.toMutableList()
        tracks.add(0, track)
        saveHistory(tracks.take(MAX_HISTORY_SIZE))
    }

    override fun clear() {
        saveHistory(emptyList())
    }

    private fun saveHistory(tracks: List<Track>) {
        sharedPreferences.edit {
            putString(HISTORY_KEY, gson.toJson(tracks))
        }
    }
}
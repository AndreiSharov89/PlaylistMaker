package com.example.playlistmaker

import android.content.SharedPreferences
import com.google.gson.Gson
import androidx.core.content.edit

class SearchHistory(private val sharedPreferences: SharedPreferences) {

    private val gson = Gson()

    companion object {
        private const val HISTORY_KEY = "search_history"
        private const val MAX_HISTORY_SIZE = 10
    }

    fun getHistory(): List<Track> {
        val history = sharedPreferences.getString(HISTORY_KEY, null) ?: return emptyList()
        return try {
            gson.fromJson(history, Array<Track>::class.java).toMutableList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addTrack(track: Track) {
        val tracks = getHistory()
        val newTracks = tracks
            .filterNot { it.trackId == track.trackId }
            .toMutableList()
        newTracks.add(0, track)
        setHistory(newTracks.take(MAX_HISTORY_SIZE))
    }

    fun clear() {
        setHistory(emptyList())
    }

    private fun setHistory(tracks: List<Track>) {
        val history = gson.toJson(tracks)
        sharedPreferences.edit {
            putString(HISTORY_KEY, history)
        }
    }
}
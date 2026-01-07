package com.example.playlistmaker.search.data

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.playlistmaker.PrefsKeys
import com.example.playlistmaker.db.AppDatabase
import com.example.playlistmaker.search.domain.SearchHistoryRepository
import com.example.playlistmaker.search.domain.Track
import com.google.gson.Gson

class HistoryRepositoryImpl(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson,
    private val appDatabase: AppDatabase
) : SearchHistoryRepository {

    companion object {
        private const val MAX_HISTORY_SIZE = 10
    }

    override suspend fun getHistory(): List<Track> {
        val json = sharedPreferences.getString(PrefsKeys.HISTORY, null) ?: return emptyList()
        val tracks = try {
            gson.fromJson(json, Array<Track>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }

        val favoritesIds = appDatabase.favoritesDao().getAllTracksId()
        tracks.forEach { track ->
            track.isFavorite = favoritesIds.contains(track.trackId.toString())
        }

        return tracks
    }

    override suspend fun addTrack(track: Track) {
        val tracks = getHistory().filterNot { it.trackId == track.trackId }.toMutableList()
        tracks.add(0, track)
        saveHistory(tracks.take(MAX_HISTORY_SIZE))
    }

    override fun clear() {
        saveHistory(emptyList())
    }

    private fun saveHistory(tracks: List<Track>) {
        sharedPreferences.edit {
            putString(PrefsKeys.HISTORY, gson.toJson(tracks))
        }
    }
}

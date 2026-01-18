package com.example.playlistmaker.playlist.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.createplaylist.domain.CreatePlaylistInteractor
import com.example.playlistmaker.createplaylist.domain.Playlist
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.utils.formatMinuteCount
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val playlistId: Long,
    private val createPlaylistInteractor: CreatePlaylistInteractor
) : ViewModel() {

    private val _playlistData = MutableLiveData<Pair<Playlist, String>>()
    val playlistData: LiveData<Pair<Playlist, String>> get() = _playlistData

    private val _tracks = MutableLiveData<List<Track>>()
    val tracks: LiveData<List<Track>> get() = _tracks

    fun loadPlaylist() {
        viewModelScope.launch {
            val playlist = createPlaylistInteractor.getPlaylistById(playlistId) ?: return@launch
            val tracks = playlist.trackIds.mapNotNull { id ->
                createPlaylistInteractor.getTrackById(id)
            }
            val timeString = getFormattedDuration(playlist.trackIds)
            _playlistData.value = playlist to timeString
            _tracks.postValue(tracks)
        }
    }

    fun removeTrack(trackId: String) {
        viewModelScope.launch {
            createPlaylistInteractor.deleteTrackFromPlaylist(trackId, playlistId)
            loadPlaylist()
        }
    }

    private suspend fun getFormattedDuration(trackIds: List<String>): String {
        var durationSumMillis = 0L
        for (id in trackIds) {
            createPlaylistInteractor.getTrackById(id)?.let { track ->
                durationSumMillis += track.trackTimeMillis
            }
        }
        val durationMinutes: Int = (durationSumMillis / 60000).toInt()
        return formatMinuteCount(durationMinutes)
    }
}

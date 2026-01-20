package com.example.playlistmaker.playlist.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.R
import com.example.playlistmaker.createplaylist.domain.CreatePlaylistInteractor
import com.example.playlistmaker.createplaylist.domain.Playlist
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.utils.SingleLiveEvent
import com.example.playlistmaker.utils.formatMinuteCount
import com.example.playlistmaker.utils.formatTrackCount
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PlaylistViewModel(
    private val playlistId: Long,
    private val createPlaylistInteractor: CreatePlaylistInteractor
) : ViewModel() {

    private val _playlistData = MutableLiveData<Pair<Playlist, String>>()
    val playlistData: LiveData<Pair<Playlist, String>> get() = _playlistData

    private val _tracks = MutableLiveData<List<Track>>()
    val tracks: LiveData<List<Track>> get() = _tracks

    private val _sharePlaylistEvent = SingleLiveEvent<String>()
    val sharePlaylistEvent: LiveData<String> get() = _sharePlaylistEvent

    private val _toastEvent = SingleLiveEvent<Int>()
    val toastEvent: LiveData<Int> get() = _toastEvent

    private val _playlistDeleted = SingleLiveEvent<Boolean>()
    val playlistDeleted: LiveData<Boolean> get() = _playlistDeleted

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
    fun removePlaylist() {
        viewModelScope.launch {
            createPlaylistInteractor.deletePlaylist(playlistId)
            _playlistDeleted.postValue(true)
        }
    }

    fun onShareButtonClicked() {
        val playlist = _playlistData.value?.first ?: return
        val tracks = _tracks.value ?: return
        if (tracks.isEmpty()) {
            _toastEvent.postValue(R.string.no_tracks_to_share)
            return
        }
        val shareText = buildShareText(playlist, tracks)
        _sharePlaylistEvent.postValue(shareText)
    }

    private fun buildShareText(playlist: Playlist, tracks: List<Track>): String {
        val builder = StringBuilder()
        builder.append(playlist.name).appendLine()
        builder.append(playlist.description).appendLine()
        builder.append(formatTrackCount(playlist.trackCount)).appendLine()

        tracks.forEachIndexed { index, track ->
            val trackDuration =
                SimpleDateFormat("mm:ss", Locale.getDefault()).format(track.trackTimeMillis)
            builder.append("${index + 1}. ${track.artistName} - ${track.trackName} ($trackDuration)")
                .appendLine()
        }
        return builder.toString()
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
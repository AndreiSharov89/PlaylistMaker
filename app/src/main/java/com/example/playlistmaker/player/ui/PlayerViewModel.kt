package com.example.playlistmaker.player.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.createplaylist.domain.CreatePlaylistInteractor
import com.example.playlistmaker.createplaylist.domain.Playlist
import com.example.playlistmaker.library.domain.FavoritesInteractor
import com.example.playlistmaker.player.domain.PlayerInteractor
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.utils.SingleLiveEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val track: Track,
    private val player: PlayerInteractor,
    private val favoritesInteractor: FavoritesInteractor,
    private val playlistsInteractor: CreatePlaylistInteractor
) : ViewModel() {

    val highResCoverUrl: String =
        track.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")

    private val uiStateLiveData =
        MutableLiveData<PlayerUiState>(PlayerUiState.Preparing)
    val observeUiStateLiveData: LiveData<PlayerUiState> = uiStateLiveData

    private val addToPlaylistResult = MutableLiveData<AddToPlaylistResult>()
    fun observeAddToPlaylistResult(): LiveData<AddToPlaylistResult> =
        addToPlaylistResult

    private val playlistsLiveData = MutableLiveData<List<Playlist>>()
    fun observePlaylists(): LiveData<List<Playlist>> = playlistsLiveData

    private val snackbarMessageEvent = SingleLiveEvent<String>()
    fun observeSnackbarMessage(): LiveData<String> = snackbarMessageEvent

    private var timerJob: Job? = null

    fun preparePlayer() {
        if (uiStateLiveData.value is PlayerUiState.Content) return

        viewModelScope.launch {
            // 🔹 Get initial favorite state BEFORE first Content emission
            val isFavorite =
                favoritesInteractor.isFavorite(track.trackId.toString())
            track.isFavorite = isFavorite

            player.preparePlayer(
                url = track.previewUrl.orEmpty(),
                onReady = {
                    uiStateLiveData.postValue(
                        PlayerUiState.Content(
                            isPlaying = false,
                            progressText = "00:00",
                            isFavorite = isFavorite
                        )
                    )
                },
                onCompletion = {
                    stopTimer()
                    uiStateLiveData.postValue(
                        PlayerUiState.Content(
                            isPlaying = false,
                            progressText = "00:00",
                            isFavorite = isFavorite
                        )
                    )
                }
            )
        }
    }

    fun onPlayButtonClicked() {
        val state = uiStateLiveData.value as? PlayerUiState.Content ?: return
        if (state.isPlaying) pausePlayer() else startPlayer()
    }

    fun onPause() {
        pausePlayer()
    }

    private fun startPlayer() {
        player.start()
        val state = uiStateLiveData.value as? PlayerUiState.Content ?: return
        uiStateLiveData.value = state.copy(
            isPlaying = true,
            progressText = state.progressText.ifEmpty { "00:00" }
        )
        startTimer()
    }

    private fun pausePlayer() {
        val state = uiStateLiveData.value as? PlayerUiState.Content ?: return
        if (state.isPlaying) {
            player.pause()
            stopTimer()
            uiStateLiveData.value = state.copy(isPlaying = false)
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(DELAY)
                val state = uiStateLiveData.value as? PlayerUiState.Content
                if (state?.isPlaying == true) {
                    uiStateLiveData.value =
                        state.copy(progressText = formatTime(player.getCurrentPosition()))
                } else break
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }

    fun onFavoriteClicked() {
        val state = uiStateLiveData.value as? PlayerUiState.Content ?: return
        val newFavorite = !state.isFavorite

        uiStateLiveData.value = state.copy(isFavorite = newFavorite)
        track.isFavorite = newFavorite

        viewModelScope.launch {
            if (newFavorite) {
                favoritesInteractor.addTrack(track)
            } else {
                favoritesInteractor.removeTrack(track)
            }
        }
    }

    fun addTrackToPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            val isAlreadyAdded =
                playlist.trackIds.contains(track.trackId.toString())

            addToPlaylistResult.postValue(
                if (isAlreadyAdded) {
                    AddToPlaylistResult.AlreadyAdded(playlist.name)
                } else {
                    playlistsInteractor.addTracksAndUpdatePlaylist(track, playlist)
                    AddToPlaylistResult.Added(playlist.name)
                }
            )
        }
    }

    fun onPlaylistCreated(playlistName: String) {
        snackbarMessageEvent.postValue("Плейлист \"$playlistName\" создан")
    }

    fun loadPlaylists() {
        if (playlistsLiveData.value != null) return
        viewModelScope.launch {
            playlistsInteractor.getAllPlaylists().collect {
                playlistsLiveData.postValue(it)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
        timerJob?.cancel()
    }

    private fun formatTime(ms: Int): String {
        val minutes = (ms / 1000) / 60
        val seconds = (ms / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    companion object {
        private const val DELAY = 200L
    }
}


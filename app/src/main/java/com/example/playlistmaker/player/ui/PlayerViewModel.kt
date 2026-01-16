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
    val highResCoverUrl: String = track.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")
    private val uiStateLiveData = MutableLiveData<PlayerUiState>(PlayerUiState.Preparing)
    val observeUiStateLiveData: LiveData<PlayerUiState> = uiStateLiveData
    private val addToPlaylistResult = MutableLiveData<AddToPlaylistResult>()
    fun observeAddToPlaylistResult(): LiveData<AddToPlaylistResult> = addToPlaylistResult
    private val playlistsLiveData = MutableLiveData<List<Playlist>>()
    fun observePlaylists(): LiveData<List<Playlist>> = playlistsLiveData
    private var timerJob: Job? = null

    private val snackbarMessageEvent = SingleLiveEvent<String>()
    fun observeSnackbarMessage(): LiveData<String> = snackbarMessageEvent


    fun preparePlayer() {
        if (uiStateLiveData.value is PlayerUiState.Content) {
            return
        }
        val currentIsFavorite =
            (uiStateLiveData.value as? PlayerUiState.Content)?.isFavorite ?: track.isFavorite
        player.preparePlayer(
            url = track.previewUrl ?: "",
            onReady = {
                uiStateLiveData.postValue(
                    PlayerUiState.Content(
                        isPlaying = false,
                        progressText = "00:00",
                        isFavorite = currentIsFavorite
                    )
                )
            },
            onCompletion = {
                stopTimer()
                uiStateLiveData.postValue(
                    PlayerUiState.Content(
                        isPlaying = false,
                        progressText = "00:00",
                        isFavorite = currentIsFavorite
                    )
                )
            }
        )
    }

    fun onPlayButtonClicked() {
        when (val state = uiStateLiveData.value) {
            is PlayerUiState.Content -> {
                if (state.isPlaying) {
                    pausePlayer()
                } else {
                    startPlayer()
                }
            }

            else -> {}
        }
    }

    fun onPause() {
        pausePlayer()
    }

    private fun startPlayer() {
        player.start()
        val currentState = uiStateLiveData.value as? PlayerUiState.Content
        uiStateLiveData.value = currentState?.copy(
            isPlaying = true,
            progressText = currentState.progressText.ifEmpty { "00:00" }
        )
        startTimer()
    }

    private fun pausePlayer() {
        (observeUiStateLiveData.value as? PlayerUiState.Content)?.let {
            if (it.isPlaying) {
                player.pause()
                stopTimer()
                uiStateLiveData.value = it.copy(isPlaying = false)
            }
        }
    }

    private fun formatTime(ms: Int): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
        timerJob?.cancel()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(DELAY)
                val currentState = uiStateLiveData.value
                if (currentState is PlayerUiState.Content && currentState.isPlaying) {
                    val currentPos = player.getCurrentPosition()
                    uiStateLiveData.value = currentState.copy(
                        progressText = formatTime(currentPos)
                    )
                } else {
                    break
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }

    fun onFavoriteClicked() {
        val currentState = uiStateLiveData.value as? PlayerUiState.Content ?: return
        val newFavoriteStatus = !currentState.isFavorite
        uiStateLiveData.value = currentState.copy(isFavorite = newFavoriteStatus)
        track.isFavorite = newFavoriteStatus

        viewModelScope.launch {
            if (newFavoriteStatus) {
                favoritesInteractor.addTrack(track)
            } else {
                favoritesInteractor.removeTrack(track)
            }
        }
    }

    fun addTrackToPlaylist(playlist: Playlist) {
        viewModelScope.launch {
            val isAlreadyAdded = playlist.trackIds.contains(track.trackId.toString())

            if (isAlreadyAdded) {
                addToPlaylistResult.postValue(
                    AddToPlaylistResult.AlreadyAdded(playlist.name)
                )
            } else {
                playlistsInteractor.addTracksAndUpdatePlaylist(track, playlist)
                addToPlaylistResult.postValue(
                    AddToPlaylistResult.Added(playlist.name)
                )
            }
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

    companion object {
        private const val DELAY = 200L
    }
}

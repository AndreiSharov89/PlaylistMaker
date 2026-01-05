package com.example.playlistmaker.player.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.player.domain.PlayerInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val previewUrl: String,
    coverUrl: String,
    private val player: PlayerInteractor
) : ViewModel() {

    val highResCoverUrl: String = coverUrl.replaceAfterLast('/', "512x512bb.jpg")

    private val uiStateLiveData = MutableLiveData<PlayerUiState>()
    val observeUiStateLiveData: LiveData<PlayerUiState> = uiStateLiveData

    private var timerJob: Job? = null

    init {
        uiStateLiveData.value = PlayerUiState.Preparing
        preparePlayer()
    }

    private fun preparePlayer() {
        player.preparePlayer(
            url = previewUrl,
            onReady = {
                uiStateLiveData.postValue(
                    PlayerUiState.Content(
                        isPlaying = false,
                        progressText = "00:00"
                    )
                )
            },
            onCompletion = {
                stopTimer()
                uiStateLiveData.postValue(
                    PlayerUiState.Content(
                        isPlaying = false,
                        progressText = "00:00"
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
        val initialTime = if (currentState?.progressText == "00:00") {
            "00:00"
        } else {
            formatTime(player.getCurrentPosition())
        }
        uiStateLiveData.value = PlayerUiState.Content(
            isPlaying = true,
            progressText = initialTime
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

    companion object {
        private const val DELAY = 200L
    }
}

package com.example.playlistmaker.player.ui

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.player.domain.PlayerInteractor

class PlayerViewModel(
    private val previewUrl: String,
    coverUrl: String,
    private val player: PlayerInteractor
) : ViewModel() {

    val highResCoverUrl: String = coverUrl.replaceAfterLast('/', "512x512bb.jpg")

    private val uiStateLiveData = MutableLiveData<PlayerUiState>()
    val observeUiStateLiveData: LiveData<PlayerUiState> = uiStateLiveData

    private val handler = Handler(Looper.getMainLooper())

    private val timerRunnable = object : Runnable {
        override fun run() {
            val currentState = observeUiStateLiveData.value
            if (currentState is PlayerUiState.Content && currentState.isPlaying) {
                uiStateLiveData.postValue(
                    currentState.copy(
                        progressText = formatTime(player.getCurrentPosition())
                    )
                )
                handler.postDelayed(this, DELAY)
            }
        }
    }

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
                handler.removeCallbacks(timerRunnable)
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
        uiStateLiveData.value = PlayerUiState.Content(
            isPlaying = true,
            progressText = formatTime(player.getCurrentPosition())
        )
        handler.post(timerRunnable)
    }

    private fun pausePlayer() {
        (observeUiStateLiveData.value as? PlayerUiState.Content)?.let {
            if (it.isPlaying) {
                player.pause()
                handler.removeCallbacks(timerRunnable)
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
        handler.removeCallbacks(timerRunnable)
    }

    companion object {
        private const val DELAY = 200L
    }
}

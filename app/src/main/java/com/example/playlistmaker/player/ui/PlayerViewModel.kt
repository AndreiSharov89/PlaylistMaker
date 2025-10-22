package com.example.playlistmaker.player.ui

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.player.domain.PlayerInteractor
import com.example.playlistmaker.player.domain.PlayerState

class PlayerViewModel(
    private val previewUrl: String,
    private val player: PlayerInteractor
) : ViewModel() {

    private val playerState = MutableLiveData<PlayerState>(PlayerState.Default)
    val playerStateObserver: LiveData<PlayerState> = playerState

    private val timerText = MutableLiveData("00:00")
    val timerTextObserver: LiveData<String> = timerText

    private val handler = Handler(Looper.getMainLooper())

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (playerState.value == PlayerState.Playing) {
                timerText.postValue(formatTime(player.getCurrentPosition()))
                handler.postDelayed(this, DELAY)
            }
        }
    }

    init {
        preparePlayer()
    }

    private fun preparePlayer() {
        player.preparePlayer(
            url = previewUrl,
            onReady = {
                playerState.postValue(PlayerState.Prepared)
                timerText.postValue("00:00")
            },
            onCompletion = {
                playerState.postValue(PlayerState.Prepared)
                handler.removeCallbacks(timerRunnable)
                timerText.postValue("00:00")
            }
        )
    }

    fun onPlayButtonClicked() {
        when (playerState.value) {
            PlayerState.Playing -> pausePlayer()
            PlayerState.Prepared, PlayerState.Paused -> startPlayer()
            else -> {}
        }
    }
    
    fun onPause() {
        pausePlayer()
    }

    private fun startPlayer() {
        player.start()
        playerState.value = PlayerState.Playing
        handler.post(timerRunnable)
    }

    private fun pausePlayer() {
        if (playerState.value == PlayerState.Playing) {
            player.pause()
            playerState.value = PlayerState.Paused
            handler.removeCallbacks(timerRunnable)
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

        fun getFactory(previewUrl: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PlayerViewModel(
                    previewUrl = previewUrl,
                    player = Creator.providePlayerInteractor()
                )
            }
        }
    }
}

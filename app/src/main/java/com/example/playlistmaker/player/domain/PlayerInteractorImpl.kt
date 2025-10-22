package com.example.playlistmaker.player.domain

class PlayerInteractorImpl(
    private val repository: PlayerRepository
) : PlayerInteractor {

    override fun preparePlayer(url: String, onReady: () -> Unit, onCompletion: () -> Unit) {
        repository.preparePlayer(url, onReady, onCompletion)
    }

    override fun start() {
        repository.start()
    }

    override fun pause() {
        repository.pause()
    }

    override fun getCurrentPosition(): Int {
        return repository.getCurrentPosition()
    }

    override fun release() {
        repository.release()
    }

    override fun isPlaying(): Boolean {
        return repository.isPlaying()
    }
}

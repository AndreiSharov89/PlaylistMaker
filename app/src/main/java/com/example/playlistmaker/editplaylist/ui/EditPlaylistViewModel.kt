package com.example.playlistmaker.editplaylist.ui

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.createplaylist.domain.CreatePlaylistInteractor
import com.example.playlistmaker.createplaylist.domain.Playlist
import com.example.playlistmaker.createplaylist.ui.CreatePlaylistViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditPlaylistViewModel(
    private val playlistInteractor: CreatePlaylistInteractor,
    private val playlistId: Long
) : CreatePlaylistViewModel(playlistInteractor) {

    init {
        viewModelScope.launch {
            val loadedPlaylist = playlistInteractor.getPlaylistById(playlistId)
            _playlist.postValue(loadedPlaylist)
        }
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _playlistCreated.postValue(null)
        //no idea in logic, snackbar we broke db?
    }
    private val _playlist = MutableLiveData<Playlist>()
    val playlist: LiveData<Playlist> = _playlist

    override var _selectedCoverUri = MutableLiveData<Uri?>()
    override val selectedCoverUri: LiveData<Uri?> = _selectedCoverUri

    fun loadPlaylist() {
        viewModelScope.launch(exceptionHandler) {
            val loadedPlaylist = playlistInteractor.getPlaylistById(playlistId)
            _playlist.postValue(loadedPlaylist)
        }
    }
    fun updatePlaylist(name: String, description: String) {
        val current = _playlist.value ?: return
        if (name.isBlank()) return

        viewModelScope.launch {
            val updated = current.copy(
                name = name,
                description = description,
                coverImagePath = savedCoverPath ?: current.coverImagePath
            )
            playlistInteractor.updatePlaylist(updated)
            _playlistCreated.postValue(name)
        }
    }

    fun preloadCover(uriString: String?) {
        if (uriString.isNullOrEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val uri = Uri.parse(uriString)
            _selectedCoverUri.postValue(uri)
        }
    }
}





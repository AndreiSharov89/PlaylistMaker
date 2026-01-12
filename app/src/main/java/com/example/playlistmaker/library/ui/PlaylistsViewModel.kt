package com.example.playlistmaker.library.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.createplaylist.domain.CreatePlaylistInteractor
import kotlinx.coroutines.launch

class PlaylistsViewModel(
    private val playlistsInteractor: CreatePlaylistInteractor
) : ViewModel() {

    private val _state = MutableLiveData<PlaylistsState>()
    val state: LiveData<PlaylistsState> = _state

    fun loadPlaylists() {
        viewModelScope.launch {
            playlistsInteractor.getAllPlaylists().collect {
                if (it.isEmpty()) {
                    _state.postValue(PlaylistsState.Empty)
                } else {
                    _state.postValue(PlaylistsState.Content(it))
                }
            }
        }
    }
}



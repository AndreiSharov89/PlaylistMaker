package com.example.playlistmaker.createplaylist.ui

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.createplaylist.domain.CreatePlaylistInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

open class CreatePlaylistViewModel(
    private val createPlaylistInteractor: CreatePlaylistInteractor
) : ViewModel() {

    protected open var _selectedCoverUri = MutableLiveData<Uri?>()
    open val selectedCoverUri: LiveData<Uri?> get() = _selectedCoverUri

    protected val _playlistCreated = MutableLiveData<String?>()
    val playlistCreated: LiveData<String?> get() = _playlistCreated
    var savedCoverPath: String? = null
    private var saveImageJob: Job? = null

    open fun onImageSelected(uri: Uri) {
        _selectedCoverUri.value = uri
        saveImageJob?.cancel()
        saveImageJob = viewModelScope.launch {
            savedCoverPath = createPlaylistInteractor.saveImage(uri).toString()
        }
    }

    open fun createPlaylist(name: String, description: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            try {
                saveImageJob?.join()
                createPlaylistInteractor.createPlaylist(name, description, savedCoverPath ?: "")
                _playlistCreated.postValue(name)
            } catch (e: Exception) {
                _playlistCreated.postValue(null)
            }
        }
    }

    fun hasUnsavedData(name: String, description: String): Boolean {
        return name.isNotBlank() || description.isNotBlank() || _selectedCoverUri.value != null
    }

}
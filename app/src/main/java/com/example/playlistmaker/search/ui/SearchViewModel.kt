package com.example.playlistmaker.search.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.search.domain.HistoryInteractor
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.search.domain.TrackSearchInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchInteractor: TrackSearchInteractor,
    private val historyInteractor: HistoryInteractor
) : ViewModel() {

    private val searchStateLiveData = MutableLiveData<SearchState>()
    val observeSearchStateLiveData: LiveData<SearchState> = searchStateLiveData


    private var latestSearchText: String? = null
    private var debounceJob: Job? = null
    private var searchJob: Job? = null

    fun showHistory() {
        viewModelScope.launch {
            val tracks = historyInteractor.getHistory()
            if (tracks.isNotEmpty()) {
                searchStateLiveData.postValue(SearchState.History(tracks))
            } else {
                searchStateLiveData.postValue(SearchState.EmptyHistory)
            }
        }
    }

    fun searchDebounce(changedText: String) {
        if (latestSearchText == changedText) return

        this.latestSearchText = changedText
        debounceJob?.cancel()
        if (changedText.isEmpty()) {
            searchJob?.cancel()
            showHistory()
        } else {
            debounceJob = viewModelScope.launch {
                delay(SEARCH_DEBOUNCE_DELAY)
                search(changedText)
            }
        }
    }

    fun search(query: String) {
        if (query.isEmpty()) return
        latestSearchText = query

        debounceJob?.cancel()
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            searchStateLiveData.value = SearchState.Loading

            searchInteractor.searchTrack(query)
                .collect { resource ->
                    when (resource) {
                        is TrackSearchInteractor.Resource.Success -> {
                            if (resource.data.isEmpty())
                                searchStateLiveData.postValue(SearchState.Empty)
                            else
                                searchStateLiveData.postValue(SearchState.Content(resource.data))
                        }

                        is TrackSearchInteractor.Resource.Error -> {
                            searchStateLiveData.postValue(SearchState.Error)
                        }
                    }
                }
        }
    }
    fun saveTrack(track: Track) {
        viewModelScope.launch {
            historyInteractor.saveTrack(track)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyInteractor.clearHistory()
            showHistory()
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}

package com.example.playlistmaker.search.ui

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.search.domain.HistoryInteractor
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.search.domain.TrackSearchInteractor

class SearchViewModel(
    private val searchInteractor: TrackSearchInteractor,
    private val historyInteractor: HistoryInteractor
) : ViewModel() {

    private val searchStateLiveData = MutableLiveData<SearchState>()
    val observeSearchStateLiveData: LiveData<SearchState> = searchStateLiveData

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null

    fun showHistory() {
        val tracks = historyInteractor.getHistory()
        if (tracks.isNotEmpty()) {
            searchStateLiveData.postValue(SearchState.History(tracks))
        } else {
            searchStateLiveData.postValue(SearchState.EmptyHistory)
        }
    }

    fun searchDebounce(query: String) {
        searchRunnable?.let { handler.removeCallbacks(it) }

        if (query.isEmpty()) {
            showHistory()
        } else {
            searchRunnable = Runnable { search(query) }
            handler.postDelayed(searchRunnable!!, SEARCH_DEBOUNCE_DELAY)
        }
    }

    fun search(query: String) {
        searchStateLiveData.value = SearchState.Loading
        searchInteractor.searchTrack(query, object : TrackSearchInteractor.Consumer<List<Track>> {
            override fun consume(data: TrackSearchInteractor.Consumer.ConsumerData<List<Track>>) {
                when (data) {
                    is TrackSearchInteractor.Consumer.ConsumerData.Data -> {
                        if (data.value.isEmpty())
                            searchStateLiveData.postValue(SearchState.Empty)
                        else
                            searchStateLiveData.postValue(SearchState.Content(data.value))
                    }

                    is TrackSearchInteractor.Consumer.ConsumerData.Error -> {
                        searchStateLiveData.postValue(SearchState.Error)
                    }
                }
            }
        })
    }

    fun saveTrack(track: Track) {
        historyInteractor.saveTrack(track)
    }

    fun clearHistory() {
        historyInteractor.clearHistory()
        showHistory()
    }

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacksAndMessages(null)
    }


    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}

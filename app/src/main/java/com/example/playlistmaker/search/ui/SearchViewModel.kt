package com.example.playlistmaker.search.ui

import android.os.Looper
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.creator.Creator
import java.util.logging.Handler

class SearchViewModel : ViewModel() {
    private val searchInteractor = Creator.provideTrackSearchInteractor()
    private val historyInteractor: HistoryInteractor = Creator.provideHistoryInteractor()

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable = Runnable { search() }

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}
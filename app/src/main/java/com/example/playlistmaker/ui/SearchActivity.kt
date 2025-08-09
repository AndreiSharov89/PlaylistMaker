package com.example.playlistmaker.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.Creator
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.interactors.TrackSearchInteractor
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.TrackAdapter

class SearchActivity : AppCompatActivity() {
    private var history = Creator.provideHistoryInteractor()

    private lateinit var trackRecycler: RecyclerView
    private lateinit var historyRecycler: RecyclerView

    private lateinit var inputEditText: EditText
    private lateinit var clearButton: ImageView
    private lateinit var imageError: ImageView
    private lateinit var placeholderMessage: TextView
    private lateinit var refresh: Button
    private lateinit var btnBack: ImageView
    private lateinit var historySection: LinearLayout
    private lateinit var clearHistoryButton: Button
    private lateinit var progressBar: ProgressBar

    private val tracks = ArrayList<Track>()
    private lateinit var adapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter
    private lateinit var searchInteractor: TrackSearchInteractor
    private var searchString: String = SEARCH

    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable = Runnable { search() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val rootView = findViewById<LinearLayout>(R.id.search_root_view)
        btnBack = findViewById(R.id.btn_back)
        inputEditText = findViewById(R.id.inputEditText)
        clearButton = findViewById(R.id.clearIcon)
        imageError = findViewById(R.id.iv_Error)
        placeholderMessage = findViewById(R.id.tv_Error)
        refresh = findViewById(R.id.btn_refresh)
        trackRecycler = findViewById(R.id.rvTrack)
        historyRecycler = findViewById(R.id.rv_searchHistory)
        historySection = findViewById(R.id.searchHistorySection)
        clearHistoryButton = findViewById(R.id.btn_clear_history)
        progressBar = findViewById(R.id.progressBar)
        searchInteractor = Creator.provideTrackSearchInteractor()

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            WindowInsetsCompat.CONSUMED
        }

        inputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchDebounce()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        if (savedInstanceState != null) {
            searchString = savedInstanceState.getString(SEARCH_STRING, SEARCH)
            inputEditText.setText(searchString)
        }

        adapter = TrackAdapter(tracks) { track ->
            history.saveTrack(track)
            historyAdapter.notifyDataSetChanged()
        }

        historyAdapter = TrackAdapter(history.getHistory()) { track ->
            history.saveTrack(track)
            historyAdapter.notifyDataSetChanged()
        }

        trackRecycler.layoutManager = LinearLayoutManager(this)
        trackRecycler.adapter = adapter

        historyRecycler.layoutManager = LinearLayoutManager(this)
        historyRecycler.adapter = historyAdapter

        clearButton.setOnClickListener {
            inputEditText.setText("")
            hideKeyboard()
            tracks.clear()
            adapter.notifyDataSetChanged()
            val historyTracks = history.getHistory()
            if (historyTracks.isNotEmpty()) {
                historyAdapter.notifyDataSetChanged()
                historyAdapter.setTracks(historyTracks)
                setUiState(showHistory = true)
            } else {
                setUiState(showHistory = true)
                historySection.visibility = View.GONE
            }
        }

        refresh.setOnClickListener {
            search()
        }

        btnBack.setOnClickListener {
            finish()
        }

        inputEditText.post {
            inputEditText.requestFocus()
        }

        inputEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && inputEditText.text.isEmpty()) {
                val historyTracks = history.getHistory()
                if (historyTracks.isNotEmpty()) {
                    historyAdapter.setTracks(historyTracks)
                    historyAdapter.notifyDataSetChanged()
                    historySection.visibility = View.VISIBLE
                } else {
                    historySection.visibility = View.GONE
                }
            } else {
                historySection.visibility = View.GONE
            }
        }

        clearHistoryButton.setOnClickListener {
            history.clearHistory()
            historyAdapter.notifyDataSetChanged()
            historySection.visibility = View.GONE
        }

        inputEditText.doOnTextChanged { text, _, _, _ ->
            clearButton.isVisible = !text.isNullOrEmpty()
            searchString = text?.toString().orEmpty()
        }

        inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && inputEditText.text.isNotEmpty()) {
                search()
                true
            } else {
                false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    private fun search() {
        val query: String = inputEditText.text.toString()
        if (query.isNotEmpty()) {
            runOnUiThread {
                setUiState(isLoading = true)
            }
            searchInteractor.searchTrack(
                query, object : TrackSearchInteractor.Consumer<List<Track>> {
                    override fun consume(data: TrackSearchInteractor.Consumer.ConsumerData<List<Track>>) {
                        runOnUiThread {
                            if (data is TrackSearchInteractor.Consumer.ConsumerData.Error) {
                                setUiState(isError = true)
                            } else if (data is TrackSearchInteractor.Consumer.ConsumerData.Data) {
                                if (data.value.isNotEmpty()) {
                                    tracks.clear()
                                    tracks.addAll(data.value)
                                    adapter.setTracks(tracks)
                                    adapter.notifyDataSetChanged()
                                    setUiState(showResults = true)
                                } else {
                                    tracks.clear()
                                    adapter.notifyDataSetChanged()
                                    setUiState(isEmpty = true)
                                }
                            }
                        }
                    }
                })
        }
    }

    private fun setUiState(
        isLoading: Boolean = false,
        isError: Boolean = false,
        isEmpty: Boolean = false,
        showResults: Boolean = false,
        showHistory: Boolean = false
    ) {
        progressBar.isVisible = isLoading
        trackRecycler.isVisible = showResults
        historySection.isVisible = showHistory
        imageError.isVisible = isError || isEmpty
        placeholderMessage.isVisible = isError || isEmpty
        refresh.isVisible = isError

        if (isError) {
            placeholderMessage.text = getString(R.string.no_internet)
            imageError.setImageResource(R.drawable.image_no_internet)
        } else if (isEmpty) {
            placeholderMessage.text = getString(R.string.nothing_found)
            imageError.setImageResource(R.drawable.image_nothing_found)
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(inputEditText.windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_STRING, searchString)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        searchString = savedInstanceState.getString(SEARCH_STRING).orEmpty()
    }

    private fun searchDebounce() {
        handler.removeCallbacks(searchRunnable)
        handler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY)
    }

    companion object {
        private const val SEARCH_STRING = "SEARCH_STRING"
        private const val SEARCH = ""
        private const val SEARCH_DEBOUNCE_DELAY = 2000L
    }
}

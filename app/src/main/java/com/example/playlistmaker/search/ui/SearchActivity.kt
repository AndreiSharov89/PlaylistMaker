package com.example.playlistmaker.search.ui

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivitySearchBinding
import com.example.playlistmaker.player.ui.PlayerActivity
import com.example.playlistmaker.search.domain.Track

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter
    private var searchString: String = SEARCH
    private val viewModel: SearchViewModel by viewModels() {
        SearchViewModel.getViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            WindowInsetsCompat.CONSUMED
        }

        setupAdapters()
        setupUI()
        viewModel.showHistory()
        setupObservers()
        restoreSearchString(savedInstanceState)
    }

    private fun setupAdapters() {
        val onTrackClick = { track: Track ->
            viewModel.saveTrack(track)
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra(PlayerActivity.TRACK_DATA, track)
            startActivity(intent)
        }

        adapter = TrackAdapter(onTrackClick)
        historyAdapter = TrackAdapter(onTrackClick)

        binding.rvTrack.layoutManager = LinearLayoutManager(this)
        binding.rvTrack.adapter = adapter

        binding.rvSearchHistory.layoutManager = LinearLayoutManager(this)
        binding.rvSearchHistory.adapter = historyAdapter
    }

    private fun setupUI() {
        binding.inputEditText.doOnTextChanged { text, _, _, _ ->
            binding.clearIcon.isVisible = !text.isNullOrEmpty()
            viewModel.searchDebounce(text.toString())
        }

        binding.clearIcon.setOnClickListener {
            binding.inputEditText.text.clear()
            hideKeyboard()
        }

        binding.btnRefresh.setOnClickListener {
            viewModel.search(binding.inputEditText.text.toString())
        }

        binding.btnClearHistory.setOnClickListener {
            viewModel.clearHistory()
            hideKeyboard()
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.search(binding.inputEditText.text.toString())
                true
            } else {
                false
            }
        }
    }

    private fun setupObservers() {
        viewModel.observeSearchStateLiveData.observe(this) { state ->
            render(state)
        }
    }


    private fun render(state: SearchState) = with(binding) {
        when (state) {

            is SearchState.Loading -> {
                progressBar.isVisible = true
                rvTrack.isVisible = false
                searchHistorySection.isVisible = false
                ivError.isVisible = false
                tvError.isVisible = false
                btnRefresh.isVisible = false
            }

            is SearchState.Content -> {
                progressBar.isVisible = false
                rvTrack.isVisible = true
                searchHistorySection.isVisible = false
                ivError.isVisible = false
                tvError.isVisible = false
                btnRefresh.isVisible = false
                adapter.tracks = ArrayList(state.tracks)
                adapter.notifyDataSetChanged()
            }

            is SearchState.Error -> {
                progressBar.isVisible = false
                rvTrack.isVisible = false
                searchHistorySection.isVisible = false
                ivError.isVisible = true
                tvError.isVisible = true
                btnRefresh.isVisible = true
                ivError.setImageResource(R.drawable.image_no_internet)
                tvError.text = getString(R.string.no_internet)
            }

            is SearchState.Empty -> {
                progressBar.isVisible = false
                rvTrack.isVisible = false
                searchHistorySection.isVisible = false
                ivError.isVisible = true
                tvError.isVisible = true
                btnRefresh.isVisible = false
                ivError.setImageResource(R.drawable.image_nothing_found)
                tvError.text = getString(R.string.nothing_found)
            }

            is SearchState.History -> {
                progressBar.isVisible = false
                rvTrack.isVisible = false
                ivError.isVisible = false
                tvError.isVisible = false
                btnRefresh.isVisible = false
                if (state.tracks.isNotEmpty()) {
                    searchHistorySection.isVisible = true
                    historyAdapter.tracks = ArrayList(state.tracks)
                    historyAdapter.notifyDataSetChanged()
                } else {
                    searchHistorySection.isVisible = false
                }
            }

            is SearchState.EmptyHistory -> {
                progressBar.isVisible = false
                rvTrack.isVisible = false
                searchHistorySection.isVisible = false
                ivError.isVisible = false
                tvError.isVisible = false
                btnRefresh.isVisible = false
            }
        }
    }


    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.inputEditText.windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_STRING, searchString)
    }

    fun restoreSearchString(savedInstanceState: Bundle?) {
        savedInstanceState?.getString(SEARCH_STRING).orEmpty()
    }


    companion object {
        private const val SEARCH_STRING = "SEARCH_STRING"
        private const val SEARCH = ""
    }
}

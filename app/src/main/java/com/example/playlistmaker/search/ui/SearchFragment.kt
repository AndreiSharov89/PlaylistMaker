package com.example.playlistmaker.search.ui

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentSearchBinding
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.utils.debounce
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TrackAdapter
    private lateinit var historyAdapter: TrackAdapter
    private val viewModel by viewModel<SearchViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupUI()
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onScreenResumed()
    }

    private fun setupAdapters() {
        val onTrackClickDebounced = debounce<Track>(
            CLICK_DEBOUNCE_DELAY,
            viewLifecycleOwner.lifecycleScope,
            false
        ) { track ->
            viewModel.saveTrack(track)
            val action = SearchFragmentDirections.actionSearchFragmentToPlayerFragment(track)
            findNavController().navigate(action)
        }

        adapter = TrackAdapter(clickListener = { track -> onTrackClickDebounced(track) })
        historyAdapter = TrackAdapter(clickListener = { track -> onTrackClickDebounced(track) })

        binding.rvTrack.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTrack.adapter = adapter

        binding.rvSearchHistory.layoutManager = LinearLayoutManager(requireContext())
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

        binding.inputEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                viewModel.search(binding.inputEditText.text.toString())
                hideKeyboard()
                true
            } else {
                false
            }
        }
    }

    private fun setupObservers() {
        viewModel.observeSearchStateLiveData.observe(viewLifecycleOwner) { state ->
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
        val imm = requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.inputEditText.windowToken, 0)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val CLICK_DEBOUNCE_DELAY = 500L
    }
}

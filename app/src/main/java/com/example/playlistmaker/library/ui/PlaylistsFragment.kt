package com.example.playlistmaker.library.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistListBinding
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlaylistsViewModel by viewModel()
    private var playlistAdapter: PlaylistAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecycler()
        setupClicks()
        observeState()
        observeNavigationResults()
    }

    private fun setupRecycler() {
        playlistAdapter = PlaylistAdapter { playlist ->
            val action = LibraryFragmentDirections
                .actionLibraryFragmentToPlaylistFragment(playlist.id!!)
            requireParentFragment()
                .findNavController()
                .navigate(action)
        }

        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = playlistAdapter
    }

    private fun setupClicks() {
        binding.btnCreatePlaylist.setOnClickListener {
            findNavController()
                .navigate(R.id.action_libraryFragment_to_createPlaylistFragment)
        }
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner, ::render)
    }

    private fun observeNavigationResults() {
        observeNavigationResult(
            NavigationKeys.NEW_PLAYLIST_NAME
        ) { name ->
            showPlaylistSnackbar(R.string.playlist_created_message, name)
        }

        observeNavigationResult(
            NavigationKeys.DELETED_PLAYLIST_NAME
        ) { name ->
            showPlaylistSnackbar(R.string.playlist_deleted_message, name)
        }
    }

    private fun observeNavigationResult(
        key: String,
        onResult: (String) -> Unit
    ) {
        val backStackEntry = findNavController()
            .getBackStackEntry(R.id.libraryFragment)

        backStackEntry.savedStateHandle
            .getLiveData<String>(key)
            .observe(viewLifecycleOwner) { result ->
                result?.let {
                    onResult(it)
                    backStackEntry.savedStateHandle.remove<String>(key)
                }
            }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPlaylists()
    }

    private fun render(state: PlaylistsState) {
        when (state) {
            is PlaylistsState.Empty -> showEmptyState()
            is PlaylistsState.Content -> showContent(state)
        }
    }

    private fun showEmptyState() {
        binding.notFoundImage.isVisible = true
        binding.notFoundText.isVisible = true
        binding.recyclerView.isVisible = false
    }

    private fun showContent(state: PlaylistsState.Content) {
        binding.notFoundImage.isVisible = false
        binding.notFoundText.isVisible = false
        binding.recyclerView.isVisible = true
        playlistAdapter?.apply {
            playlists = state.playlists
            notifyDataSetChanged()
        }
    }

    private fun showPlaylistSnackbar(
        messageRes: Int,
        playlistName: String
    ) {
        val message = getString(messageRes, playlistName)
        val bottomNav =
            requireActivity().findViewById<View>(R.id.bottomNavigationView)

        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(
                ContextCompat.getColor(requireContext(), R.color.text_black)
            )
            .setTextColor(
                ContextCompat.getColor(requireContext(), R.color.text_white)
            )
            .apply {
                bottomNav?.let { anchorView = it }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        playlistAdapter = null
    }

    companion object {
        fun newInstance() = PlaylistsFragment()
    }

    private object NavigationKeys {
        const val NEW_PLAYLIST_NAME = "new_playlist_name"
        const val DELETED_PLAYLIST_NAME = "deleted_playlist_name"
    }
}
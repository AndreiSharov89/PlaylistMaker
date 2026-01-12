package com.example.playlistmaker.library.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistBinding
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlaylistsViewModel by viewModel()
    private var playlistAdapter: PlaylistAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlistAdapter = PlaylistAdapter { playlist ->
            // TODO: Handle playlist click
        }

        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = playlistAdapter

        binding.btnCreatePlaylist.setOnClickListener {
            findNavController().navigate(R.id.action_libraryFragment_to_createPlaylistFragment)
        }

        viewModel.state.observe(viewLifecycleOwner) {
            render(it)
        }

        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getLiveData<String>("new_playlist_name")
            ?.observe(viewLifecycleOwner) { playlistName ->
                if (playlistName != null) {
                    showPlaylistCreatedSnackbar(playlistName)
                    savedStateHandle.remove<String>("new_playlist_name")
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

    private fun showPlaylistCreatedSnackbar(playlistName: String) {
        val message = getString(R.string.playlist_created_message, playlistName)
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        playlistAdapter = null
    }

    companion object {
        fun newInstance() = PlaylistsFragment()
    }
}

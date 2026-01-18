package com.example.playlistmaker.playlist.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlaylistBinding
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.search.ui.TrackAdapter
import com.example.playlistmaker.utils.formatTrackCount
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.max


class PlaylistFragment : Fragment() {
    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!
    private val args: PlaylistFragmentArgs by navArgs()
    private val viewModel by viewModel<PlaylistViewModel> { parametersOf(args.playlistId) }

    private lateinit var trackAdapter: TrackAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTrackAdapter()
        setupBottomSheet()
        observeViewModel()

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupTrackAdapter() {
        trackAdapter = TrackAdapter(
            clickListener = { track ->
                val action =
                    PlaylistFragmentDirections.actionPlaylistFragmentToPlayerFragment(track)
                findNavController().navigate(action)
            },
            onTrackLongClick = { track ->
                showTrackDeletionDialog(track)
            }
        )
        binding.tracksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.tracksRecyclerView.adapter = trackAdapter
    }

    private fun setupBottomSheet() {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.tracksSheet).apply {
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.overlay.isVisible = false
                    }

                    else -> {
                        binding.overlay.isVisible = true
                    }
                }
                binding.overlay.isVisible = newState != BottomSheetBehavior.STATE_HIDDEN
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.overlay.alpha = max(0f, slideOffset)
            }
        })
    }

    private fun observeViewModel() {
        viewModel.loadPlaylist()
        viewModel.playlistData.observe(viewLifecycleOwner) { (playlist, timeString) ->
            binding.playlistTitle.text = playlist.name
            binding.playlistDescription.text = playlist.description
            binding.playlistCount.text = formatTrackCount(playlist.trackCount)
            binding.playlistTime.text = timeString
            Glide.with(this)
                .load(playlist.coverImagePath)
                .placeholder(R.drawable.track_placeholder_512)
                .centerCrop()
                .into(binding.playlistCover)
        }

        viewModel.tracks.observe(viewLifecycleOwner) { tracks ->
            trackAdapter.tracks = ArrayList(tracks)
            trackAdapter.notifyDataSetChanged()
            binding.tracksRecyclerView.isVisible = tracks.isNotEmpty()
        }
    }

    private fun showTrackDeletionDialog(track: Track) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.delete_track_dialog_title))
            .setNegativeButton(R.string.delete_track_dialog_no) { _, _ -> }
            .setPositiveButton(R.string.delete_track_dialog_yes) { _, _ ->
                viewModel.removeTrack(track.trackId.toString())
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

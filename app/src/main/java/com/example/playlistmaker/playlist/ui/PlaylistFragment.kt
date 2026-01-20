package com.example.playlistmaker.playlist.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.createplaylist.domain.Playlist
import com.example.playlistmaker.databinding.FragmentPlaylistBinding
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.search.ui.TrackAdapter
import com.example.playlistmaker.utils.formatTrackCount
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.min


class PlaylistFragment : Fragment() {
    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!
    private val args: PlaylistFragmentArgs by navArgs()
    private val viewModel by viewModel<PlaylistViewModel> { parametersOf(args.playlistId) }

    private lateinit var trackAdapter: TrackAdapter
    private lateinit var tracksSheetBehavior: BottomSheetBehavior<*>
    private lateinit var menuSheetBehavior: BottomSheetBehavior<LinearLayout>

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

        binding.shareButton.setOnClickListener { viewModel.onShareButtonClicked() }

        binding.menuButton.setOnClickListener {
            menuSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }

        binding.actionShare.setOnClickListener {
            viewModel.onShareButtonClicked()
            menuSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        binding.actionDelete.setOnClickListener {
            viewModel.playlistData.value?.first?.let { playlist ->
                showPlaylistDeletionDialog(playlist)
            }
        }

        binding.actionEdit.setOnClickListener {
            val action =
                PlaylistFragmentDirections.actionPlaylistFragmentToEditPlaylistFragment(args.playlistId)
            findNavController().navigate(action)
        }

        binding.tracksSheet.doOnLayout {
            val availableHeight = binding.root.height - binding.shareButton.bottom
            tracksSheetBehavior.peekHeight = min(
                resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height),
                availableHeight
            )
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

        tracksSheetBehavior = BottomSheetBehavior.from(binding.tracksSheet).apply {
            state = BottomSheetBehavior.STATE_COLLAPSED
        }
        tracksSheetBehavior.addBottomSheetCallback(object :
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
                binding.overlay.apply {
                    alpha = slideOffset.coerceIn(0f, 1f)
                    isVisible = slideOffset > 0f
                }
            }
        })

        menuSheetBehavior = BottomSheetBehavior.from(binding.menuSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
            isHideable = true
            skipCollapsed = true
        }
        menuSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        binding.overlay.isVisible = false
                        tracksSheetBehavior.isDraggable = true
                    }

                    else -> {
                        binding.overlay.alpha = 0.5f
                        tracksSheetBehavior.isDraggable = false

                    }
                }
                binding.overlay.isVisible = newState != BottomSheetBehavior.STATE_HIDDEN
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                }
            })
            .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.text_black))
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.text_white))
            .show()
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

            binding.playlistName.text = playlist.name
            binding.trackCount.text = formatTrackCount(playlist.trackCount)
            Glide.with(this)
                .load(playlist.coverImagePath)
                .placeholder(R.drawable.track_placeholder_45)
                .centerCrop()
                .into(binding.playlistImageSmall)
        }

        viewModel.tracks.observe(viewLifecycleOwner) { tracks ->
            trackAdapter.tracks = ArrayList(tracks.asReversed())
            trackAdapter.notifyDataSetChanged()
            binding.tracksRecyclerView.isVisible = tracks.isNotEmpty()
            if (tracks.isEmpty()) {
                tracksSheetBehavior.isDraggable = false
                binding.tracksSheet.isVisible = false
                binding.playlistEmpty.isVisible = true
            } else {
                tracksSheetBehavior.isDraggable = true
                binding.tracksSheet.isVisible = true
                binding.playlistEmpty.isVisible = false
            }
        }
        viewModel.sharePlaylistEvent.observe(viewLifecycleOwner) { shareText ->
                sharePlaylist(shareText)
        }
        viewModel.toastEvent.observe(viewLifecycleOwner) { resId ->
            showSnackbar(getString(resId))
        }
        viewModel.playlistDeleted.observe(viewLifecycleOwner) { isDeleted ->
            if (isDeleted) {
                val playlistName = viewModel.playlistData.value?.first?.name
                if (playlistName != null) {
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        "deleted_playlist_name",
                        playlistName
                    )
                }
                findNavController().navigateUp()
            }
        }
    }

    private fun showTrackDeletionDialog(track: Track) {
        MaterialAlertDialogBuilder(requireContext(), R.style.App_MaterialAlertDialogTheme)
            .setTitle(getString(R.string.delete_track_dialog_title))
            .setNegativeButton(R.string.delete_track_dialog_no) { _, _ -> }
            .setPositiveButton(R.string.delete_track_dialog_yes) { _, _ ->
                viewModel.removeTrack(track.trackId.toString())
            }
            .show()
    }

    private fun showPlaylistDeletionDialog(playlist: Playlist) {
        menuSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        MaterialAlertDialogBuilder(requireContext(), R.style.App_MaterialAlertDialogTheme)
            .setTitle(getString(R.string.delete_playlist_dialog_title, playlist.name))
            .setNegativeButton(R.string.delete_track_dialog_no) { _, _ ->
            }
            .setPositiveButton(R.string.delete_track_dialog_yes) { _, _ ->
                viewModel.removePlaylist()
            }
            .show()
    }

    private fun sharePlaylist(text: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }

        val chooserIntent =
            Intent.createChooser(shareIntent, getString(R.string.share_playlist_title))
        if (shareIntent.resolveActivity(requireActivity().packageManager) != null) {
            startActivity(chooserIntent)
        } else {
            showSnackbar(getString(R.string.no_apps))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.example.playlistmaker.player.ui

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentPlayerBinding
import com.example.playlistmaker.search.domain.Track
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.max

class PlayerFragment : Fragment() {
    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    private val args: PlayerFragmentArgs by navArgs()
    private val track: Track by lazy { args.track }
    private val viewModel: PlayerViewModel by viewModel {
        parametersOf(track)
    }
    private var bottomSheetAdapter: BottomsheetPlaylistAdapter? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        bindTrackData(track)
        loadCoverImage()

        viewModel.observeUiStateLiveData.observe(viewLifecycleOwner) { state ->
            render(state)
        }
        viewModel.observePlaylists().observe(viewLifecycleOwner) { playlists ->
            bottomSheetAdapter?.let {
                it.playlists = playlists
                it.notifyDataSetChanged()
            }
        }
        setupBottomSheet()
        binding.btnPlay.setOnClickListener { viewModel.onPlayButtonClicked() }
        binding.btnLike.setOnClickListener {
            viewModel.onFavoriteClicked()
        }
        viewModel.preparePlayer()

        viewModel.observeSnackbarMessage().observe(viewLifecycleOwner) { message ->
            showSnackbar(message)
        }
        viewModel.observeAddToPlaylistResult().observe(viewLifecycleOwner) { result ->
            when (result) {
                is AddToPlaylistResult.Added -> {
                    showSnackbar("Добавлено в плейлист ${result.playlistName}")
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }

                is AddToPlaylistResult.AlreadyAdded -> {
                    showSnackbar("Трек уже добавлен в плейлист ${result.playlistName}")
                }
            }
        }

        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle
        savedStateHandle?.getLiveData<String>("new_playlist_name")
            ?.observe(viewLifecycleOwner) { playlistName ->
                playlistName?.let {
                    viewModel.onPlaylistCreated(playlistName)
                    savedStateHandle.remove<String>("new_playlist_name")
                }
            }
    }

    private fun setupBottomSheet() {
        bottomSheetAdapter = BottomsheetPlaylistAdapter { playlist ->
            viewModel.addTrackToPlaylist(playlist)
        }

        binding.playlistsRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())
        binding.playlistsRecyclerView.adapter = bottomSheetAdapter

        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet).apply {
            state = BottomSheetBehavior.STATE_HIDDEN
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
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                binding.overlay.alpha = max(0f, slideOffset)
            }
        })

        binding.btnAddToPlaylist.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            viewModel.loadPlaylists()
        }

        binding.newPlaylistButton.setOnClickListener {
            findNavController().navigate(R.id.action_playerFragment_to_createPlaylistFragment)
        }
    }

    override fun onPause() {
        super.onPause()
        if (!requireActivity().isChangingConfigurations) {
            viewModel.onPause()
        }
    }

    override fun onDestroyView() {
        _binding = null
        bottomSheetAdapter = null
        super.onDestroyView()
    }

    private fun render(state: PlayerUiState) {
        when (state) {
            is PlayerUiState.Preparing -> {
                binding.btnPlay.isEnabled = false
            }

            is PlayerUiState.Content -> {
                binding.btnPlay.isEnabled = true
                binding.btnPlay.setImageResource(
                    if (state.isPlaying) R.drawable.ic_pause_100 else R.drawable.ic_play_100
                )

                binding.tvTrackTimeCurrent.text = state.progressText
                renderFavoriteButton(state.isFavorite)
            }
        }
    }

    private fun renderFavoriteButton(isFavorite: Boolean) {
        val favoriteIcon = if (isFavorite) {
            R.drawable.ic_like_pressed_51
        } else {
            R.drawable.ic_like_51
        }
        binding.btnLike.setImageResource(favoriteIcon)
    }

    private fun bindTrackData(track: Track) {
        binding.tvPlayerTrackName.text = track.trackName
        binding.tvPlayerArtistName.text = track.artistName
        binding.tvTrackLength.text = SimpleDateFormat(
            "mm:ss",
            Locale.getDefault()
        ).format(track.trackTimeMillis)

        binding.tvTrackAlbum.apply {
            text = track.collectionName
            isVisible = !text.isNullOrEmpty()
        }

        binding.tvTrackYear.apply {
            text = track.releaseDate?.take(4).orEmpty()
            isVisible = text.isNotEmpty()
        }

        binding.tvTrackGenre.apply {
            text = track.primaryGenreName
            isVisible = !text.isNullOrEmpty()
        }

        binding.tvTrackCountry.apply {
            text = track.country
            isVisible = !text.isNullOrEmpty()
        }
        renderFavoriteButton(track.isFavorite)
    }

    private fun loadCoverImage() {
        Glide.with(this)
            .load(viewModel.highResCoverUrl)
            .placeholder(R.drawable.track_placeholder_312)
            .transform(RoundedCorners(dpToPx(resources.getDimension(R.dimen.dp_8))))
            .into(binding.ivCover)
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

    private fun dpToPx(dp: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dp, resources.displayMetrics).toInt()

}

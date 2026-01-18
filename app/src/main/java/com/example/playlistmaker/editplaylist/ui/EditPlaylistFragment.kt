package com.example.playlistmaker.editplaylist.ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.createplaylist.ui.CreatePlaylistFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class EditPlaylistFragment : CreatePlaylistFragment() {

    private val args: EditPlaylistFragmentArgs by navArgs()
    private val playlistId by lazy { args.playlistIdEdit }
    override val viewModel: EditPlaylistViewModel by viewModel {
        parametersOf(playlistId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.playlist.observe(viewLifecycleOwner) { playlist ->
            binding.playlistName.setText(playlist.name)
            binding.playlistDescription.setText(playlist.description)
            binding.createButton.text = getString(R.string.save_button_text)
            binding.tittleTextView.text = getString(R.string.edit_playlist_tittle, playlist.name)

            playlist.coverImagePath?.let {
                viewModel.preloadCover(it)
            }

            viewModel.selectedCoverUri.observe(viewLifecycleOwner) { uri ->
                uri?.let { loadCoverImage(it) }
            }
        }
        binding.createButton.setOnClickListener {
            viewModel.updatePlaylist(
                name = binding.playlistName.text.toString(),
                description = binding.playlistDescription.text.toString()
            )
            findNavController().navigateUp()
        }
        binding.ivPlBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.ivImageAlbum.setOnClickListener {
            super.requestPermission()
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            }
        )

    }

    private fun loadCoverImage(uri: Uri) {
        binding.ivImageAlbum.setBackgroundResource(R.drawable.rounded_corners)
        Glide.with(requireContext())
            .load(uri)
            .placeholder(R.drawable.track_placeholder_312)
            .transform(
                MultiTransformation(
                    CenterCrop(),
                    RoundedCorners(resources.getDimensionPixelSize(R.dimen.dp_8))
                )
            )
            .into(binding.ivImageAlbum)
    }
}

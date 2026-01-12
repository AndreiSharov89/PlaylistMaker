package com.example.playlistmaker.createplaylist.ui

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentCreatePlaylistBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel


class CreatePlaylistFragment : Fragment() {
    private var _binding: FragmentCreatePlaylistBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreatePlaylistViewModel by viewModel()
    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                viewModel.onImageSelected(uri)
            }
        }
    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                viewModel.onImageSelected(uri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (viewModel.hasUnsavedData(
                            binding.playlistName.text.toString(),
                            binding.playlistDescription.text.toString()
                        )
                    ) {
                        showExitConfirmationDialog()
                    } else {
                        findNavController().navigateUp()
                    }
                }
            })
    }

    private fun setupUI() {
        binding.ivPlBack.setOnClickListener {
            if (viewModel.hasUnsavedData(
                    binding.playlistName.text.toString(),
                    binding.playlistDescription.text.toString()
                )
            ) {
                showExitConfirmationDialog()
            } else {
                findNavController().navigateUp()
            }
        }
        binding.ivImageAlbum.setOnClickListener {
            try {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } catch (e: ActivityNotFoundException) {
                getContent.launch("image/*")
            }
        }

        binding.playlistName.setOnFocusChangeListener { view, hasFocus ->
            val text = binding.playlistName.text
            val colorId =
                if (hasFocus && !text.isNullOrEmpty()) R.color.YP_blue else R.color.YP_text_gray
            val colorInt = ContextCompat.getColor(requireContext(), colorId)
            binding.clueName.setTextColor(colorInt)
        }

        binding.playlistDescription.setOnFocusChangeListener { view, hasFocus ->
            val text = binding.playlistDescription.text
            val colorId =
                if (hasFocus && !text.isNullOrEmpty()) R.color.YP_blue else R.color.YP_text_gray
            val colorInt = ContextCompat.getColor(requireContext(), colorId)
            binding.clueDescription.setTextColor(colorInt)
        }


        binding.playlistName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                setCreateButtonState(!s.isNullOrEmpty())
                binding.clueName.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                val colorInt = ContextCompat.getColor(requireContext(), R.color.YP_blue)
                binding.clueName.setTextColor(colorInt)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.playlistDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.clueDescription.visibility =
                    if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
                val colorInt = ContextCompat.getColor(requireContext(), R.color.YP_blue)
                binding.clueDescription.setTextColor(colorInt)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.createButton.setOnClickListener {
            viewModel.createPlaylist(
                name = binding.playlistName.text.toString(),
                description = binding.playlistDescription.text.toString()
            )
        }
    }

    private fun setupObservers() {
        viewModel.selectedCoverUri.observe(viewLifecycleOwner) { uri ->
            Glide.with(requireContext())
                .load(uri)
                .placeholder(R.drawable.track_placeholder_312)
                .centerCrop()
                .transform(RoundedCorners(resources.getDimensionPixelSize(R.dimen.dp_8)))
                .into(binding.ivImageAlbum)
        }

        viewModel.playlistCreated.observe(viewLifecycleOwner) { playlistName ->
            if (playlistName != null) {
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    "new_playlist_name",
                    playlistName
                )
                findNavController().navigateUp()
            }
        }
    }

    private fun setCreateButtonState(isEnabled: Boolean) {
        binding.createButton.isEnabled = isEnabled
        val colorRes = if (isEnabled) R.color.YP_blue else R.color.YP_text_gray
        binding.createButton.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), colorRes)
    }

    private fun showExitConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_title)
            .setMessage(R.string.dialog_text)
            .setNeutralButton(R.string.dialog_cancel) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(R.string.dialog_close) { _, _ -> findNavController().navigateUp() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
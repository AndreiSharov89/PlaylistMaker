package com.example.playlistmaker.createplaylist.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.FragmentCreatePlaylistBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.markodevcic.peko.PermissionRequester
import com.markodevcic.peko.PermissionResult
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


open class CreatePlaylistFragment : Fragment() {
    private var _binding: FragmentCreatePlaylistBinding? = null
    protected val binding get() = _binding!!
    open val viewModel: CreatePlaylistViewModel by viewModel()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PermissionRequester.instance()
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
            requestPermission()
        }

        setupTextField(binding.playlistName, binding.clueName) { text ->
            setCreateButtonState(text.isNotEmpty())
        }

        setupTextField(binding.playlistDescription, binding.clueDescription)

        binding.createButton.setOnClickListener {
            viewModel.createPlaylist(
                name = binding.playlistName.text.toString(),
                description = binding.playlistDescription.text.toString()
            )
        }
    }

    private fun setupObservers() {
        viewModel.selectedCoverUri.observe(viewLifecycleOwner) { uri ->
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

        viewModel.playlistCreated.observe(viewLifecycleOwner) { playlistName ->
            playlistName?.let { name ->
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    "new_playlist_name",
                    name
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

    protected fun requestPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        lifecycleScope.launch {
            PermissionRequester.instance().request(permission).collect { result ->
                when (result) {
                    is PermissionResult.Granted -> {
                        openImagePicker()
                    }

                    is PermissionResult.Denied.DeniedPermanently -> {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.data = Uri.fromParts("package", requireContext().packageName, null)
                        requireContext().startActivity(intent)
                    }

                    is PermissionResult.Denied.NeedsRationale -> {
                        Snackbar.make(
                            binding.root,
                            R.string.storage_permission_rationale,
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    is PermissionResult.Cancelled -> {
                        //requestPermission()
                    }
                }
            }
        }
    }

    private fun openImagePicker() {
        try {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } catch (e: ActivityNotFoundException) {
            getContent.launch("image/*")
        }
    }
    private fun setupTextField(
        editText: EditText,
        clueView: TextView,
        onTextChangedAction: ((String) -> Unit)? = null
    ) {
        editText.setOnFocusChangeListener { _, hasFocus ->
            val text = editText.text
            val colorId =
                if (hasFocus && !text.isNullOrEmpty()) R.color.YP_blue else R.color.YP_text_gray
            val colorInt = ContextCompat.getColor(requireContext(), colorId)
            clueView.setTextColor(colorInt)
        }

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s?.toString().orEmpty()
                clueView.visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
                clueView.setTextColor(ContextCompat.getColor(requireContext(), R.color.YP_blue))
                onTextChangedAction?.invoke(text)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        PermissionRequester.instance()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.example.playlistmaker.settings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.playlistmaker.databinding.FragmentSettingsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {
    private val viewModel by viewModel<SettingsViewModel>()
    private var _binding: FragmentSettingsBinding? = null
    private val binding: FragmentSettingsBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.observeIsDarkTheme().observe(viewLifecycleOwner) { isDark ->
            binding.sw.isChecked = isDark
            AppCompatDelegate.setDefaultNightMode(
                if (isDark)
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        binding.sw.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onThemeSwitchToggled(isChecked)
        }

        binding.btnShare.setOnClickListener {
            viewModel.shareApp()
        }

        binding.btnSupport.setOnClickListener {
            viewModel.openSupport()
        }

        binding.btnEula.setOnClickListener {
            viewModel.openTerms()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
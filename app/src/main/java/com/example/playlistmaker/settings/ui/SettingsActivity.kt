package com.example.playlistmaker.settings.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.playlistmaker.databinding.ActivitySettingsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : AppCompatActivity() {
    private val viewModel by viewModel<SettingsViewModel>()
    private lateinit var binding: ActivitySettingsBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        ViewCompat.setOnApplyWindowInsetsListener(binding.settingsRootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom


            )
            WindowInsetsCompat.CONSUMED
        }


        viewModel.observeIsDarkTheme().observe(this) { isDark ->
            binding.sw.isChecked = isDark
            AppCompatDelegate.setDefaultNightMode(
                if (isDark)
                    AppCompatDelegate.MODE_NIGHT_YES
                else
                    AppCompatDelegate.MODE_NIGHT_NO
            )
        }


        binding.btnBack.setOnClickListener {
            finish()
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
}
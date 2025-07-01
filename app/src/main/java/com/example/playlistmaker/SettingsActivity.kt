package com.example.playlistmaker

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val rootView = findViewById<LinearLayout>(R.id.settings_root_view)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom

            )
            WindowInsetsCompat.CONSUMED
        }

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }

        val switch = findViewById<SwitchMaterial>(R.id.sw)
        switch.isUseMaterialThemeColors = false
        switch.thumbTintList = ContextCompat.getColorStateList(this, R.color.sw_thumb)
        switch.trackTintList = ContextCompat.getColorStateList(this, R.color.sw_track)
        switch.background = null
        switch.isChecked = (applicationContext as App).darkTheme
        switch.setOnCheckedChangeListener { switcher, checked ->
            (applicationContext as App).switchTheme(checked)
        }
        val shareButton = findViewById<LinearLayout>(R.id.btn_share)
        shareButton.setOnClickListener {
            val message = getString(R.string.share_url)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            startActivity(Intent.createChooser(shareIntent, "Поделиться приложением"))
        }
        val btnSupport = findViewById<LinearLayout>(R.id.btn_support)
        btnSupport.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = Uri.parse("mailto:")
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_email)))
            emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.support_text))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_subj))
            startActivity(emailIntent)
        }
        val btnEULA = findViewById<LinearLayout>(R.id.btn_eula)
        btnEULA.setOnClickListener {
            val eulaURL = Uri.parse(getString(R.string.eula_url))
            val eulaIntent = Intent(Intent.ACTION_VIEW, eulaURL)
            startActivity(eulaIntent)
        }
    }
}
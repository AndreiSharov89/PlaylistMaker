package com.example.playlistmaker

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }
    }
}
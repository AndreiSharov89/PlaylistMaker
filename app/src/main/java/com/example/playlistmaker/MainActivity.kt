package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnSearch = findViewById<Button>(R.id.btn_search)
        val btnSearchCliclListener: View.OnClickListener = object : View.OnClickListener{
            override fun onClick(v: View?) {
                val displayIntent = Intent(this@MainActivity, SearchActivity::class.java)
                startActivity(displayIntent)
            }
        }
        btnSearch.setOnClickListener(btnSearchCliclListener)

        val btnLibrary = findViewById<Button>(R.id.btn_library)
        btnLibrary.setOnClickListener {
            val displayIntent = Intent(this, LibraryActivity::class.java)
            startActivity(displayIntent)
        }

        val btnSettings = findViewById<Button>(R.id.btn_settings)
        btnSettings.setOnClickListener {
            val displayIntent = Intent(this, SettingsActivity::class.java)
            startActivity(displayIntent)
        }
    }
}
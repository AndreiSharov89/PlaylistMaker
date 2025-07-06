package com.example.playlistmaker

import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerActivity : AppCompatActivity() {
    companion object {
        const val TRACK_DATA = "TrackData"
    }

    private val track: Track by lazy {
        val json = intent.getStringExtra(TRACK_DATA)
        Gson().fromJson(json, object : TypeToken<Track>() {}.type)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        val rootView = findViewById<ConstraintLayout>(R.id.clPlayerMain)
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

        findViewById<androidx.appcompat.widget.AppCompatImageButton>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        bindTrackData(track)
        loadImage()
    }

    private fun bindTrackData(track: Track) {
        val titleView = findViewById<TextView>(R.id.tvPlayerTrackName)
        val artistView = findViewById<TextView>(R.id.tvPlayerArtistName)
        val lengthView = findViewById<TextView>(R.id.tvTrackLength)
        val albumLabel = findViewById<TextView>(R.id.tvTrackAlbumLabel)
        val albumView = findViewById<TextView>(R.id.tvTrackAlbum)
        val yearLabel = findViewById<TextView>(R.id.tvTrackYearLabel)
        val yearView = findViewById<TextView>(R.id.tvTrackYear)
        val genreLabel = findViewById<TextView>(R.id.tvTrackGenreLabel)
        val genreView = findViewById<TextView>(R.id.tvTrackGenre)
        val countryLabel = findViewById<TextView>(R.id.tvTrackCountryLabel)
        val countryView = findViewById<TextView>(R.id.tvTrackCountry)

        titleView.text = track.trackName
        artistView.text = track.artistName
        lengthView.text = SimpleDateFormat(
            "mm:ss",
            Locale.getDefault()
        ).format(track.trackTimeMillis)

        albumLabel.visibility = if (track.collectionName.isNullOrEmpty()) View.GONE
            else View.VISIBLE
        albumView.visibility = albumLabel.visibility
        albumView.text = track.collectionName

        yearLabel.visibility = if (track.releaseDate.isNullOrEmpty()) View.GONE
            else View.VISIBLE
        yearView.visibility = yearLabel.visibility
        yearView.text =
            (track.releaseDate ?: "").let { if (it.length >= 4) it.substring(0, 4) else it }

        genreLabel.visibility = if (track.primaryGenreName.isNullOrEmpty()) View.GONE
            else View.VISIBLE
        genreView.visibility = genreLabel.visibility
        genreView.text = track.primaryGenreName

        countryLabel.visibility = if (track.country.isNullOrEmpty()) View.GONE
            else View.VISIBLE
        countryView.visibility = countryLabel.visibility
        countryView.text = track.country
    }

    private fun loadImage() {
        val coverUrl = track.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")

        Glide.with(this)
            .load(Uri.parse(coverUrl))
            .placeholder(R.drawable.track_placeholder_312)
            .transform(RoundedCorners(dpToPx(resources.getDimension(R.dimen.dp_8))))
            .into(findViewById(R.id.ivCover))
    }

    private fun dpToPx(dp: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dp, resources.displayMetrics).toInt()
}

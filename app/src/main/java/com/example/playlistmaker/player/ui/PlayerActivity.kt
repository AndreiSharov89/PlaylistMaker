package com.example.playlistmaker.player.ui

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityPlayerBinding
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.player.domain.PlayerState
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    private val track: Track by lazy {
        intent.getParcelableExtra(TRACK_DATA, Track::class.java)!!
    }

    private val viewModel: PlayerViewModel by viewModels {
        PlayerViewModel.getFactory(track.previewUrl ?: "")
    }

    private lateinit var binding: ActivityPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.clPlayerMain) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            WindowInsetsCompat.CONSUMED
        }

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        bindTrackData(track)
        loadImage()

        viewModel.playerStateObserver.observe(this) { state ->
            updatePlayButton(state)
        }

        viewModel.timerTextObserver.observe(this) { time ->
            binding.tvTrackTimeCurrent.text = time
        }

        binding.btnPlay.setOnClickListener { viewModel.onPlayButtonClicked() }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    private fun updatePlayButton(state: PlayerState) {
        binding.btnPlay.setImageResource(
            when (state) {
                PlayerState.Playing -> R.drawable.ic_pause_100
                else -> R.drawable.ic_play_100
            }
        )
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
            visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

        binding.tvTrackYear.apply {
            text = track.releaseDate?.take(4) ?: ""
            visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
        }

        binding.tvTrackGenre.apply {
            text = track.primaryGenreName
            visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

        binding.tvTrackCountry.apply {
            text = track.country
            visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun loadImage() {
        val coverUrl = track.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")
        Glide.with(this)
            .load(coverUrl)
            .placeholder(R.drawable.track_placeholder_312)
            .transform(RoundedCorners(dpToPx(resources.getDimension(R.dimen.dp_8))))
            .into(binding.ivCover)
    }

    private fun dpToPx(dp: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dp, resources.displayMetrics).toInt()

    companion object {
        const val TRACK_DATA = "TrackData"
    }
}

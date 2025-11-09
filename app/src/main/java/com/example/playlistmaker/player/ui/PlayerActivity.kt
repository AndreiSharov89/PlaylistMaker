package com.example.playlistmaker.player.ui

import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivityPlayerBinding
import com.example.playlistmaker.search.domain.Track
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    private val track: Track by lazy {
        intent.getParcelableExtra(TRACK_DATA, Track::class.java)!!
    }

    private val viewModel: PlayerViewModel by viewModel {
        parametersOf(track.previewUrl ?: "", track.artworkUrl100)
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
        loadCoverImage()
        viewModel.observeUiStateLiveData.observe(this) { state ->
            render(state)
        }
        binding.btnPlay.setOnClickListener { viewModel.onPlayButtonClicked() }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    private fun render(state: PlayerUiState) {
        when (state) {
            is PlayerUiState.Preparing -> {
                binding.btnPlay.isEnabled = false
            }

            is PlayerUiState.Content -> {
                binding.btnPlay.isEnabled = true
                binding.btnPlay.setImageResource(
                    if (state.isPlaying) R.drawable.ic_pause_100 else R.drawable.ic_play_100
                )
                binding.tvTrackTimeCurrent.text = state.progressText
            }
        }
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
            isVisible = !text.isNullOrEmpty()
        }

        binding.tvTrackYear.apply {
            text = track.releaseDate?.take(4).orEmpty()
            isVisible = text.isNotEmpty()
        }

        binding.tvTrackGenre.apply {
            text = track.primaryGenreName
            isVisible = !text.isNullOrEmpty()
        }

        binding.tvTrackCountry.apply {
            text = track.country
            isVisible = !text.isNullOrEmpty()
        }
    }

    private fun loadCoverImage() {
        Glide.with(this)
            .load(viewModel.highResCoverUrl)
            .placeholder(R.drawable.track_placeholder_312)
            .transform(RoundedCorners(dpToPx(resources.getDimension(R.dimen.dp_8))))
            .into(binding.ivCover)
    }


    private fun dpToPx(dp: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dp, resources.displayMetrics).toInt()

    companion object {
        const val TRACK_DATA = "track"
    }
}

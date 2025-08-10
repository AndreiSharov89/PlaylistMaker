package com.example.playlistmaker.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.Creator
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.interactors.PlayerInteractor
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.PlayerState
import java.text.SimpleDateFormat
import java.util.Locale

class PlayerActivity : AppCompatActivity() {

    private val track: Track by lazy {
        intent.getParcelableExtra(TRACK_DATA, Track::class.java)!!
    }
    private val player: PlayerInteractor by lazy {
        Creator.providePlayerInteractor()
    }
    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    private lateinit var playButton: ImageView
    private lateinit var timerText: TextView
    private var state: PlayerState = PlayerState.Default


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

        findViewById<View>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        playButton = findViewById(R.id.btnPlay)
        timerText = findViewById(R.id.tvTrackTimeCurrent)

        bindTrackData(track)
        loadImage()

        player.preparePlayer(
            url = track.previewUrl ?: "",
            onReady = {
                state = PlayerState.Prepared
                updatePlayButton()
                timerText.text = formatTime(player.getCurrentPosition())
            },
            onCompletion = {
                state = PlayerState.Prepared
                updatePlayButton()
                stopTimer()
                timerText.text = formatTime(player.getCurrentPosition())
            }
        )

        playButton.setOnClickListener { togglePlayback() }
    }

    override fun onPause() {
        super.onPause()
        if (player.isPlaying()) {
            player.pause()
            state = PlayerState.Paused
            updatePlayButton()
            stopTimer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
        stopTimer()
    }

    private fun togglePlayback() {
        when (state) {
            PlayerState.Playing -> {
                player.pause()
                state = PlayerState.Paused
                stopTimer()
            }

            PlayerState.Prepared, PlayerState.Paused -> {
                player.start()
                state = PlayerState.Playing
                startTimer()
            }

            else -> {}
        }
        updatePlayButton()
    }

    private fun startTimer() {
        timerRunnable = object : Runnable {
            override fun run() {
                if (state == PlayerState.Playing) {
                    timerText.text = formatTime(player.getCurrentPosition())
                    handler.postDelayed(this, DELAY)
                }
            }
        }
        handler.post(timerRunnable!!)
    }

    private fun stopTimer() {
        timerRunnable?.let { handler.removeCallbacks(it) }
        timerRunnable = null
    }

    private fun updatePlayButton() {
        playButton.setImageResource(
            when (state) {
                PlayerState.Playing -> R.drawable.ic_pause_100
                else -> R.drawable.ic_play_100
            }
        )
    }

    private fun formatTime(ms: Int): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun bindTrackData(track: Track) {
        findViewById<TextView>(R.id.tvPlayerTrackName).text = track.trackName
        findViewById<TextView>(R.id.tvPlayerArtistName).text = track.artistName
        findViewById<TextView>(R.id.tvTrackLength).text = SimpleDateFormat(
            "mm:ss",
            Locale.getDefault()
        ).format(track.trackTimeMillis)

        findViewById<TextView>(R.id.tvTrackAlbum).apply {
            text = track.collectionName
            visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

        findViewById<TextView>(R.id.tvTrackYear).apply {
            text = track.releaseDate?.take(4) ?: ""
            visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
        }

        findViewById<TextView>(R.id.tvTrackGenre).apply {
            text = track.primaryGenreName
            visibility = if (text.isNullOrEmpty()) View.GONE else View.VISIBLE
        }

        findViewById<TextView>(R.id.tvTrackCountry).apply {
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
            .into(findViewById(R.id.ivCover))
    }

    private fun dpToPx(dp: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dp, resources.displayMetrics).toInt()

    companion object {
        const val TRACK_DATA = "TrackData"
        private const val DELAY = 200L
    }
}

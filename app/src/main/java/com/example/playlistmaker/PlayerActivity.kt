package com.example.playlistmaker

import android.media.MediaPlayer
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
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.net.toUri

class PlayerActivity : AppCompatActivity() {

    private val track: Track by lazy {
        intent.getParcelableExtra(TRACK_DATA, Track::class.java)!!
    }
    private var mainThreadHandler: Handler? = null
    private var timerRunnable: Runnable? = null
    private lateinit var play: ImageView
    private lateinit var timer: TextView
    private var mediaPlayer = MediaPlayer()
    private var playerState = STATE_DEFAULT
    private val formater = SimpleDateFormat("mm:ss", Locale.getDefault())


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

        findViewById<AppCompatImageButton>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        val url: String = track.previewUrl.toString()
        play = findViewById(R.id.btnPlay)
        timer = findViewById(R.id.tvTrackTimeCurrent)
        mainThreadHandler = Handler(Looper.getMainLooper())

        bindTrackData(track)
        loadImage()
        preparePlayer(url)
        play.setOnClickListener {
            playbackControl()
        }
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        timerRunnable?.let {
            mainThreadHandler?.removeCallbacksAndMessages(it)
            timerRunnable = null
        }
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
            .load(coverUrl.toUri())
            .placeholder(R.drawable.track_placeholder_312)
            .transform(RoundedCorners(dpToPx(resources.getDimension(R.dimen.dp_8))))
            .into(findViewById(R.id.ivCover))
    }

    private fun dpToPx(dp: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, dp, resources.displayMetrics).toInt()

    private fun preparePlayer(url: String) {
        mediaPlayer.setDataSource(url)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            play.setImageResource(R.drawable.ic_play_100)
            timer.text = formater.format(0)
            playerState = STATE_PREPARED
        }
        mediaPlayer.setOnCompletionListener {
            play.setImageResource(R.drawable.ic_play_100)
            mainThreadHandler?.removeCallbacksAndMessages(null)
            timer.text = formater.format(0)
            playerState = STATE_PREPARED
        }
    }

    private fun startPlayer() {
        mediaPlayer.start()
        play.setImageResource(R.drawable.ic_pause_100)
        playerState = STATE_PLAYING
    }

    private fun pausePlayer() {
        mediaPlayer.pause()
        timerRunnable = null
        play.setImageResource(R.drawable.ic_play_100)
        playerState = STATE_PAUSED
    }

    private fun playbackControl() {
        when (playerState) {
            STATE_PLAYING -> {
                pausePlayer()
            }

            STATE_PREPARED, STATE_PAUSED -> {
                startPlayer()
                timerRunnable = createTimerRunnable()
                mainThreadHandler?.post(timerRunnable!!)
            }
        }
    }

    private fun createTimerRunnable(): Runnable {
        return object : Runnable {
            override fun run() {
                if (playerState == STATE_PLAYING) {
                    timer.text = formater.format(mediaPlayer.currentPosition)
                    mainThreadHandler?.postDelayed(this, DELAY)
                } else {
                    mainThreadHandler?.removeCallbacks(this)
                }
            }
        }
    }

    companion object {
        const val TRACK_DATA = "TrackData"
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val DELAY = 200L
    }
}

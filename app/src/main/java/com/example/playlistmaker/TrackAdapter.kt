package com.example.playlistmaker

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class TrackAdapter(
    private var data: List<Track> = listOf(),
    private val onItemClick: (Track) -> Unit
) : RecyclerView.Adapter<TrackViewHolder>() {
    private var isClickAllowed = true
    private val handler = Handler(Looper.getMainLooper())
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        return TrackViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = data[position]
        holder.bind(track)
        holder.itemView.setOnClickListener {
            if (clickDebounce()) {
                onItemClick(track)
                val context = holder.itemView.context
                val intent = Intent(context, PlayerActivity::class.java)
                intent.putExtra(PlayerActivity.TRACK_DATA, track)
                context.startActivity(intent)
            }
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setTracks(tracks: List<Track>) {
        data = tracks
        notifyDataSetChanged()
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            handler.postDelayed({ isClickAllowed = true }, CLICK_DEBOUNCE_DELAY)
        }
        return current
    }

    companion object {
        const val CLICK_DEBOUNCE_DELAY = 1000L
    }
}
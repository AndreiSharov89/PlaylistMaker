package com.example.playlistmaker.search.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.playlistmaker.search.domain.Track

class TrackAdapter(
    private val clickListener: TrackClickListener,
    private val onTrackLongClick: ((Track) -> Unit)? = null
) : RecyclerView.Adapter<TrackViewHolder>() {
    var tracks = ArrayList<Track>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder =
        TrackViewHolder.from(parent)

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = tracks[position]
        holder.bind(track)
        holder.itemView.setOnClickListener {
                clickListener.onTrackClick(track)
        }
        onTrackLongClick?.let { longClickListener ->
            holder.itemView.setOnLongClickListener {
                longClickListener(track)
                true
            }
        }
    }

    override fun getItemCount(): Int {
        return tracks.size
    }

    fun interface TrackClickListener {
        fun onTrackClick(track: Track)
    }

    fun setTracks(trackList: List<Track>) {
        tracks = ArrayList(trackList)
        notifyDataSetChanged()
    }
}
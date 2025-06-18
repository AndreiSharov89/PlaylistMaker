package com.example.playlistmaker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

class TrackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    companion object {
        fun create(parent: ViewGroup): TrackViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.track_view, parent, false)
            return TrackViewHolder(view)
        }
    }

    private val trackName: TextView = itemView.findViewById(R.id.tv_TrackName)
    private val artistName: TextView = itemView.findViewById(R.id.tv_ArtistName)
    private val trackTime: TextView = itemView.findViewById(R.id.tv_TrackTime)
    private val trackImage: ImageView = itemView.findViewById(R.id.ivTrackImage)


    fun bind(item: Track) {
        trackName.text = item.trackName
        artistName.text = item.artistName
        trackTime.text = item.trackTime
        Glide.with(itemView)
            .load(item.artworkUrl100)
            .placeholder(R.drawable.track_placeholder_45)
            .centerCrop()
            .transform(RoundedCorners(itemView.resources.getDimensionPixelSize(R.dimen.dp_2)))
            .into(trackImage)
    }
}
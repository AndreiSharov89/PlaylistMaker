package com.example.playlistmaker.search.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.TrackViewBinding
import com.example.playlistmaker.search.domain.Track
import java.text.SimpleDateFormat
import java.util.Locale

class TrackViewHolder(private val binding: TrackViewBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: Track) {
        binding.tvTrackName.text = item.trackName
        binding.tvArtistName.text = item.artistName
        binding.tvTrackTime.text = dateFormat.format(item.trackTimeMillis)
        Glide.with(itemView)
            .load(item.artworkUrl100)
            .placeholder(R.drawable.track_placeholder_45)
            .centerCrop()
            .transform(RoundedCorners(itemView.resources.getDimensionPixelSize(R.dimen.dp_2)))
            .into(binding.ivTrackImage)
    }

    companion object {
        private val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())

        fun from(parent: ViewGroup): TrackViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = TrackViewBinding.inflate(inflater, parent, false)
            return TrackViewHolder(binding)
        }
    }

}
package com.example.playlistmaker.player.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.playlistmaker.R
import com.example.playlistmaker.createplaylist.domain.Playlist
import com.example.playlistmaker.databinding.PlaylistViewSmallBinding


class BottomsheetPlaylistAdapter(
    private val clickListener: (Playlist) -> Unit
) : RecyclerView.Adapter<BottomsheetPlaylistAdapter.PlaylistViewHolder>() {

    var playlists = listOf<Playlist>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = PlaylistViewSmallBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.bind(playlist)
        holder.itemView.setOnClickListener { clickListener(playlist) }
    }

    override fun getItemCount() = playlists.size

    class PlaylistViewHolder(private val binding: PlaylistViewSmallBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            binding.playlistName.text = playlist.name
            binding.trackCount.text = itemView.resources.getQuantityString(
                R.plurals.track_count ?: 0,
                playlist.trackCount,
                playlist.trackCount
            )

            Glide.with(itemView)
                .load(playlist.coverImagePath)
                .placeholder(R.drawable.track_placeholder_45)
                .centerCrop()
                .into(binding.playlistImage)
        }
    }
}
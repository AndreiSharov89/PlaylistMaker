package com.example.playlistmaker

import android.content.Intent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson

class TrackAdapter(
    private var data: List<Track> = listOf(),
    private val onItemClick: (Track) -> Unit
) : RecyclerView.Adapter<TrackViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        return TrackViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        val track = data[position]
        holder.bind(track)
        holder.itemView.setOnClickListener {
            onItemClick(track)
            val context = holder.itemView.context
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra(PlayerActivity.TRACK_DATA, Gson().toJson(track))
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun setTracks(tracks: List<Track>) {
        data = tracks
        notifyDataSetChanged()
    }

}
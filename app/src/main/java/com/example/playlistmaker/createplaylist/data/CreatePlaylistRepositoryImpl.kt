package com.example.playlistmaker.createplaylist.data

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.example.playlistmaker.createplaylist.domain.CreatePlaylistRepository
import com.example.playlistmaker.createplaylist.domain.Playlist
import com.example.playlistmaker.db.PlaylistDao
import com.example.playlistmaker.db.PlaylistDbConverter
import com.example.playlistmaker.db.TrackDbConverter
import com.example.playlistmaker.db.TrackIntPlaylistDao
import com.example.playlistmaker.search.domain.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.FileOutputStream

class CreatePlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val trackInPlaylistDao: TrackIntPlaylistDao,
    private val trackDbConverter: TrackDbConverter,
    private val playlistDbConverter: PlaylistDbConverter,
    private val context: Context
) : CreatePlaylistRepository {

    override suspend fun createPlaylist(playlist: Playlist) {
        playlistDao.insertPlaylist(playlistDbConverter.map(playlist))
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        playlistDao.updatePlaylist(playlistDbConverter.map(playlist))
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { playlists ->
            playlists.map { playlistEntity -> playlistDbConverter.map(playlistEntity) }
        }
    }

    override suspend fun addTrackToPlaylist(track: Track) {
        val trackEntity = trackDbConverter.mapToPlaylistTrack(track)
        trackInPlaylistDao.insertTrackToPlaylist(trackEntity)
    }

    override suspend fun getTrackById(id: String): Track? {
        val trackEntity = trackInPlaylistDao.getTrackById(id)
        return trackEntity?.let { trackDbConverter.mapFromPlaylistTrack(it) }
    }

    override fun getAllTracks(): Flow<List<Track>> {
        return trackInPlaylistDao.getAllTracks().map { trackEntities ->
            trackEntities.map { entity ->
                trackDbConverter.mapFromPlaylistTrack(entity)
            }
        }
    }

    override suspend fun addTrackAndUpdatePlaylist(track: Track, playlist: Playlist) {
        val trackEntity = trackDbConverter.mapToPlaylistTrack(track)
        trackInPlaylistDao.insertTrackToPlaylist(trackEntity)

        val updatedTrackIds = playlist.trackIds.toMutableList()
        updatedTrackIds.add(track.trackId.toString())

        val updatedPlaylist = playlist.copy(
            trackIds = updatedTrackIds,
            trackCount = updatedTrackIds.size
        )
        updatePlaylist(updatedPlaylist)
    }

    override suspend fun saveImage(uri: Uri, fileName: String): Uri {
        val storageDir = File(context.filesDir, "playlist_covers")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }

        val file = File(storageDir, fileName)
        val inputStream = context.contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(file)

        inputStream.use { input ->
            outputStream.use { output ->
                input?.copyTo(output)
            }
        }

        return file.toUri()
    }
}

package com.example.playlistmaker.createplaylist.data

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.example.playlistmaker.createplaylist.domain.CreatePlaylistRepository
import com.example.playlistmaker.createplaylist.domain.Playlist
import com.example.playlistmaker.db.PlaylistDao
import com.example.playlistmaker.db.PlaylistEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.FileOutputStream

class CreatePlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val context: Context
) : CreatePlaylistRepository {

    override suspend fun createPlaylist(playlist: PlaylistEntity) {
        playlistDao.insertPlaylist(playlist)
    }

    override suspend fun updatePlaylist(playlist: PlaylistEntity) {
        playlistDao.updatePlaylist(playlist)
    }

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylists().map { playlists ->
            playlists.map { playlistEntity ->
                Playlist(
                    id = playlistEntity.id,
                    name = playlistEntity.name,
                    description = playlistEntity.description,
                    coverImagePath = playlistEntity.coverImagePath,
                    trackIds = playlistEntity.trackIds,
                    trackCount = playlistEntity.trackCount
                )
            }
        }
    }

    override suspend fun saveImage(uri: Uri, fileName: String): Uri {
        // Use app's private internal storage. No permissions needed.
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

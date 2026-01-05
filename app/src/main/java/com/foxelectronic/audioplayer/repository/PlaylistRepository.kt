package com.foxelectronic.audioplayer.repository

import android.content.Context
import com.foxelectronic.audioplayer.data.dao.PlaylistDao
import com.foxelectronic.audioplayer.data.dao.TrackMetadataDao
import com.foxelectronic.audioplayer.data.model.Playlist
import com.foxelectronic.audioplayer.data.model.PlaylistTrackCrossRef
import com.foxelectronic.audioplayer.data.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PlaylistRepository(
    private val playlistDao: PlaylistDao,
    private val trackMetadataDao: TrackMetadataDao
) {
    val allPlaylists: Flow<List<Playlist>> = playlistDao.getAllPlaylists()

    suspend fun createPlaylist(name: String, coverImagePath: String? = null): Long {
        val playlist = Playlist(
            name = name,
            coverImagePath = coverImagePath
        )
        return playlistDao.insertPlaylist(playlist)
    }

    suspend fun updatePlaylist(playlist: Playlist) {
        playlistDao.updatePlaylist(playlist.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.deletePlaylist(playlist)
    }

    suspend fun deletePlaylistById(playlistId: Long) {
        playlistDao.deletePlaylistById(playlistId)
    }

    suspend fun getPlaylistById(playlistId: Long): Playlist? {
        return playlistDao.getPlaylistById(playlistId)
    }

    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        // Проверяем, не добавлен ли уже трек
        if (playlistDao.isTrackInPlaylist(playlistId, trackId)) {
            return
        }

        // Получаем следующую позицию
        val maxPosition = playlistDao.getMaxPosition(playlistId) ?: -1
        val newPosition = maxPosition + 1

        playlistDao.insertPlaylistTrack(
            PlaylistTrackCrossRef(playlistId, trackId, newPosition)
        )

        // Обновляем время изменения плейлиста
        playlistDao.getPlaylistById(playlistId)?.let { playlist ->
            playlistDao.updatePlaylist(playlist.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        playlistDao.deletePlaylistTrack(playlistId, trackId)

        // Обновляем время изменения плейлиста
        playlistDao.getPlaylistById(playlistId)?.let { playlist ->
            playlistDao.updatePlaylist(playlist.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun clearPlaylist(playlistId: Long) {
        playlistDao.clearPlaylist(playlistId)
    }

    fun getPlaylistTrackIds(playlistId: Long): Flow<List<Long>> {
        return playlistDao.getPlaylistTrackIds(playlistId)
            .map { list -> list.sortedBy { it.position }.map { it.trackId } }
    }

    suspend fun getPlaylistTrackCount(playlistId: Long): Int {
        return playlistDao.getPlaylistTrackCount(playlistId)
    }

    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean {
        return playlistDao.isTrackInPlaylist(playlistId, trackId)
    }

    /**
     * Получить список ID плейлистов, содержащих данный трек
     */
    suspend fun getPlaylistsContainingTrack(trackId: Long): Set<Long> {
        return playlistDao.getPlaylistsContainingTrack(trackId).toSet()
    }

    /**
     * Применить переопределения метаданных к списку треков
     */
    suspend fun applyMetadataOverrides(tracks: List<Track>): List<Track> {
        val overrides = trackMetadataDao.getAllOverridesList()
        if (overrides.isEmpty()) return tracks

        val overrideMap = overrides.associateBy { it.trackId }

        return tracks.map { track ->
            val override = overrideMap[track.id]
            if (override != null) {
                track.copy(
                    title = override.customTitle ?: track.title,
                    artist = override.customArtist ?: track.artist,
                    album = override.customAlbum ?: track.album,
                    albumArtPath = override.customCoverPath ?: track.albumArtPath
                )
            } else {
                track
            }
        }
    }
}

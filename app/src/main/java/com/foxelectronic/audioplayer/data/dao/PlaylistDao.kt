package com.foxelectronic.audioplayer.data.dao

import androidx.room.*
import com.foxelectronic.audioplayer.data.model.Playlist
import com.foxelectronic.audioplayer.data.model.PlaylistTrackCrossRef
import com.foxelectronic.audioplayer.data.model.PlaylistTrackWithPosition
import com.foxelectronic.audioplayer.data.model.PlaylistWithTrackCount
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {

    @Query("SELECT * FROM playlists ORDER BY updatedAt DESC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Query("""
        SELECT p.*, COUNT(pt.trackId) as trackCount
        FROM playlists p
        LEFT JOIN playlist_tracks pt ON p.playlistId = pt.playlistId
        GROUP BY p.playlistId
        ORDER BY p.updatedAt DESC
    """)
    fun getAllPlaylistsWithTrackCount(): Flow<List<PlaylistWithTrackCount>>

    @Query("SELECT * FROM playlists WHERE playlistId = :id")
    suspend fun getPlaylistById(id: Long): Playlist?

    @Insert
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Query("DELETE FROM playlists WHERE playlistId = :playlistId")
    suspend fun deletePlaylistById(playlistId: Long)

    // Получить треки плейлиста с сортировкой по position
    @Query("""
        SELECT trackId, position
        FROM playlist_tracks
        WHERE playlistId = :playlistId
        ORDER BY position ASC
    """)
    fun getPlaylistTrackIds(playlistId: Long): Flow<List<PlaylistTrackWithPosition>>

    @Query("SELECT COUNT(*) FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun getPlaylistTrackCount(playlistId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistTrack(crossRef: PlaylistTrackCrossRef)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun deletePlaylistTrack(playlistId: Long, trackId: Long)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun clearPlaylist(playlistId: Long)

    @Query("SELECT MAX(position) FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun getMaxPosition(playlistId: Long): Int?

    @Query("SELECT EXISTS(SELECT 1 FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId)")
    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean

    @Query("SELECT DISTINCT playlistId FROM playlist_tracks WHERE trackId = :trackId")
    suspend fun getPlaylistsContainingTrack(trackId: Long): List<Long>
}

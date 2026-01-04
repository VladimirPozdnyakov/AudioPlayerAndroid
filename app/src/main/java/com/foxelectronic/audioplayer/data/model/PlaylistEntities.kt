package com.foxelectronic.audioplayer.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Пользовательский плейлист
 */
@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val playlistId: Long = 0,
    val name: String,
    val coverImagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Связь many-to-many между плейлистами и треками
 */
@Entity(
    tableName = "playlist_tracks",
    primaryKeys = ["playlistId", "trackId"],
    foreignKeys = [
        ForeignKey(
            entity = Playlist::class,
            parentColumns = ["playlistId"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("playlistId"), Index("trackId")]
)
data class PlaylistTrackCrossRef(
    val playlistId: Long,
    val trackId: Long,
    val position: Int
)

/**
 * Кастомные метаданные для треков (переопределяют ID3 теги)
 */
@Entity(tableName = "track_metadata_overrides")
data class TrackMetadataOverride(
    @PrimaryKey val trackId: Long,
    val customTitle: String? = null,
    val customArtist: String? = null,
    val customAlbum: String? = null,
    val customCoverPath: String? = null,
    val lastModified: Long = System.currentTimeMillis()
)

/**
 * Вспомогательный класс для получения трека с позицией в плейлисте
 */
data class PlaylistTrackWithPosition(
    val trackId: Long,
    val position: Int
)

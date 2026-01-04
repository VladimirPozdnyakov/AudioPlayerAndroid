package com.foxelectronic.audioplayer

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Delete
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import com.foxelectronic.audioplayer.data.model.Playlist
import com.foxelectronic.audioplayer.data.model.PlaylistTrackCrossRef
import com.foxelectronic.audioplayer.data.model.TrackMetadataOverride
import com.foxelectronic.audioplayer.data.dao.PlaylistDao
import com.foxelectronic.audioplayer.data.dao.TrackMetadataDao

@Entity(tableName = "favorites")
data class FavoriteTrack(
    @PrimaryKey val trackId: Long,
    val uri: String,
    val title: String,
    val artist: String?,
    val album: String? = null,
    val albumArtPath: String?,
    val addedAt: Long = System.currentTimeMillis()
)

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteTrack>>

    @Query("SELECT * FROM favorites WHERE trackId = :trackId")
    suspend fun getFavoriteById(trackId: Long): FavoriteTrack?

    @Insert
    suspend fun insertFavorite(favoriteTrack: FavoriteTrack)

    @Delete
    suspend fun deleteFavorite(favoriteTrack: FavoriteTrack)

    @Query("DELETE FROM favorites WHERE trackId = :trackId")
    suspend fun deleteFavoriteById(trackId: Long)

    @Query("SELECT COUNT(*) FROM favorites WHERE trackId = :trackId")
    suspend fun isFavorite(trackId: Long): Int
}

@Database(
    entities = [
        FavoriteTrack::class,
        Playlist::class,
        PlaylistTrackCrossRef::class,
        TrackMetadataOverride::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters
abstract class FavoriteDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun trackMetadataDao(): TrackMetadataDao

    companion object {
        @Volatile
        private var INSTANCE: FavoriteDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE favorites ADD COLUMN album TEXT")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Таблица плейлистов
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS playlists (
                        playlistId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        coverImagePath TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """)

                // Таблица связей плейлист-трек
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS playlist_tracks (
                        playlistId INTEGER NOT NULL,
                        trackId INTEGER NOT NULL,
                        position INTEGER NOT NULL,
                        PRIMARY KEY(playlistId, trackId),
                        FOREIGN KEY(playlistId) REFERENCES playlists(playlistId) ON DELETE CASCADE
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS index_playlist_tracks_playlistId ON playlist_tracks(playlistId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_playlist_tracks_trackId ON playlist_tracks(trackId)")

                // Таблица переопределений метаданных
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS track_metadata_overrides (
                        trackId INTEGER PRIMARY KEY NOT NULL,
                        customTitle TEXT,
                        customArtist TEXT,
                        customAlbum TEXT,
                        customCoverPath TEXT,
                        lastModified INTEGER NOT NULL
                    )
                """)
            }
        }

        fun getDatabase(context: Context): FavoriteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FavoriteDatabase::class.java,
                    "favorite_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
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
    entities = [FavoriteTrack::class],
    version = 2,
    exportSchema = false
)
@TypeConverters
abstract class FavoriteDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile
        private var INSTANCE: FavoriteDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE favorites ADD COLUMN album TEXT")
            }
        }

        fun getDatabase(context: Context): FavoriteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FavoriteDatabase::class.java,
                    "favorite_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
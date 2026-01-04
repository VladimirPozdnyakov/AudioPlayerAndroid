package com.foxelectronic.audioplayer.data.dao

import androidx.room.*
import com.foxelectronic.audioplayer.data.model.TrackMetadataOverride
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackMetadataDao {

    @Query("SELECT * FROM track_metadata_overrides WHERE trackId = :trackId")
    suspend fun getMetadataOverride(trackId: Long): TrackMetadataOverride?

    @Query("SELECT * FROM track_metadata_overrides")
    fun getAllOverrides(): Flow<List<TrackMetadataOverride>>

    @Query("SELECT * FROM track_metadata_overrides")
    suspend fun getAllOverridesList(): List<TrackMetadataOverride>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateOverride(override: TrackMetadataOverride)

    @Delete
    suspend fun deleteOverride(override: TrackMetadataOverride)

    @Query("DELETE FROM track_metadata_overrides WHERE trackId = :trackId")
    suspend fun deleteOverrideById(trackId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM track_metadata_overrides WHERE trackId = :trackId)")
    suspend fun hasOverride(trackId: Long): Boolean
}

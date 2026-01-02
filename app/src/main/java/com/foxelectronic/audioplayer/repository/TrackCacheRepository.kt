package com.foxelectronic.audioplayer.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.foxelectronic.audioplayer.data.model.Track
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.trackCacheDataStore: DataStore<Preferences> by preferencesDataStore(name = "track_cache")

class TrackCacheRepository(private val context: Context) {
    
    private val trackCacheKey = stringPreferencesKey("cached_tracks")
    
    suspend fun saveTracksToCache(tracks: List<Track>) {
        val json = Json { encodeDefaults = true }
        val serializedTracks = json.encodeToString(tracks.map {
            TrackDto(
                id = it.id,
                uri = it.uri.toString(),  // Convert Uri to string for serialization
                title = it.title,
                artist = it.artist,
                album = it.album,
                albumArtPath = it.albumArtPath
            )
        })
        
        context.trackCacheDataStore.edit { preferences ->
            preferences[trackCacheKey] = serializedTracks
        }
    }
    
    suspend fun getTracksFromCache(): List<Track> {
        return context.trackCacheDataStore.data
            .map { preferences ->
                val json = Json {
                    encodeDefaults = true
                    ignoreUnknownKeys = true  // Handle old cache without album field
                }
                val serializedTracks = preferences[trackCacheKey] ?: return@map emptyList()

                try {
                    val trackDtos = json.decodeFromString<List<TrackDto>>(serializedTracks)
                    trackDtos.map { dto ->
                        Track(
                            id = dto.id,
                            uri = android.net.Uri.parse(dto.uri),  // Convert string back to Uri
                            title = dto.title,
                            artist = dto.artist,
                            album = dto.album,
                            albumArtPath = dto.albumArtPath
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace() // Print the error for debugging
                    // If there's an error (e.g., deserialization), return empty list
                    emptyList()
                }
            }
            .first()
    }
    
    suspend fun clearCache() {
        context.trackCacheDataStore.edit { preferences ->
            preferences.remove(trackCacheKey)
        }
    }
}

// Data transfer object for serialization
@Serializable
data class TrackDto(
    val id: Long,
    val uri: String,  // Store as string to make it serializable
    val title: String,
    val artist: String?,
    val album: String? = null,
    val albumArtPath: String?
)
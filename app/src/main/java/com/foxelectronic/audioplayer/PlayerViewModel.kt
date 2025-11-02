package com.foxelectronic.audioplayer

import android.content.Context
import android.os.Build
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class Track(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String?,
    val albumArtPath: String? = null
)

data class PlayerUiState(
    val tracks: List<Track> = emptyList(),
    val isPlaying: Boolean = false,
    val currentIndex: Int = -1,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isLoading: Boolean = false,
    val repeatMode: Int = androidx.media3.common.Player.REPEAT_MODE_OFF,
    val isShuffleModeEnabled: Boolean = false
)

class PlayerViewModel : ViewModel() {
    private var player: ExoPlayer? = null
    private var settingsRepository: SettingsRepository? = null

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState

    fun loadTracks(context: Context, settingsRepo: SettingsRepository, allowedFolders: List<String> = emptyList()) {
        this.settingsRepository = settingsRepo
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val tracks = queryDeviceAudio(context, allowedFolders)
            _uiState.value = _uiState.value.copy(tracks = tracks, isLoading = false)
            if (player == null) {
                initializePlayer(context)
            }
            preparePlaylist()
            
            // Try to restore the last played track after a short delay to ensure playlist is fully loaded
            kotlinx.coroutines.delay(100) // Small delay to ensure playlist is ready
            restoreLastPlayedTrack() // This is now a suspend function
        }
    }

    private fun initializePlayer(context: Context) {
        // Используем глобальный плеер из Application
        val globalPlayer = AudioPlayerApplication.mediaSession?.player as? ExoPlayer

        if (globalPlayer != null) {
            player = globalPlayer
        } else {
            player = ExoPlayer.Builder(context)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build(),
                    true
                )
                .setHandleAudioBecomingNoisy(true)
                .build()
        }

        player?.apply {
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        val index = this@apply.currentMediaItemIndex
                        val dur = this@apply.duration.coerceAtLeast(0L)
                        _uiState.value = _uiState.value.copy(currentIndex = index, durationMs = dur)
                        
                        // Save the current track ID when transitioning to a new track, but not during restoration
                        if (!isRestoringTrack && index >= 0 && _uiState.value.tracks.getOrNull(index) != null) {
                            val currentTrack = _uiState.value.tracks[index]
                            viewModelScope.launch {
                                settingsRepository?.setLastPlayedTrackId(currentTrack.id.toString())
                            }
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        val dur = this@apply.duration.coerceAtLeast(0L)
                        _uiState.value = _uiState.value.copy(durationMs = dur)
                    }

                    override fun onRepeatModeChanged(repeatMode: Int) {
                        _uiState.value = _uiState.value.copy(repeatMode = repeatMode)
                    }

                    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                        _uiState.value = _uiState.value.copy(isShuffleModeEnabled = shuffleModeEnabled)
                    }
                })
            }


    }



    private var isRestoringTrack = false

    private fun preparePlaylist() {
        val p = player ?: return
        p.clearMediaItems()
        val items = _uiState.value.tracks.map { track ->
            MediaItem.Builder()
                .setUri(track.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artist)
                        .build()
                )
                .build()
        }
        p.setMediaItems(items)
        p.prepare()
        // Ensure that the player does not auto-play after preparing
        p.playWhenReady = false
    }

    fun play(track: Track) {
        val index = _uiState.value.tracks.indexOfFirst { it.id == track.id }
        if (index >= 0) {
            val p = player ?: return
            p.seekTo(index, 0)
            p.playWhenReady = true
            p.play()
            _uiState.value = _uiState.value.copy(isPlaying = true, currentIndex = index)
            
            // Save the current track ID when playing
            viewModelScope.launch {
                settingsRepository?.setLastPlayedTrackId(track.id.toString())
            }
        }
    }

    fun toggle() {
        val p = player ?: return
        if (p.isPlaying) {
            p.pause()
        } else {
            p.play()
        }
    }

    fun pause() {
        val p = player ?: return
        p.pause()
        _uiState.value = _uiState.value.copy(isPlaying = false)
    }

    fun resume() {
        val p = player ?: return
        if (_uiState.value.currentIndex >= 0) {
            p.playWhenReady = true
            p.play()
            _uiState.value = _uiState.value.copy(isPlaying = true)
        }
    }

    fun startPlaybackService(context: Context) {
        val intent = Intent(context, MediaPlaybackService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopPlaybackService(context: Context) {
        val intent = Intent(context, MediaPlaybackService::class.java)
        context.stopService(intent)
    }

    private suspend fun restoreLastPlayedTrack() {
        isRestoringTrack = true
        val lastPlayedId = settingsRepository?.lastPlayedTrackIdFlow?.firstOrNull()
        if (!lastPlayedId.isNullOrEmpty()) {
            val trackId = lastPlayedId.toLongOrNull()
            if (trackId != null) {
                val trackIndex = _uiState.value.tracks.indexOfFirst { it.id == trackId }
                if (trackIndex >= 0) {
                    val p = player ?: return
                    // Only restore if the player is not already playing another track
                    if (_uiState.value.currentIndex == -1 || _uiState.value.currentIndex != trackIndex) {
                        p.seekTo(trackIndex, 0)
                        _uiState.value = _uiState.value.copy(currentIndex = trackIndex)
                    }
                }
            }
        }
        // Reset the flag after a short delay to allow for proper initialization
        kotlinx.coroutines.delay(200)
        isRestoringTrack = false
    }

    fun seekTo(positionMs: Long) {
        val p = player ?: return
        // Сохраняем текущее состояние воспроизведения до seekTo
        val wasPlaying = p.isPlaying
        p.seekTo(positionMs.coerceIn(0L, p.duration.coerceAtLeast(0L)))

        // Если трек не должен был воспроизводиться, останавливаем его после seekTo
        if (!wasPlaying && p.isPlaying) {
            p.pause()
        }

        // Обновляем только позицию, сохраняя текущее состояние воспроизведения
        _uiState.value = _uiState.value.copy(
            positionMs = positionMs,
            isPlaying = wasPlaying  // Используем оригинальное состояние до seekTo
        )
    }

    fun toggleRepeatMode() {
        val p = player ?: return
        val newMode = when (p.repeatMode) {
            androidx.media3.common.Player.REPEAT_MODE_OFF -> androidx.media3.common.Player.REPEAT_MODE_ALL
            androidx.media3.common.Player.REPEAT_MODE_ALL -> androidx.media3.common.Player.REPEAT_MODE_ONE
            else -> androidx.media3.common.Player.REPEAT_MODE_OFF
        }
        p.repeatMode = newMode
        
        // When enabling single track repeat, turn off shuffle mode
        if (newMode == androidx.media3.common.Player.REPEAT_MODE_ONE && p.shuffleModeEnabled) {
            p.shuffleModeEnabled = false
            _uiState.value = _uiState.value.copy(repeatMode = newMode, isShuffleModeEnabled = false)
        } else {
            _uiState.value = _uiState.value.copy(repeatMode = newMode)
        }
    }

    fun toggleShuffleMode() {
        val p = player ?: return
        val newState = !p.shuffleModeEnabled
        p.shuffleModeEnabled = newState
        _uiState.value = _uiState.value.copy(isShuffleModeEnabled = newState)
    }

    fun next() {
        val p = player ?: return
        if (p.hasNextMediaItem()) {
            p.seekToNextMediaItem()
            p.play()
            _uiState.value = _uiState.value.copy(isPlaying = true)
        }
    }

    fun previous() {
        val p = player ?: return
        if (p.hasPreviousMediaItem()) {
            p.seekToPreviousMediaItem()
            p.play()
            _uiState.value = _uiState.value.copy(isPlaying = true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        player?.release()
        player = null
    }

    private fun queryDeviceAudio(context: Context, allowedFolders: List<String>): List<Track> {
        // Если папки не настроены, используем папку Music по умолчанию
        val foldersToScan = if (allowedFolders.isEmpty()) {
            listOf("content://com.android.externalstorage.documents/tree/primary%3AMusic")
        } else {
            allowedFolders
        }

        if (foldersToScan.isEmpty()) return emptyList()
        val tracks = mutableListOf<Track>()
        val collection: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = buildList {
            add(MediaStore.Audio.Media._ID)
            add(MediaStore.Audio.Media.TITLE)
            add(MediaStore.Audio.Media.ARTIST)
            add(MediaStore.Audio.Media.ALBUM_ID) // Add album ID to fetch album art
            if (Build.VERSION.SDK_INT >= 29) {
                add(MediaStore.Audio.Media.RELATIVE_PATH)
            } else {
                @Suppress("DEPRECATION")
                add(MediaStore.Audio.Media.DATA)
            }
        }.toTypedArray()
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"

        val sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC"
        val resolver = context.contentResolver
        val cursor: Cursor? = resolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )
        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIdCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val relPathCol = if (Build.VERSION.SDK_INT >= 29) it.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH) else -1
            @Suppress("DEPRECATION")
            val dataCol = if (Build.VERSION.SDK_INT < 29) it.getColumnIndex(MediaStore.Audio.Media.DATA) else -1
            while (it.moveToNext()) {
                val id = it.getLong(idCol)
                val title = it.getString(titleCol)
                val artist = it.getString(artistCol)
                val contentUri = Uri.withAppendedPath(collection, id.toString())
                // Filter by chosen folders if any
                val passesFilter = run {
                    val chosenRelBases: List<String> = foldersToScan.mapNotNull { folderUriStr ->
                        try {
                            val u = Uri.parse(folderUriStr)
                            val docId = android.provider.DocumentsContract.getTreeDocumentId(u)
                            // e.g. primary:Music/MyFolder -> RELATIVE_PATH starts with Music/MyFolder/
                            val afterColon = docId.substringAfter(":", "")
                            if (afterColon.isNotEmpty()) afterColon.trimEnd('/') + "/" else null
                        } catch (t: Throwable) { null }
                    }
                    if (Build.VERSION.SDK_INT >= 29) {
                        val rel = if (relPathCol >= 0) it.getString(relPathCol) else null
                        rel != null && chosenRelBases.any { base -> rel.startsWith(base, ignoreCase = true) }
                    } else {
                        @Suppress("DEPRECATION")
                        val fullPath = if (dataCol >= 0) it.getString(dataCol) else null
                        if (fullPath == null) false else {
                            // Map primary: to /storage/emulated/0/
                            val basesFs: List<String> = foldersToScan.mapNotNull { folderUriStr ->
                                try {
                                    val u = Uri.parse(folderUriStr)
                                    val docId = android.provider.DocumentsContract.getTreeDocumentId(u)
                                    val afterColon = docId.substringAfter(":", "")
                                    if (docId.startsWith("primary:")) {
                                        "/storage/emulated/0/" + afterColon.trimEnd('/') + "/"
                                    } else null
                                } catch (t: Throwable) { null }
                            }
                            basesFs.any { base -> fullPath.startsWith(base, ignoreCase = true) }
                        }
                    }
                }
                if (passesFilter) {
                    // Get album art path if available
                    val albumId = it.getLong(albumIdCol)
                    val albumArtUri = getAlbumArtUri(albumId)

                    tracks += Track(
                        id = id,
                        uri = contentUri,
                        title = title,
                        artist = artist,
                        albumArtPath = albumArtUri?.toString()
                    )
                }
            }
        }
        return tracks
    }

    private fun getAlbumArtUri(albumId: Long): Uri? {
        return try {
            val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
            Uri.withAppendedPath(sArtworkUri, albumId.toString())
        } catch (e: Exception) {
            null
        }
    }

    init {
        // Периодическое обновление позиции
        viewModelScope.launch {
            while (true) {
                val p = player
                if (p != null) {
                    val pos = p.currentPosition.coerceAtLeast(0L)
                    val dur = p.duration.coerceAtLeast(0L)
                    _uiState.value = _uiState.value.copy(positionMs = pos, durationMs = dur)
                }
                kotlinx.coroutines.delay(500)
            }
        }
    }

    companion object {
        @JvmStatic
        fun createExoPlayerInstance(context: android.content.Context): androidx.media3.exoplayer.ExoPlayer {
            return androidx.media3.exoplayer.ExoPlayer.Builder(context)
                .setAudioAttributes(
                    androidx.media3.common.AudioAttributes.Builder()
                        .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                        .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build(),
                    true
                )
                .setHandleAudioBecomingNoisy(true)
                .build()
        }
    }
}

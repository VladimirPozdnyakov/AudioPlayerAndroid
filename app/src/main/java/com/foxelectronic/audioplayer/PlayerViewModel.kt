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
import com.foxelectronic.audioplayer.data.model.Track
import com.foxelectronic.audioplayer.repository.TrackCacheRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine

data class PlayerUiState(
    val allTracks: List<Track> = emptyList(),  // Все треки (для отображения)
    val tracks: List<Track> = emptyList(),      // Текущий плейлист плеера
    val isPlaying: Boolean = false,
    val currentIndex: Int = -1,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val isLoading: Boolean = false,
    val repeatMode: Int = androidx.media3.common.Player.REPEAT_MODE_OFF,
    val isShuffleModeEnabled: Boolean = false,
    val sortMode: SortMode = SortMode.ALPHABETICAL_AZ,
    val playlistName: String = "Все треки",
    val artistGroups: Map<String, List<Track>> = emptyMap(),
    val albumGroups: Map<String, List<Track>> = emptyMap(),
    val selectedArtist: String? = null,
    val selectedAlbum: String? = null
)

enum class SortMode {
    ALPHABETICAL_AZ,    // A-Z
    ALPHABETICAL_ZA     // Z-A
}

class PlayerViewModel : ViewModel() {
    private var player: ExoPlayer? = null
    private var settingsRepository: SettingsRepository? = null
    private var favoriteDao: FavoriteDao? = null

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState

    fun loadTracks(context: Context, settingsRepo: SettingsRepository, allowedFolders: List<String> = emptyList()) {
        this.settingsRepository = settingsRepo
        this.favoriteDao = FavoriteDatabase.getDatabase(context).favoriteDao()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Store the last played track ID before updating anything
            val lastPlayedTrackId = settingsRepository?.lastPlayedTrackIdFlow?.firstOrNull()?.toLongOrNull()
            
            // Store current playback state to restore later
            val wasPlaying = _uiState.value.isPlaying
            
            // Get cached tracks to show immediately (for better UX)
            val trackCacheRepository = TrackCacheRepository(context)
            var tracks = trackCacheRepository.getTracksFromCache()
            
            // If we have cached tracks, show them immediately while scanning in background
            if (tracks.isNotEmpty()) {
                val tracksWithFavorites = updateFavoritesInTracks(tracks)
                val sortedTracks = applySorting(tracksWithFavorites, _uiState.value.sortMode)
                _uiState.value = _uiState.value.copy(allTracks = sortedTracks, tracks = sortedTracks, isLoading = true)

                if (player == null) {
                    initializePlayer(context)
                }
                // Don't prepare playlist here - will be done later if we have a track to restore
            }
            
            // Always scan for new tracks in the background to ensure we have all available tracks for search
            val freshTracks = queryDeviceAudio(context, allowedFolders)
            
            // Update cache with fresh results
            trackCacheRepository.saveTracksToCache(freshTracks)
            
            if (player == null) {
                initializePlayer(context)
            }
            
            // Update the UI with the fresh track list with favorite information
            val tracksWithFavorites = updateFavoritesInTracks(freshTracks)
            val sortedFreshTracks = applySorting(tracksWithFavorites, _uiState.value.sortMode)

            // Update UI state first
            _uiState.value = _uiState.value.copy(allTracks = sortedFreshTracks, tracks = sortedFreshTracks, isLoading = false)
            updateGroups()

            // Restore the last played track if it exists in the new track list
            if (lastPlayedTrackId != null) {
                // Prepare the playlist only if we have a track to restore
                preparePlaylist()

                val trackIndex = sortedFreshTracks.indexOfFirst { it.id == lastPlayedTrackId }
                if (trackIndex >= 0) {
                    // Get the saved position before restoring
                    val lastPlayedPosition = settingsRepository?.lastPlayedPositionFlow?.firstOrNull() ?: 0L
                    
                    // Add a small delay to ensure the player is properly initialized
                    kotlinx.coroutines.delay(100)
                    val p = player ?: return@launch
                    p.seekTo(trackIndex, lastPlayedPosition)
                    _uiState.value = _uiState.value.copy(currentIndex = trackIndex)
                    
                    // Resume playback if it was playing before
                    if (wasPlaying) {
                        p.play()
                        _uiState.value = _uiState.value.copy(isPlaying = true)
                    } else {
                        p.pause()
                        _uiState.value = _uiState.value.copy(isPlaying = false)
                    }
                }
            }
        }
    }

    private fun initializePlayer(context: Context) {
        // Используем глобальный плеер из Application
        val globalPlayer = AudioPlayerApplication.globalMediaSession?.player as? ExoPlayer

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

                        // Ignore automatic transition to first track when playlist is prepared but not playing
                        // This prevents auto-selection of first track on app start
                        val isAutoPrepare = _uiState.value.currentIndex == -1 && index == 0 && !this@apply.isPlaying
                        if (!isAutoPrepare) {
                            _uiState.value = _uiState.value.copy(currentIndex = index, durationMs = dur)

                            // Save the current track ID and position when transitioning to a new track, but not during restoration
                            if (!isRestoringTrack && index >= 0 && _uiState.value.tracks.getOrNull(index) != null) {
                                val currentTrack = _uiState.value.tracks[index]
                                val currentPosition = this@apply.currentPosition.coerceAtLeast(0L)
                                viewModelScope.launch {
                                    settingsRepository?.setLastPlayedTrackId(currentTrack.id.toString())
                                    settingsRepository?.setLastPlayedPosition(currentPosition)
                                }
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

            // Всегда обновляем плейлист, чтобы он соответствовал текущей сортировке
            preparePlaylist()

            p.seekTo(index, 0)
            p.playWhenReady = true
            p.play()
            _uiState.value = _uiState.value.copy(isPlaying = true, currentIndex = index)

            // Save the current track ID and position when playing
            viewModelScope.launch {
                settingsRepository?.setLastPlayedTrackId(track.id.toString())
                settingsRepository?.setLastPlayedPosition(0L) // Starting from beginning
            }
        }
    }

    fun playFromPlaylist(track: Track, playlist: List<Track>, playlistName: String = "Все треки") {
        val p = player ?: return

        // Сохраняем текущий трек
        val currentTrackId = if (_uiState.value.currentIndex >= 0 && _uiState.value.tracks.isNotEmpty()) {
            _uiState.value.tracks[_uiState.value.currentIndex].id
        } else null

        // Очищаем плеер и добавляем треки из плейлиста
        p.clearMediaItems()
        val items = playlist.map { t ->
            MediaItem.Builder()
                .setUri(t.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(t.title)
                        .setArtist(t.artist)
                        .build()
                )
                .build()
        }
        p.setMediaItems(items)
        p.prepare()

        // Находим индекс трека в плейлисте
        val index = playlist.indexOfFirst { it.id == track.id }
        if (index >= 0) {
            p.seekTo(index, 0)
            p.playWhenReady = true
            p.play()

            // Обновляем состояние с новым плейлистом
            _uiState.value = _uiState.value.copy(
                tracks = playlist,
                isPlaying = true,
                currentIndex = index,
                playlistName = playlistName
            )

            viewModelScope.launch {
                settingsRepository?.setLastPlayedTrackId(track.id.toString())
                settingsRepository?.setLastPlayedPosition(0L)
            }
        }
    }

    fun restoreFullPlaylist() {
        val p = player ?: return
        val fullTracks = _uiState.value.allTracks

        // Сохраняем текущий трек и позицию
        val currentTrackId = if (_uiState.value.currentIndex >= 0 && _uiState.value.tracks.isNotEmpty()) {
            _uiState.value.tracks[_uiState.value.currentIndex].id
        } else null
        val currentPosition = p.currentPosition
        val wasPlaying = p.isPlaying

        // Восстанавливаем полный плейлист
        p.clearMediaItems()
        val items = fullTracks.map { t ->
            MediaItem.Builder()
                .setUri(t.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(t.title)
                        .setArtist(t.artist)
                        .build()
                )
                .build()
        }
        p.setMediaItems(items)
        p.prepare()

        // Находим индекс текущего трека в полном плейлисте
        val newIndex = currentTrackId?.let { id ->
            fullTracks.indexOfFirst { it.id == id }
        } ?: -1

        if (newIndex >= 0) {
            p.seekTo(newIndex, currentPosition)
            if (wasPlaying) {
                p.play()
            }
        }

        _uiState.value = _uiState.value.copy(
            tracks = fullTracks,
            currentIndex = newIndex
        )
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
        val currentPosition = p.currentPosition.coerceAtLeast(0L)
        p.pause()
        _uiState.value = _uiState.value.copy(isPlaying = false)
        
        // Save the current position when pausing
        viewModelScope.launch {
            settingsRepository?.setLastPlayedPosition(currentPosition)
        }
    }

    fun resume() {
        val p = player ?: return
        if (_uiState.value.currentIndex >= 0) {
            p.playWhenReady = true
            p.play()
            _uiState.value = _uiState.value.copy(isPlaying = true)
        }
    }

    fun stopPlayback() {
        val p = player ?: return
        p.pause()
        p.stop()
        _uiState.value = _uiState.value.copy(
            isPlaying = false,
            currentIndex = -1,
            positionMs = 0L,
            durationMs = 0L
        )

        // Очищаем сохранённый последний воспроизводимый трек
        viewModelScope.launch {
            settingsRepository?.setLastPlayedTrackId("")
            settingsRepository?.setLastPlayedPosition(0L)
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
        val lastPlayedPosition = settingsRepository?.lastPlayedPositionFlow?.firstOrNull() ?: 0L
        if (!lastPlayedId.isNullOrEmpty()) {
            val trackId = lastPlayedId.toLongOrNull()
            if (trackId != null) {
                val trackIndex = _uiState.value.tracks.indexOfFirst { it.id == trackId }
                if (trackIndex >= 0) {
                    val p = player ?: return
                    // Only restore if the player is not already playing another track
                    if (_uiState.value.currentIndex == -1 || _uiState.value.currentIndex != trackIndex) {
                        p.seekTo(trackIndex, lastPlayedPosition)
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
        
        // Save this position to settings as well
        viewModelScope.launch {
            settingsRepository?.setLastPlayedPosition(positionMs)
        }
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
        val currentIndex = _uiState.value.currentIndex
        val tracks = _uiState.value.tracks

        if (currentIndex >= 0 && currentIndex < tracks.size - 1) {
            val nextTrack = tracks[currentIndex + 1]
            play(nextTrack)
        } else if (currentIndex == tracks.size - 1 && _uiState.value.repeatMode == Player.REPEAT_MODE_ALL) {
            // Если последний трек и включен повтор всех - переходим к первому
            val firstTrack = tracks.firstOrNull()
            if (firstTrack != null) {
                play(firstTrack)
            }
        }
    }

    fun previous() {
        val currentIndex = _uiState.value.currentIndex
        val tracks = _uiState.value.tracks

        if (currentIndex > 0 && tracks.isNotEmpty()) {
            val previousTrack = tracks[currentIndex - 1]
            play(previousTrack)
        } else if (currentIndex == 0 && _uiState.value.repeatMode == Player.REPEAT_MODE_ALL) {
            // Если первый трек и включен повтор всех - переходим к последнему
            val lastTrack = tracks.lastOrNull()
            if (lastTrack != null) {
                play(lastTrack)
            }
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
            add(MediaStore.Audio.Media.ALBUM)
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
            val albumCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val relPathCol = if (Build.VERSION.SDK_INT >= 29) it.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH) else -1
            @Suppress("DEPRECATION")
            val dataCol = if (Build.VERSION.SDK_INT < 29) it.getColumnIndex(MediaStore.Audio.Media.DATA) else -1
            while (it.moveToNext()) {
                val id = it.getLong(idCol)
                val title = it.getString(titleCol)
                val artist = it.getString(artistCol)?.takeIf { a -> a != "<unknown>" && a.isNotBlank() }
                val album = it.getString(albumCol)?.takeIf { a -> a != "<unknown>" && a.isNotBlank() }
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
                        album = album,
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

    private var lastSavedPositionTime = 0L

    init {
        // Периодическое обновление позиции
        viewModelScope.launch {
            while (true) {
                val p = player
                if (p != null) {
                    val pos = p.currentPosition.coerceAtLeast(0L)
                    val dur = p.duration.coerceAtLeast(0L)
                    _uiState.value = _uiState.value.copy(positionMs = pos, durationMs = dur)

                    // Сохраняем позицию не чаще раза в 5 секунд, чтобы снизить нагрузку на DataStore
                    val currentTime = System.currentTimeMillis()
                    if (_uiState.value.currentIndex >= 0 &&
                        _uiState.value.tracks.getOrNull(_uiState.value.currentIndex) != null &&
                        currentTime - lastSavedPositionTime > 5000) {
                        lastSavedPositionTime = currentTime
                        settingsRepository?.setLastPlayedPosition(pos)
                    }
                }
                kotlinx.coroutines.delay(500)
            }
        }
    }

    fun toggleSortMode() {
        val currentTrackId = if (_uiState.value.currentIndex >= 0 && _uiState.value.tracks.isNotEmpty()) {
            _uiState.value.tracks[_uiState.value.currentIndex].id
        } else null

        val newSortMode = when (_uiState.value.sortMode) {
            SortMode.ALPHABETICAL_AZ -> SortMode.ALPHABETICAL_ZA
            SortMode.ALPHABETICAL_ZA -> SortMode.ALPHABETICAL_AZ
        }

        // Update the UI state with the new sort mode and sorted tracks
        val sortedAllTracks = applySorting(_uiState.value.allTracks, newSortMode)
        val sortedTracks = applySorting(_uiState.value.tracks, newSortMode)
        val newCurrentIndex = currentTrackId?.let { id ->
            sortedTracks.indexOfFirst { it.id == id }
        } ?: -1

        _uiState.value = _uiState.value.copy(
            sortMode = newSortMode,
            allTracks = sortedAllTracks,
            tracks = sortedTracks,
            currentIndex = newCurrentIndex
        )

        // НЕ обновляем плейлист в ExoPlayer - это вызывает паузу
        // Плейлист обновится автоматически при следующем выборе трека пользователем
    }

    private suspend fun updateFavoritesInTracks(tracks: List<Track>): List<Track> {
        val favoriteTrackIds = favoriteDao?.getAllFavorites()?.first()?.map { it.trackId } ?: emptyList()
        return tracks.map { track ->
            track.copy(isFavorite = track.id in favoriteTrackIds)
        }
    }

    private fun applySorting(tracks: List<Track>, sortMode: SortMode): List<Track> {
        return when (sortMode) {
            SortMode.ALPHABETICAL_AZ -> tracks.sortedBy { it.title.lowercase() }
            SortMode.ALPHABETICAL_ZA -> tracks.sortedByDescending { it.title.lowercase() }
        }
    }

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            val favoriteTrack = FavoriteTrack(
                trackId = track.id,
                uri = track.uri.toString(),
                title = track.title,
                artist = track.artist ?: "",
                album = track.album ?: "",
                albumArtPath = track.albumArtPath
            )
            
            if (track.isFavorite) {
                // Remove from favorites
                favoriteDao?.deleteFavoriteById(track.id)
            } else {
                // Add to favorites
                favoriteDao?.insertFavorite(favoriteTrack)
            }
            
            // Update both track lists with the new favorite status
            val updatedAllTracks = _uiState.value.allTracks.map { t ->
                if (t.id == track.id) {
                    t.copy(isFavorite = !t.isFavorite)
                } else {
                    t
                }
            }
            val updatedTracks = _uiState.value.tracks.map { t ->
                if (t.id == track.id) {
                    t.copy(isFavorite = !t.isFavorite)
                } else {
                    t
                }
            }
            _uiState.value = _uiState.value.copy(allTracks = updatedAllTracks, tracks = updatedTracks)
            updateGroups()
        }
    }

    private fun groupTracksByArtist(tracks: List<Track>): Map<String, List<Track>> {
        return tracks.groupBy { it.artist ?: "Неизвестный исполнитель" }
    }

    private fun groupTracksByAlbum(tracks: List<Track>): Map<String, List<Track>> {
        return tracks.groupBy { it.album ?: "Неизвестный альбом" }
    }

    private fun updateGroups() {
        _uiState.value = _uiState.value.copy(
            artistGroups = groupTracksByArtist(_uiState.value.allTracks),
            albumGroups = groupTracksByAlbum(_uiState.value.allTracks)
        )
    }

    fun selectArtist(artist: String) {
        _uiState.value = _uiState.value.copy(selectedArtist = artist)
    }

    fun clearSelectedArtist() {
        _uiState.value = _uiState.value.copy(selectedArtist = null)
    }

    fun selectAlbum(album: String) {
        _uiState.value = _uiState.value.copy(selectedAlbum = album)
    }

    fun clearSelectedAlbum() {
        _uiState.value = _uiState.value.copy(selectedAlbum = null)
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
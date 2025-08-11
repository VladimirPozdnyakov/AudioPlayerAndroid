package com.example.audioplayer

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
import androidx.media3.ui.PlayerNotificationManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.app.PendingIntent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class Track(
    val id: Long,
    val uri: Uri,
    val title: String,
    val artist: String?
)

data class PlayerUiState(
    val tracks: List<Track> = emptyList(),
    val isPlaying: Boolean = false,
    val currentIndex: Int = -1,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L
)

class PlayerViewModel : ViewModel() {
    private var player: ExoPlayer? = null
    private var notificationManager: PlayerNotificationManager? = null

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState

    fun loadTracks(context: Context, allowedFolders: List<String> = emptyList()) {
        viewModelScope.launch {
            val tracks = queryDeviceAudio(context, allowedFolders)
            _uiState.value = _uiState.value.copy(tracks = tracks)
            if (player == null) {
                initializePlayer(context)
            }
            preparePlaylist()
        }
    }

    private fun initializePlayer(context: Context) {
        player = ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build().apply {
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        val index = this@apply.currentMediaItemIndex
                        val dur = this@apply.duration.coerceAtLeast(0L)
                        _uiState.value = _uiState.value.copy(currentIndex = index, durationMs = dur)
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        val dur = this@apply.duration.coerceAtLeast(0L)
                        _uiState.value = _uiState.value.copy(durationMs = dur)
                    }
                })
            }
        
        createNotificationManager(context)
    }

    private fun createNotificationManager(context: Context) {
        val p = player ?: return
        
        // Создаем канал уведомлений
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel("playback", "Music Playback", NotificationManager.IMPORTANCE_LOW)
            channel.description = "Media playback controls"
            nm.createNotificationChannel(channel)
        }

        notificationManager = PlayerNotificationManager.Builder(context, 1, "playback")
            .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): CharSequence {
                    val index = player.currentMediaItemIndex
                    val track = _uiState.value.tracks.getOrNull(index)
                    return track?.title ?: "Unknown"
                }

                override fun createCurrentContentIntent(player: Player): PendingIntent? {
                    val intent = Intent(context, MainActivity::class.java)
                    return PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                }

                override fun getCurrentContentText(player: Player): CharSequence? {
                    val index = player.currentMediaItemIndex
                    val track = _uiState.value.tracks.getOrNull(index)
                    return track?.artist ?: "Unknown Artist"
                }

                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ): android.graphics.Bitmap? = null
            })
            .setSmallIconResourceId(android.R.drawable.ic_media_play)
            .build().apply {
                setPlayer(p)
                setUseNextAction(true)
                setUsePreviousAction(true)
                setUsePlayPauseActions(true)
            }
    }

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
    }

    fun play(track: Track) {
        val index = _uiState.value.tracks.indexOfFirst { it.id == track.id }
        if (index >= 0) {
            val p = player ?: return
            p.seekTo(index, 0)
            p.playWhenReady = true
            p.play()
            _uiState.value = _uiState.value.copy(isPlaying = true, currentIndex = index)
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

    fun seekTo(positionMs: Long) {
        val p = player ?: return
        p.seekTo(positionMs.coerceIn(0L, p.duration.coerceAtLeast(0L)))
        _uiState.value = _uiState.value.copy(positionMs = positionMs)
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
        notificationManager?.setPlayer(null)
        player?.release()
        player = null
        notificationManager = null
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
                    tracks += Track(
                        id = id,
                        uri = contentUri,
                        title = title,
                        artist = artist
                    )
                }
            }
        }
        return tracks
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
}

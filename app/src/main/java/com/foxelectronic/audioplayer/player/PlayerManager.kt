package com.foxelectronic.audioplayer.player

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.foxelectronic.audioplayer.data.model.Track
import com.foxelectronic.audioplayer.data.repository.PlaybackStateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

data class PlayerState(
    val isPlaying: Boolean = false,
    val currentIndex: Int = -1,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val isShuffleModeEnabled: Boolean = false
)

@Singleton
class PlayerManager @Inject constructor(
    val player: ExoPlayer,
    private val playbackStateRepository: PlaybackStateRepository
) {
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _isRestoringTrack = AtomicBoolean(false)
    val isRestoringTrack: Boolean get() = _isRestoringTrack.get()

    private var currentTracks: List<Track> = emptyList()

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val index = player.currentMediaItemIndex
                _playerState.value = _playerState.value.copy(currentIndex = index)

                if (!_isRestoringTrack.get() && index >= 0 && currentTracks.getOrNull(index) != null) {
                    val currentTrack = currentTracks[index]
                    playbackStateRepository.saveTrackId(currentTrack.id.toString())
                }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                _playerState.value = _playerState.value.copy(repeatMode = repeatMode)
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _playerState.value = _playerState.value.copy(isShuffleModeEnabled = shuffleModeEnabled)
            }
        })
    }

    fun setPlaylist(tracks: List<Track>) {
        currentTracks = tracks
        player.clearMediaItems()
        val items = tracks.map { track ->
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
        player.setMediaItems(items)
        player.prepare()
        player.playWhenReady = false
    }

    fun play(trackIndex: Int) {
        if (trackIndex >= 0 && trackIndex < currentTracks.size) {
            player.seekTo(trackIndex, 0)
            player.playWhenReady = true
            player.play()

            val track = currentTracks[trackIndex]
            playbackStateRepository.saveTrackId(track.id.toString())
            playbackStateRepository.savePosition(0L, immediate = true)
        }
    }

    fun toggle() {
        if (player.isPlaying) {
            pause()
        } else {
            resume()
        }
    }

    fun pause() {
        val currentPosition = player.currentPosition.coerceAtLeast(0L)
        player.pause()
        playbackStateRepository.savePosition(currentPosition, immediate = true)
    }

    fun resume() {
        if (_playerState.value.currentIndex >= 0) {
            player.playWhenReady = true
            player.play()
        }
    }

    fun seekTo(positionMs: Long) {
        val wasPlaying = player.isPlaying
        player.seekTo(positionMs.coerceIn(0L, player.duration.coerceAtLeast(0L)))

        if (!wasPlaying && player.isPlaying) {
            player.pause()
        }

        playbackStateRepository.savePosition(positionMs, immediate = true)
    }

    fun next() {
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
            player.play()
        }
    }

    fun previous() {
        if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
            player.play()
        }
    }

    fun toggleRepeatMode() {
        val newMode = when (player.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        player.repeatMode = newMode

        if (newMode == Player.REPEAT_MODE_ONE && player.shuffleModeEnabled) {
            player.shuffleModeEnabled = false
        }
    }

    fun toggleShuffleMode() {
        player.shuffleModeEnabled = !player.shuffleModeEnabled
    }

    suspend fun restorePlayback(trackId: Long, position: Long) {
        _isRestoringTrack.set(true)
        try {
            val trackIndex = currentTracks.indexOfFirst { it.id == trackId }
            if (trackIndex >= 0) {
                player.seekTo(trackIndex, position)
                _playerState.value = _playerState.value.copy(currentIndex = trackIndex)
            }
        } finally {
            kotlinx.coroutines.delay(200)
            _isRestoringTrack.set(false)
        }
    }

    fun getCurrentTrack(): Track? {
        val index = _playerState.value.currentIndex
        return if (index >= 0) currentTracks.getOrNull(index) else null
    }

    fun getTracks(): List<Track> = currentTracks
}

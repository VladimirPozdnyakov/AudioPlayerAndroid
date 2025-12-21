package com.foxelectronic.audioplayer.player

import androidx.media3.common.Player
import com.foxelectronic.audioplayer.data.repository.PlaybackStateRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class PlaybackPosition(
    val positionMs: Long = 0L,
    val durationMs: Long = 0L
)

@Singleton
class PlaybackPositionTracker @Inject constructor(
    private val playbackStateRepository: PlaybackStateRepository
) {
    private var trackingJob: Job? = null
    private val _position = MutableStateFlow(PlaybackPosition())
    val position: StateFlow<PlaybackPosition> = _position.asStateFlow()

    fun startTracking(player: Player, scope: CoroutineScope) {
        stopTracking()
        playbackStateRepository.startPeriodicSave(scope)

        trackingJob = scope.launch {
            while (isActive) {
                val pos = player.currentPosition.coerceAtLeast(0L)
                val dur = player.duration.coerceAtLeast(0L)

                _position.value = PlaybackPosition(pos, dur)

                // Периодическое сохранение с debounce через репозиторий
                playbackStateRepository.savePosition(pos, immediate = false)

                delay(500)
            }
        }
    }

    fun stopTracking() {
        trackingJob?.cancel()
        trackingJob = null
        playbackStateRepository.stopPeriodicSave()
    }

    fun updatePosition(positionMs: Long, durationMs: Long) {
        _position.value = PlaybackPosition(positionMs, durationMs)
    }
}

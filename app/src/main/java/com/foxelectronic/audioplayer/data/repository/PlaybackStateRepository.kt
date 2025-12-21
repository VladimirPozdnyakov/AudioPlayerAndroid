package com.foxelectronic.audioplayer.data.repository

import com.foxelectronic.audioplayer.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(FlowPreview::class)
@Singleton
class PlaybackStateRepository @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    private val _pendingPosition = MutableStateFlow<Long?>(null)
    private var saveJob: Job? = null
    private var lastSavedPosition = 0L
    private val saveThresholdMs = 5000L

    val lastPlayedTrackIdFlow: Flow<String?> = settingsRepository.lastPlayedTrackIdFlow
    val lastPlayedPositionFlow: Flow<Long> = settingsRepository.lastPlayedPositionFlow

    fun savePosition(position: Long, immediate: Boolean = false) {
        if (immediate) {
            CoroutineScope(Dispatchers.IO).launch {
                settingsRepository.setLastPlayedPosition(position)
                lastSavedPosition = position
            }
        } else {
            if (kotlin.math.abs(position - lastSavedPosition) > saveThresholdMs) {
                _pendingPosition.value = position
            }
        }
    }

    fun saveTrackId(trackId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            settingsRepository.setLastPlayedTrackId(trackId)
        }
    }

    fun startPeriodicSave(scope: CoroutineScope) {
        saveJob?.cancel()
        saveJob = scope.launch {
            _pendingPosition
                .debounce(5000)
                .filterNotNull()
                .collect { position ->
                    settingsRepository.setLastPlayedPosition(position)
                    lastSavedPosition = position
                }
        }
    }

    fun stopPeriodicSave() {
        saveJob?.cancel()
        saveJob = null
    }
}

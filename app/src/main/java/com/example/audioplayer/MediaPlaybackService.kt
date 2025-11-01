package com.foxelectronic.audioplayer

import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class MediaPlaybackService : MediaSessionService() {
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        // Используем глобальную сессию из приложения
        return AudioPlayerApplication.mediaSession
    }
}

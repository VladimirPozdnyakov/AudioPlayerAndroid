package com.foxelectronic.audioplayer

import android.app.Application
import androidx.media3.session.MediaSession

class AudioPlayerApplication : Application() {
    companion object {
        @Volatile
        var mediaSession: MediaSession? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()

        // Создаём ExoPlayer и MediaSession при запуске приложения
        val player = PlayerViewModel.createExoPlayerInstance(this)
        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(
                android.app.PendingIntent.getActivity(
                    this,
                    0,
                    android.content.Intent(this, MainActivity::class.java).apply {
                        flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
                    },
                    android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .build()
    }

    override fun onTerminate() {
        mediaSession?.run {
            if (player.isPlaying) {
                player.pause()
            }
            release()
        }
        mediaSession = null
        super.onTerminate()
    }
}

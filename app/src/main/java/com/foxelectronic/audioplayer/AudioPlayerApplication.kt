package com.foxelectronic.audioplayer

import android.app.Application
import androidx.media3.session.MediaSession
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache

class AudioPlayerApplication : Application(), ImageLoaderFactory {
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

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // Use 25% of available memory for the cache
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02) // Use 2% of available disk space for disk cache
                    .build()
            }
            .respectCacheHeaders(false) // Don't use HTTP cache headers to determine cache
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

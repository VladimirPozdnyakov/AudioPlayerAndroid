package com.foxelectronic.audioplayer

import android.app.Application
import androidx.media3.session.MediaSession
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AudioPlayerApplication : Application(), ImageLoaderFactory {

    @Inject
    lateinit var mediaSession: MediaSession

    companion object {
        @Volatile
        var globalMediaSession: MediaSession? = null
            private set
    }

    override fun onCreate() {
        super.onCreate()
        // MediaSession теперь инжектится через Hilt
        globalMediaSession = mediaSession
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
        globalMediaSession?.run {
            if (player.isPlaying) {
                player.pause()
            }
            release()
        }
        globalMediaSession = null
        super.onTerminate()
    }
}

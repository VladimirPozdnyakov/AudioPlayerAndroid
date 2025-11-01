package com.foxelectronic.audioplayer

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.media3.common.Player
import androidx.media3.session.MediaSession

class MediaSessionHelper {
    companion object {
        @Volatile
        private var mediaSession: MediaSession? = null
        private var serviceStarted = false

        fun setMediaSession(session: MediaSession) {
            mediaSession = session
        }

        fun getMediaSession(): MediaSession? = mediaSession

        fun startService(context: Context) {
            if (!serviceStarted) {
                val intent = Intent(context, MediaPlaybackService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                serviceStarted = true
            }
        }

        fun stopService(context: Context) {
            if (serviceStarted) {
                val intent = Intent(context, MediaPlaybackService::class.java)
                context.stopService(intent)
                serviceStarted = false
            }
        }
    }
}

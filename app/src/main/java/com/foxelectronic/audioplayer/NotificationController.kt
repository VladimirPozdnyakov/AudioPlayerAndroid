package com.foxelectronic.audioplayer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerNotificationManager

object NotificationController {
    private var playerNotificationManager: PlayerNotificationManager? = null
    private var mediaSession: MediaSession? = null

    fun initNotification(context: Context, session: MediaSession) {
        mediaSession = session

        // Создаём PlayerNotificationManager, связанный с MediaSession
        playerNotificationManager = PlayerNotificationManager
            .Builder(context, 1, "playback")
            .setMediaDescriptionAdapter(CustomDescriptionAdapter(context))
            .build()
            .apply {
                setPlayer(session.player)
                setUseNextAction(true)
                setUsePreviousAction(true)
                setUsePlayPauseActions(true)
                setUseStopAction(true)
            }

        // Создаём канал уведомлений
        createNotificationChannel(context)
    }

    fun setPlayer(player: Player) {
        playerNotificationManager?.setPlayer(player)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                "playback",
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Media playback controls"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private class CustomDescriptionAdapter(private val context: Context) : PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): CharSequence {
            // Здесь нужно возвращать реальную информацию о треке
            return "Аудио Плеер"
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            return PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        override fun getCurrentContentText(player: Player): CharSequence? {
            return "Воспроизводится музыка"
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ) = null
    }

    fun release() {
        playerNotificationManager?.setPlayer(null)
        playerNotificationManager = null
        mediaSession = null
    }
}

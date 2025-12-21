package com.foxelectronic.audioplayer.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.foxelectronic.audioplayer.MainActivity
import com.foxelectronic.audioplayer.data.repository.PlaybackStateRepository
import com.foxelectronic.audioplayer.player.PlaybackPositionTracker
import com.foxelectronic.audioplayer.player.PlayerManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {

    @Provides
    @Singleton
    fun provideExoPlayer(@ApplicationContext context: Context): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideMediaSession(
        @ApplicationContext context: Context,
        player: ExoPlayer
    ): MediaSession {
        return MediaSession.Builder(context, player)
            .setSessionActivity(
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    },
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .build()
    }

    @Provides
    @Singleton
    fun providePlayerManager(
        player: ExoPlayer,
        playbackStateRepository: PlaybackStateRepository
    ): PlayerManager {
        return PlayerManager(player, playbackStateRepository)
    }

    @Provides
    @Singleton
    fun providePlaybackPositionTracker(
        playbackStateRepository: PlaybackStateRepository
    ): PlaybackPositionTracker {
        return PlaybackPositionTracker(playbackStateRepository)
    }
}

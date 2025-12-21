package com.foxelectronic.audioplayer.di

import android.content.Context
import com.foxelectronic.audioplayer.SettingsRepository
import com.foxelectronic.audioplayer.data.repository.PlaybackStateRepository
import com.foxelectronic.audioplayer.repository.TrackCacheRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository {
        return SettingsRepository(context)
    }

    @Provides
    @Singleton
    fun provideTrackCacheRepository(@ApplicationContext context: Context): TrackCacheRepository {
        return TrackCacheRepository(context)
    }

    @Provides
    @Singleton
    fun providePlaybackStateRepository(settingsRepository: SettingsRepository): PlaybackStateRepository {
        return PlaybackStateRepository(settingsRepository)
    }
}

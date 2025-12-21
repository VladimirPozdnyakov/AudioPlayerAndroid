package com.foxelectronic.audioplayer.di

import android.content.Context
import androidx.room.Room
import com.foxelectronic.audioplayer.FavoriteDao
import com.foxelectronic.audioplayer.FavoriteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFavoriteDatabase(@ApplicationContext context: Context): FavoriteDatabase {
        return Room.databaseBuilder(
            context,
            FavoriteDatabase::class.java,
            "favorite_database"
        ).build()
    }

    @Provides
    fun provideFavoriteDao(database: FavoriteDatabase): FavoriteDao {
        return database.favoriteDao()
    }
}

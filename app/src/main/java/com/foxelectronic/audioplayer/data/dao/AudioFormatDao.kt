package com.foxelectronic.audioplayer.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.foxelectronic.audioplayer.data.model.AudioFormat
import kotlinx.coroutines.flow.Flow

/**
 * DAO для работы с аудио форматами
 */
@Dao
interface AudioFormatDao {
    /**
     * Получить формат для конкретного трека (suspend)
     */
    @Query("SELECT * FROM audio_formats WHERE trackId = :trackId")
    suspend fun getFormat(trackId: Long): AudioFormat?

    /**
     * Получить формат для конкретного трека (Flow для реактивных обновлений)
     */
    @Query("SELECT * FROM audio_formats WHERE trackId = :trackId")
    fun getFormatFlow(trackId: Long): Flow<AudioFormat?>

    /**
     * Получить форматы для списка треков
     */
    @Query("SELECT * FROM audio_formats WHERE trackId IN (:trackIds)")
    suspend fun getFormats(trackIds: List<Long>): List<AudioFormat>

    /**
     * Вставить формат (заменить при конфликте)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormat(format: AudioFormat)

    /**
     * Вставить несколько форматов
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormats(formats: List<AudioFormat>)

    /**
     * Обновить формат
     */
    @Update
    suspend fun updateFormat(format: AudioFormat)

    /**
     * Удалить формат для конкретного трека
     */
    @Query("DELETE FROM audio_formats WHERE trackId = :trackId")
    suspend fun deleteFormat(trackId: Long)

    /**
     * Удалить все форматы
     */
    @Query("DELETE FROM audio_formats")
    suspend fun deleteAllFormats()

    /**
     * Получить количество кэшированных форматов
     */
    @Query("SELECT COUNT(*) FROM audio_formats")
    suspend fun getFormatCount(): Int

    /**
     * Получить все форматы Hi-Res (sample rate >= 96000 или bit depth >= 24)
     */
    @Query("SELECT * FROM audio_formats WHERE sampleRate >= 96000 OR bitDepth >= 24")
    suspend fun getHiResFormats(): List<AudioFormat>

    /**
     * Получить все форматы без потерь
     */
    @Query("SELECT * FROM audio_formats WHERE isLossless = 1")
    suspend fun getLosslessFormats(): List<AudioFormat>
}

package com.foxelectronic.audioplayer.repository

import android.content.Context
import android.net.Uri
import com.foxelectronic.audioplayer.data.dao.TrackMetadataDao
import com.foxelectronic.audioplayer.data.model.TrackMetadataOverride
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class MetadataRepository(
    private val trackMetadataDao: TrackMetadataDao,
    private val context: Context
) {
    val allOverrides: Flow<List<TrackMetadataOverride>> = trackMetadataDao.getAllOverrides()

    /**
     * Сохранить переопределение метаданных в БД
     */
    suspend fun saveMetadataOverride(
        trackId: Long,
        title: String? = null,
        artist: String? = null,
        album: String? = null,
        coverImagePath: String? = null
    ) {
        val override = TrackMetadataOverride(
            trackId = trackId,
            customTitle = title,
            customArtist = artist,
            customAlbum = album,
            customCoverPath = coverImagePath
        )
        trackMetadataDao.insertOrUpdateOverride(override)
    }

    /**
     * Получить переопределение метаданных для трека
     */
    suspend fun getMetadataOverride(trackId: Long): TrackMetadataOverride? {
        return trackMetadataDao.getMetadataOverride(trackId)
    }

    /**
     * Удалить переопределение метаданных
     */
    suspend fun deleteMetadataOverride(trackId: Long) {
        trackMetadataDao.deleteOverrideById(trackId)
    }

    /**
     * Проверить, есть ли переопределение для трека
     */
    suspend fun hasOverride(trackId: Long): Boolean {
        return trackMetadataDao.hasOverride(trackId)
    }

    /**
     * Сохранить изображение обложки в filesDir
     * @return путь к сохраненному файлу или null при ошибке
     */
    suspend fun saveCoverImage(imageUri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val fileName = "cover_${System.currentTimeMillis()}.jpg"
            val coverDir = File(context.filesDir, "album_covers").apply {
                if (!exists()) mkdirs()
            }
            val outputFile = File(coverDir, fileName)

            context.contentResolver.openInputStream(imageUri)?.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            outputFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Удалить файл обложки
     */
    suspend fun deleteCoverImage(path: String) = withContext(Dispatchers.IO) {
        try {
            File(path).delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Получить директорию для хранения обложек
     */
    fun getCoverDirectory(): File {
        return File(context.filesDir, "album_covers").apply {
            if (!exists()) mkdirs()
        }
    }
}

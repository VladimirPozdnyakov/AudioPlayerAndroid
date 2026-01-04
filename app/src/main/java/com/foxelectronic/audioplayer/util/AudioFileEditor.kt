package com.foxelectronic.audioplayer.util

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.ArtworkFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Утилита для редактирования ID3 тегов аудиофайлов
 * Использует JAudioTagger + SAF для обхода ограничений Scoped Storage
 */
object AudioFileEditor {

    /**
     * Редактирование ID3 тегов аудиофайла
     * @param context Context приложения
     * @param uri URI аудиофайла (content:// схема)
     * @param title Новое название (null = не менять)
     * @param artist Новый исполнитель (null = не менять)
     * @param album Новый альбом (null = не менять)
     * @param coverImageFile Файл с изображением обложки (null = не менять)
     * @return Result.success() при успехе или Result.failure() с ошибкой
     */
    suspend fun editAudioFile(
        context: Context,
        uri: Uri,
        title: String? = null,
        artist: String? = null,
        album: String? = null,
        coverImageFile: File? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Открываем file descriptor для чтения/записи
            val pfd = context.contentResolver.openFileDescriptor(uri, "rw")
                ?: return@withContext Result.failure(Exception("Не удалось открыть файл для записи"))

            pfd.use { descriptor ->
                // Создаем временный файл для JAudioTagger
                val tempFile = File(context.cacheDir, "temp_audio_${System.currentTimeMillis()}.mp3")

                try {
                    // Копируем содержимое в temp файл
                    FileInputStream(descriptor.fileDescriptor).use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    // Редактируем через JAudioTagger
                    val audioFile = AudioFileIO.read(tempFile)
                    val tag = audioFile.tagOrCreateAndSetDefault

                    title?.let { tag.setField(FieldKey.TITLE, it) }
                    artist?.let { tag.setField(FieldKey.ARTIST, it) }
                    album?.let { tag.setField(FieldKey.ALBUM, it) }

                    coverImageFile?.let { imageFile ->
                        if (imageFile.exists()) {
                            val artwork = ArtworkFactory.createArtworkFromFile(imageFile)
                            tag.deleteArtworkField()
                            tag.setField(artwork)
                        }
                    }

                    audioFile.commit()

                    // Копируем обратно через file descriptor
                    FileOutputStream(descriptor.fileDescriptor).use { output ->
                        output.channel.truncate(0) // Очищаем оригинал
                        tempFile.inputStream().use { input ->
                            input.copyTo(output)
                        }
                    }

                    Result.success(Unit)
                } finally {
                    tempFile.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Чтение текущих метаданных из файла
     */
    suspend fun readMetadata(
        context: Context,
        uri: Uri
    ): Result<AudioMetadata> = withContext(Dispatchers.IO) {
        try {
            val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                ?: return@withContext Result.failure(Exception("Не удалось открыть файл для чтения"))

            pfd.use { descriptor ->
                val tempFile = File(context.cacheDir, "temp_read_${System.currentTimeMillis()}.mp3")

                try {
                    FileInputStream(descriptor.fileDescriptor).use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    val audioFile = AudioFileIO.read(tempFile)
                    val tag = audioFile.tag

                    Result.success(
                        AudioMetadata(
                            title = tag?.getFirst(FieldKey.TITLE),
                            artist = tag?.getFirst(FieldKey.ARTIST),
                            album = tag?.getFirst(FieldKey.ALBUM),
                            hasArtwork = tag?.firstArtwork != null
                        )
                    )
                } finally {
                    tempFile.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}

/**
 * Данные метаданных аудиофайла
 */
data class AudioMetadata(
    val title: String?,
    val artist: String?,
    val album: String?,
    val hasArtwork: Boolean
)

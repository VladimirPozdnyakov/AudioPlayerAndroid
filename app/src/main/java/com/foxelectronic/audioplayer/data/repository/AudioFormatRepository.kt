package com.foxelectronic.audioplayer.data.repository

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.foxelectronic.audioplayer.data.dao.AudioFormatDao
import com.foxelectronic.audioplayer.data.model.AudioFormat
import com.foxelectronic.audioplayer.data.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.jaudiotagger.audio.AudioFileIO
import java.io.File
import java.io.FileInputStream

/**
 * Repository для извлечения и кэширования информации об аудио форматах
 */
class AudioFormatRepository(
    private val audioFormatDao: AudioFormatDao,
    private val context: Context
) {
    /**
     * Извлечение формата аудио из URI через MediaMetadataRetriever и кэширование
     */
    suspend fun extractAndCacheFormat(trackId: Long, uri: Uri): AudioFormat? = withContext(Dispatchers.IO) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)

            val sampleRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)?.toIntOrNull()
            val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull()
            val mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)

            // Определение codec из MIME type
            val codec = mimeType?.let { mime ->
                when {
                    mime.contains("flac", ignoreCase = true) -> "FLAC"
                    mime.contains("mp3", ignoreCase = true) || mime.contains("mpeg", ignoreCase = true) -> "MP3"
                    mime.contains("aac", ignoreCase = true) || mime.contains("mp4", ignoreCase = true) -> "AAC"
                    mime.contains("opus", ignoreCase = true) -> "OPUS"
                    mime.contains("ogg", ignoreCase = true) || mime.contains("vorbis", ignoreCase = true) -> "OGG"
                    mime.contains("wav", ignoreCase = true) || mime.contains("wave", ignoreCase = true) -> "WAV"
                    mime.contains("wma", ignoreCase = true) -> "WMA"
                    mime.contains("alac", ignoreCase = true) -> "ALAC"
                    mime.contains("ape", ignoreCase = true) -> "APE"
                    else -> mime.substringAfter("/").uppercase()
                }
            } ?: "UNKNOWN"

            // Определение lossless форматов
            val isLossless = codec in listOf("FLAC", "WAV", "ALAC", "APE")

            val format = AudioFormat(
                trackId = trackId,
                sampleRate = sampleRate,
                bitrate = bitrate,
                bitDepth = null, // MediaMetadataRetriever не предоставляет bit depth
                codec = codec,
                channelCount = null, // Не предоставляется
                isLossless = isLossless
            )

            audioFormatDao.insertFormat(format)
            retriever.release()

            format
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Обогащение данных о формате через JAudioTagger (для Hi-Res файлов)
     */
    suspend fun enrichFormatWithJAudioTagger(trackId: Long, uri: Uri): AudioFormat? = withContext(Dispatchers.IO) {
        try {
            val existing = audioFormatDao.getFormat(trackId) ?: return@withContext null

            // Обогащаем только Hi-Res кандидатов
            if ((existing.sampleRate ?: 0) < 96000) return@withContext existing

            val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return@withContext existing
            pfd.use { descriptor ->
                val extension = uri.lastPathSegment?.substringAfterLast('.') ?: "mp3"
                val tempFile = File(context.cacheDir, "temp_format_${System.currentTimeMillis()}.$extension")

                try {
                    FileInputStream(descriptor.fileDescriptor).use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                    val audioFile = AudioFileIO.read(tempFile)
                    val audioHeader = audioFile.audioHeader

                    val enriched = existing.copy(
                        bitDepth = audioHeader.bitsPerSample?.toInt(),
                        channelCount = audioHeader.channels?.toInt(),
                        codec = audioHeader.format ?: existing.codec,
                        lastUpdated = System.currentTimeMillis()
                    )

                    audioFormatDao.updateFormat(enriched)
                    enriched
                } finally {
                    tempFile.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Получить формат для конкретного трека (Flow)
     */
    fun getFormat(trackId: Long): Flow<AudioFormat?> {
        return audioFormatDao.getFormatFlow(trackId)
    }

    /**
     * Получить формат для конкретного трека (suspend)
     */
    suspend fun getFormatSync(trackId: Long): AudioFormat? {
        return audioFormatDao.getFormat(trackId)
    }

    /**
     * Массовое извлечение форматов для списка треков
     */
    suspend fun bulkExtractFormats(tracks: List<Track>) = withContext(Dispatchers.IO) {
        tracks.forEach { track ->
            // Извлекаем только если еще нет в кэше
            if (audioFormatDao.getFormat(track.id) == null) {
                extractAndCacheFormat(track.id, track.uri)
            }
        }
    }

    /**
     * Обогащение Hi-Res файлов в фоне
     */
    suspend fun enrichHiResFormats(tracks: List<Track>) = withContext(Dispatchers.IO) {
        tracks.forEach { track ->
            val format = audioFormatDao.getFormat(track.id)
            // Обогащаем только если это потенциальный Hi-Res файл и еще не обогащен
            if (format != null && (format.sampleRate ?: 0) >= 96000 && format.bitDepth == null) {
                enrichFormatWithJAudioTagger(track.id, track.uri)
            }
        }
    }

    /**
     * Удалить формат для трека
     */
    suspend fun deleteFormat(trackId: Long) {
        audioFormatDao.deleteFormat(trackId)
    }

    /**
     * Очистить все кэшированные форматы
     */
    suspend fun clearAllFormats() {
        audioFormatDao.deleteAllFormats()
    }
}

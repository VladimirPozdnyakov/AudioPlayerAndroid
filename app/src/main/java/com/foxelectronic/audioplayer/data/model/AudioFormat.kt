package com.foxelectronic.audioplayer.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity для хранения информации о формате аудио файла
 */
@Entity(tableName = "audio_formats")
data class AudioFormat(
    @PrimaryKey val trackId: Long,
    val sampleRate: Int?,        // Hz (44100, 48000, 96000, 192000, etc.)
    val bitrate: Int?,           // bps (320000 для 320kbps, и т.д.)
    val bitDepth: Int?,          // bits (16, 24, 32)
    val codec: String?,          // FLAC, MP3, AAC, OPUS, OGG, WAV, etc.
    val channelCount: Int?,      // 2 для стерео, 1 для моно
    val isLossless: Boolean = false,  // Без потерь качества (FLAC, WAV, ALAC, APE)
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Классификация качества аудио
 */
enum class AudioQuality {
    UNKNOWN,          // Нет информации о формате
    STANDARD,         // ≤48kHz, lossy codec
    HIGH,             // 48kHz+ высокий bitrate (≥320kbps) или lossless 16-bit
    HI_RES,           // ≥96kHz или 24-bit+
    HI_RES_LOSSLESS   // ≥96kHz + lossless codec (FLAC/WAV)
}

/**
 * Определение качества аудио на основе параметров формата
 */
fun AudioFormat.getQuality(): AudioQuality {
    val isHiResSampleRate = (sampleRate ?: 0) >= 96000
    val isHiResBitDepth = (bitDepth ?: 0) >= 24
    val isHighBitrate = (bitrate ?: 0) >= 320000

    return when {
        isLossless && isHiResSampleRate -> AudioQuality.HI_RES_LOSSLESS
        isHiResSampleRate || isHiResBitDepth -> AudioQuality.HI_RES
        isLossless || isHighBitrate -> AudioQuality.HIGH
        codec != null -> AudioQuality.STANDARD
        else -> AudioQuality.UNKNOWN
    }
}

/**
 * Короткая метка для UI (для compact режима)
 */
fun AudioQuality.getShortLabel(): String = when (this) {
    AudioQuality.HI_RES_LOSSLESS -> "Hi-Res"
    AudioQuality.HI_RES -> "Hi-Res"
    AudioQuality.HIGH -> "HQ"
    AudioQuality.STANDARD -> "STD"
    AudioQuality.UNKNOWN -> ""
}

/**
 * Полное название для UI
 */
fun AudioQuality.getDisplayName(): String = when (this) {
    AudioQuality.HI_RES_LOSSLESS -> "Hi-Res Lossless"
    AudioQuality.HI_RES -> "Hi-Res"
    AudioQuality.HIGH -> "High Quality"
    AudioQuality.STANDARD -> "Standard"
    AudioQuality.UNKNOWN -> "Unknown"
}

/**
 * Форматированная строка для отображения в UI
 * Например: "24-bit / 192 kHz FLAC"
 */
fun AudioFormat.getFormattedString(): String {
    val parts = mutableListOf<String>()

    bitDepth?.let { parts.add("${it}-bit") }
    sampleRate?.let {
        val khz = it / 1000.0
        parts.add("$khz kHz")
    }
    codec?.let { parts.add(it) }

    return parts.joinToString(" / ")
}

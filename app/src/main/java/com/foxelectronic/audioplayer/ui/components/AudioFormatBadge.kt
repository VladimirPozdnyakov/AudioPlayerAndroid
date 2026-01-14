package com.foxelectronic.audioplayer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.foxelectronic.audioplayer.R
import com.foxelectronic.audioplayer.data.model.AudioFormat
import com.foxelectronic.audioplayer.data.model.AudioQuality
import com.foxelectronic.audioplayer.data.model.getDisplayName
import com.foxelectronic.audioplayer.data.model.getFormattedString
import com.foxelectronic.audioplayer.data.model.getQuality
import com.foxelectronic.audioplayer.data.model.getShortLabel

/**
 * Бейдж для отображения качества аудио
 */
@Composable
fun AudioFormatBadge(
    format: AudioFormat?,
    modifier: Modifier = Modifier,
    compact: Boolean = true
) {
    val quality = format?.getQuality() ?: AudioQuality.UNKNOWN
    if (quality == AudioQuality.UNKNOWN) return

    val (badgeColor, badgeText) = when (quality) {
        AudioQuality.HI_RES_LOSSLESS -> Color(0xFFFFD700) to if (compact) quality.getShortLabel() else quality.getDisplayName()
        AudioQuality.HI_RES -> Color(0xFFFF9800) to if (compact) quality.getShortLabel() else quality.getDisplayName()
        AudioQuality.HIGH -> Color(0xFF4CAF50) to if (compact) quality.getShortLabel() else quality.getDisplayName()
        AudioQuality.STANDARD -> Color(0xFF9E9E9E) to if (compact) quality.getShortLabel() else quality.getDisplayName()
        AudioQuality.UNKNOWN -> return
    }

    Surface(
        modifier = modifier,
        color = badgeColor.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.GraphicEq,
                contentDescription = null,
                tint = badgeColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelSmall,
                color = badgeColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Детальное отображение информации о формате и метаданных трека (для диалога)
 */
@Composable
fun DetailedFormatInfo(
    format: AudioFormat?,
    trackTitle: String? = null,
    trackArtist: String? = null,
    trackAlbum: String? = null,
    durationMs: Long = 0L,
    onArtistClick: ((String) -> Unit)? = null,
    onAlbumClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ID3 теги (метаданные трека)
        trackTitle?.let {
            FormatDetailRow(
                label = stringResource(R.string.field_title),
                value = it
            )
        }

        trackArtist?.let { artist ->
            FormatDetailRow(
                label = stringResource(R.string.field_artist),
                value = artist,
                isClickable = onArtistClick != null,
                onClick = { onArtistClick?.invoke(artist) }
            )
        }

        trackAlbum?.let { album ->
            FormatDetailRow(
                label = stringResource(R.string.field_album),
                value = album,
                isClickable = onAlbumClick != null,
                onClick = { onAlbumClick?.invoke(album) }
            )
        }

        if (durationMs > 0) {
            val minutes = (durationMs / 1000) / 60
            val seconds = (durationMs / 1000) % 60
            FormatDetailRow(
                label = stringResource(R.string.audio_format_duration),
                value = String.format("%d:%02d", minutes, seconds)
            )
        }

        // Разделитель между метаданными и техническими деталями
        if ((trackTitle != null || trackArtist != null || trackAlbum != null || durationMs > 0) && format != null) {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }

        // Качество аудио
        format?.let { fmt ->
            val quality = fmt.getQuality()
            if (quality != AudioQuality.UNKNOWN) {
                val (badgeColor, badgeText) = when (quality) {
                    AudioQuality.HI_RES_LOSSLESS -> Color(0xFFFFD700) to quality.getDisplayName()
                    AudioQuality.HI_RES -> Color(0xFFFF9800) to quality.getDisplayName()
                    AudioQuality.HIGH -> Color(0xFF4CAF50) to quality.getDisplayName()
                    AudioQuality.STANDARD -> Color(0xFF9E9E9E) to quality.getDisplayName()
                    AudioQuality.UNKNOWN -> null to null
                }

                if (badgeColor != null && badgeText != null) {
                    Surface(
                        color = badgeColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.GraphicEq,
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.titleMedium,
                                color = badgeColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // Технические детали
            fmt.codec?.let {
                FormatDetailRow(
                    label = stringResource(R.string.audio_format_codec),
                    value = it
                )
            }

            fmt.sampleRate?.let {
                FormatDetailRow(
                    label = stringResource(R.string.audio_format_sample_rate),
                    value = "${it / 1000.0} kHz"
                )
            }

            fmt.bitDepth?.let {
                FormatDetailRow(
                    label = stringResource(R.string.audio_format_bit_depth),
                    value = "$it-bit"
                )
            }

            fmt.bitrate?.let {
                FormatDetailRow(
                    label = stringResource(R.string.audio_format_bitrate),
                    value = "${it / 1000} kbps"
                )
            }

            fmt.channelCount?.let {
                val channelText = when (it) {
                    1 -> stringResource(R.string.audio_format_mono)
                    2 -> stringResource(R.string.audio_format_stereo)
                    else -> "$it ${stringResource(R.string.audio_format_channels)}"
                }
                FormatDetailRow(
                    label = stringResource(R.string.audio_format_channels_label),
                    value = channelText
                )
            }

            if (fmt.isLossless) {
                FormatDetailRow(
                    label = stringResource(R.string.audio_format_quality_type),
                    value = stringResource(R.string.audio_format_lossless)
                )
            }
        }
    }
}

/**
 * Строка с деталью формата
 */
@Composable
private fun FormatDetailRow(
    label: String,
    value: String,
    isClickable: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isClickable) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isClickable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Форматированная строка для отображения в одну линию
 * Например: "24-bit / 192 kHz / FLAC"
 */
@Composable
fun FormattedAudioInfo(
    format: AudioFormat?,
    modifier: Modifier = Modifier
) {
    format ?: return

    val formattedString = format.getFormattedString()
    if (formattedString.isNotEmpty()) {
        Text(
            text = formattedString,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier
        )
    }
}

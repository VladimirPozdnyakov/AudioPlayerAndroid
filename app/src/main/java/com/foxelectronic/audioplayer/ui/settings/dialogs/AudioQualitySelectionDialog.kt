package com.foxelectronic.audioplayer.ui.settings.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.foxelectronic.audioplayer.AudioQualityPreference
import com.foxelectronic.audioplayer.R
import com.foxelectronic.audioplayer.ui.components.ModernDialog
import com.foxelectronic.audioplayer.ui.components.ModernSelectionItem
import com.foxelectronic.audioplayer.ui.theme.AudioPlayerThemeExtended

/**
 * Современный диалог выбора качества аудио
 */
@Composable
fun AudioQualitySelectionDialog(
    currentQuality: AudioQualityPreference,
    onDismiss: () -> Unit,
    onConfirm: (AudioQualityPreference) -> Unit
) {
    var selectedQuality by remember { mutableStateOf(currentQuality) }

    ModernDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.audio_quality_preference),
        confirmText = stringResource(R.string.btn_ok),
        dismissText = stringResource(R.string.btn_cancel),
        onConfirm = { onConfirm(selectedQuality) },
        onDismiss = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AudioQualityPreference.entries.forEach { quality ->
                val (title, description, icon) = when (quality) {
                    AudioQualityPreference.AUTO -> Triple(
                        stringResource(R.string.audio_quality_auto),
                        stringResource(R.string.audio_quality_auto_desc),
                        Icons.Rounded.AutoAwesome
                    )
                    AudioQualityPreference.PRIORITIZE_QUALITY -> Triple(
                        stringResource(R.string.audio_quality_high),
                        stringResource(R.string.audio_quality_high_desc),
                        Icons.Rounded.HighQuality
                    )
                    AudioQualityPreference.SAVE_BANDWIDTH -> Triple(
                        stringResource(R.string.audio_quality_save),
                        stringResource(R.string.audio_quality_save_desc),
                        Icons.Rounded.Speed
                    )
                }

                ModernSelectionItem(
                    title = title,
                    subtitle = description,
                    selected = selectedQuality == quality,
                    onClick = { selectedQuality = quality },
                    icon = {
                        QualityIcon(
                            icon = icon,
                            selected = selectedQuality == quality
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun QualityIcon(
    icon: ImageVector,
    selected: Boolean
) {
    val extendedColors = AudioPlayerThemeExtended.colors

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) extendedColors.accentSoft
                else extendedColors.cardBorder.copy(alpha = 0.5f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) MaterialTheme.colorScheme.primary
            else extendedColors.subtleText,
            modifier = Modifier.size(22.dp)
        )
    }
}

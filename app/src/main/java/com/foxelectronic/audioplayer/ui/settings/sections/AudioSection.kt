package com.foxelectronic.audioplayer.ui.settings.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.foxelectronic.audioplayer.AudioQualityPreference
import com.foxelectronic.audioplayer.R
import com.foxelectronic.audioplayer.ui.components.SettingsListItem

/**
 * Секция настроек аудио
 */
@Composable
fun AudioSection(
    currentQuality: AudioQualityPreference,
    onQualityClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Настройка качества аудио
        val qualityLabel = when (currentQuality) {
            AudioQualityPreference.AUTO -> stringResource(R.string.audio_quality_auto)
            AudioQualityPreference.PRIORITIZE_QUALITY -> stringResource(R.string.audio_quality_high)
            AudioQualityPreference.SAVE_BANDWIDTH -> stringResource(R.string.audio_quality_save)
        }
        SettingsListItem(
            title = stringResource(R.string.audio_quality_preference),
            subtitle = qualityLabel,
            onClick = onQualityClick
        )
    }
}

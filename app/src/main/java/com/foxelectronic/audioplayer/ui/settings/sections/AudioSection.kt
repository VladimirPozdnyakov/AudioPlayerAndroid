package com.foxelectronic.audioplayer.ui.settings.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.foxelectronic.audioplayer.AudioQualityPreference
import com.foxelectronic.audioplayer.R
import com.foxelectronic.audioplayer.ui.components.SettingsListItem
import com.foxelectronic.audioplayer.ui.theme.AudioPlayerThemeExtended

/**
 * Секция настроек аудио с современным карточным дизайном
 */
@Composable
fun AudioSection(
    currentQuality: AudioQualityPreference,
    onQualityClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = AudioPlayerThemeExtended.dimens

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing)
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
            icon = Icons.Rounded.HighQuality,
            onClick = onQualityClick
        )
    }
}

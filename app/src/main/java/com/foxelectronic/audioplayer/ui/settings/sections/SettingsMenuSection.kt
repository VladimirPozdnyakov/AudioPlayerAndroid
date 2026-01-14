package com.foxelectronic.audioplayer.ui.settings.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.foxelectronic.audioplayer.R
import com.foxelectronic.audioplayer.ui.components.SettingsMenuButton

/**
 * Главное меню настроек
 */
@Composable
fun SettingsMenuSection(
    onNavigateToInterface: () -> Unit,
    onNavigateToAudio: () -> Unit,
    onNavigateToFolders: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsMenuButton(
            text = stringResource(R.string.settings_interface),
            icon = Icons.Rounded.Palette,
            onClick = onNavigateToInterface
        )
        SettingsMenuButton(
            text = stringResource(R.string.settings_audio),
            icon = Icons.Rounded.Headphones,
            onClick = onNavigateToAudio
        )
        SettingsMenuButton(
            text = stringResource(R.string.settings_folders),
            icon = Icons.Rounded.Folder,
            onClick = onNavigateToFolders
        )
        SettingsMenuButton(
            text = stringResource(R.string.settings_about),
            icon = Icons.Rounded.Info,
            onClick = onNavigateToAbout
        )
    }
}

package com.foxelectronic.audioplayer.ui.settings.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.foxelectronic.audioplayer.ui.components.SettingsMenuButton

/**
 * Главное меню настроек
 */
@Composable
fun SettingsMenuSection(
    onNavigateToInterface: () -> Unit,
    onNavigateToFolders: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SettingsMenuButton(
            text = "Интерфейс",
            icon = Icons.Rounded.Palette,
            onClick = onNavigateToInterface
        )
        SettingsMenuButton(
            text = "Папки",
            icon = Icons.Rounded.Folder,
            onClick = onNavigateToFolders
        )
        SettingsMenuButton(
            text = "О приложении",
            icon = Icons.Rounded.Info,
            onClick = onNavigateToAbout
        )
    }
}

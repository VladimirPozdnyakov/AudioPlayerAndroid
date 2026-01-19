package com.foxelectronic.audioplayer.ui.settings.sections

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import com.foxelectronic.audioplayer.R
import com.foxelectronic.audioplayer.ui.components.SettingsMenuButton
import com.foxelectronic.audioplayer.ui.theme.AudioPlayerThemeExtended
import kotlinx.coroutines.delay

/**
 * Главное меню настроек с анимацией появления
 */
@Composable
fun SettingsMenuSection(
    onNavigateToInterface: () -> Unit,
    onNavigateToAudio: () -> Unit,
    onNavigateToFolders: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = AudioPlayerThemeExtended.dimens

    // Анимация последовательного появления элементов
    var visibleItems by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        for (i in 1..4) {
            delay(50)
            visibleItems = i
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimens.itemSpacing)
    ) {
        AnimatedSettingsMenuButton(
            visible = visibleItems >= 1,
            text = stringResource(R.string.settings_interface),
            icon = Icons.Rounded.Palette,
            onClick = onNavigateToInterface
        )
        AnimatedSettingsMenuButton(
            visible = visibleItems >= 2,
            text = stringResource(R.string.settings_audio),
            icon = Icons.Rounded.Headphones,
            onClick = onNavigateToAudio
        )
        AnimatedSettingsMenuButton(
            visible = visibleItems >= 3,
            text = stringResource(R.string.settings_folders),
            icon = Icons.Rounded.Folder,
            onClick = onNavigateToFolders
        )
        AnimatedSettingsMenuButton(
            visible = visibleItems >= 4,
            text = stringResource(R.string.settings_about),
            icon = Icons.Rounded.Info,
            onClick = onNavigateToAbout
        )
    }
}

@Composable
private fun AnimatedSettingsMenuButton(
    visible: Boolean,
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "menuItemAlpha"
    )

    SettingsMenuButton(
        text = text,
        icon = icon,
        onClick = onClick,
        modifier = Modifier.alpha(alpha)
    )
}

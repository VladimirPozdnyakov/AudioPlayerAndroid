package com.foxelectronic.audioplayer.ui.settings.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.foxelectronic.audioplayer.AppTheme
import com.foxelectronic.audioplayer.R
import com.foxelectronic.audioplayer.ui.components.ModernDialog
import com.foxelectronic.audioplayer.ui.components.ModernSelectionItem
import com.foxelectronic.audioplayer.ui.theme.AudioPlayerThemeExtended

/**
 * Современный диалог выбора темы приложения
 */
@Composable
fun ThemeSelectionDialog(
    currentTheme: AppTheme,
    onDismiss: () -> Unit,
    onConfirm: (AppTheme) -> Unit
) {
    var selectedTheme by remember { mutableStateOf(currentTheme) }

    ModernDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.dialog_select_theme),
        confirmText = stringResource(R.string.btn_done),
        dismissText = stringResource(R.string.btn_cancel),
        onConfirm = {
            onConfirm(selectedTheme)
            onDismiss()
        },
        onDismiss = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ModernSelectionItem(
                title = stringResource(R.string.theme_system),
                subtitle = stringResource(R.string.theme_system_desc),
                selected = selectedTheme == AppTheme.SYSTEM,
                onClick = { selectedTheme = AppTheme.SYSTEM },
                icon = {
                    ThemeIcon(
                        icon = Icons.Rounded.BrightnessAuto,
                        selected = selectedTheme == AppTheme.SYSTEM
                    )
                }
            )
            ModernSelectionItem(
                title = stringResource(R.string.theme_light),
                subtitle = stringResource(R.string.theme_light_desc),
                selected = selectedTheme == AppTheme.LIGHT,
                onClick = { selectedTheme = AppTheme.LIGHT },
                icon = {
                    ThemeIcon(
                        icon = Icons.Rounded.LightMode,
                        selected = selectedTheme == AppTheme.LIGHT
                    )
                }
            )
            ModernSelectionItem(
                title = stringResource(R.string.theme_dark),
                subtitle = stringResource(R.string.theme_dark_desc),
                selected = selectedTheme == AppTheme.DARK,
                onClick = { selectedTheme = AppTheme.DARK },
                icon = {
                    ThemeIcon(
                        icon = Icons.Rounded.DarkMode,
                        selected = selectedTheme == AppTheme.DARK
                    )
                }
            )
        }
    }
}

@Composable
private fun ThemeIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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

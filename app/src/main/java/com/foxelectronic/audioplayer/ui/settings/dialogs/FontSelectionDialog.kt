package com.foxelectronic.audioplayer.ui.settings.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.TextFields
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
import com.foxelectronic.audioplayer.FontType
import com.foxelectronic.audioplayer.R
import com.foxelectronic.audioplayer.ui.components.ModernDialog
import com.foxelectronic.audioplayer.ui.components.ModernSelectionItem
import com.foxelectronic.audioplayer.ui.theme.AudioPlayerThemeExtended

/**
 * Современный диалог выбора шрифта приложения
 */
@Composable
fun FontSelectionDialog(
    currentFont: FontType,
    onDismiss: () -> Unit,
    onConfirm: (FontType) -> Unit
) {
    var selectedFont by remember { mutableStateOf(currentFont) }

    ModernDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.dialog_select_font),
        confirmText = stringResource(R.string.btn_done),
        dismissText = stringResource(R.string.btn_cancel),
        onConfirm = {
            onConfirm(selectedFont)
            onDismiss()
        },
        onDismiss = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ModernSelectionItem(
                title = stringResource(R.string.font_system),
                subtitle = stringResource(R.string.font_system_desc),
                selected = selectedFont == FontType.SYSTEM,
                onClick = { selectedFont = FontType.SYSTEM },
                icon = {
                    FontIcon(
                        icon = Icons.Rounded.TextFields,
                        selected = selectedFont == FontType.SYSTEM
                    )
                }
            )
            ModernSelectionItem(
                title = stringResource(R.string.font_jetbrains),
                subtitle = stringResource(R.string.font_jetbrains_desc),
                selected = selectedFont == FontType.JETBRAINS_MONO,
                onClick = { selectedFont = FontType.JETBRAINS_MONO },
                icon = {
                    FontIcon(
                        icon = Icons.Rounded.FontDownload,
                        selected = selectedFont == FontType.JETBRAINS_MONO
                    )
                }
            )
        }
    }
}

@Composable
private fun FontIcon(
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

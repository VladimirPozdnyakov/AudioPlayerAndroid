package com.foxelectronic.audioplayer.ui.settings.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.foxelectronic.audioplayer.AppLanguage
import com.foxelectronic.audioplayer.R
import com.foxelectronic.audioplayer.ui.components.ModernDialog
import com.foxelectronic.audioplayer.ui.components.ModernSelectionItem
import com.foxelectronic.audioplayer.ui.theme.AudioPlayerThemeExtended

/**
 * Современный диалог выбора языка приложения
 */
@Composable
fun LanguageSelectionDialog(
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (AppLanguage) -> Unit
) {
    var selectedLanguage by remember { mutableStateOf(currentLanguage) }

    ModernDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.dialog_select_language),
        confirmText = stringResource(R.string.btn_done),
        dismissText = stringResource(R.string.btn_cancel),
        onConfirm = {
            onConfirm(selectedLanguage)
            onDismiss()
        },
        onDismiss = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ModernSelectionItem(
                title = stringResource(R.string.language_english),
                subtitle = "English",
                selected = selectedLanguage == AppLanguage.ENGLISH,
                onClick = { selectedLanguage = AppLanguage.ENGLISH },
                icon = {
                    LanguageFlag(
                        flag = "EN",
                        selected = selectedLanguage == AppLanguage.ENGLISH
                    )
                }
            )
            ModernSelectionItem(
                title = stringResource(R.string.language_russian),
                subtitle = "Русский",
                selected = selectedLanguage == AppLanguage.RUSSIAN,
                onClick = { selectedLanguage = AppLanguage.RUSSIAN },
                icon = {
                    LanguageFlag(
                        flag = "RU",
                        selected = selectedLanguage == AppLanguage.RUSSIAN
                    )
                }
            )
        }
    }
}

@Composable
private fun LanguageFlag(
    flag: String,
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
        Text(
            text = flag,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = if (selected) MaterialTheme.colorScheme.primary
            else extendedColors.subtleText
        )
    }
}

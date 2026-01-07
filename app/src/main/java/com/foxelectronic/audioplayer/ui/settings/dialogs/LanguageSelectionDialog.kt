package com.foxelectronic.audioplayer.ui.settings.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.foxelectronic.audioplayer.AppLanguage
import com.foxelectronic.audioplayer.R
import com.foxelectronic.audioplayer.ui.components.RadioOptionRow

/**
 * Диалог выбора языка приложения
 */
@Composable
fun LanguageSelectionDialog(
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (AppLanguage) -> Unit
) {
    var selectedLanguage by remember { mutableStateOf(currentLanguage) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_select_language)) },
        text = {
            Column {
                RadioOptionRow(
                    label = stringResource(R.string.language_english),
                    selected = selectedLanguage == AppLanguage.ENGLISH,
                    onSelect = { selectedLanguage = AppLanguage.ENGLISH }
                )
                RadioOptionRow(
                    label = stringResource(R.string.language_russian),
                    selected = selectedLanguage == AppLanguage.RUSSIAN,
                    onSelect = { selectedLanguage = AppLanguage.RUSSIAN }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(selectedLanguage)
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.btn_done))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}

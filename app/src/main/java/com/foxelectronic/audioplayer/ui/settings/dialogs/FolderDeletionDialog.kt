package com.foxelectronic.audioplayer.ui.settings.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * НОВАЯ ФИЧА: Диалог подтверждения удаления папки
 */
@Composable
fun FolderDeletionDialog(
    folderName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Удалить папку?") },
        text = {
            Text("Вы действительно хотите удалить папку \"$folderName\" из списка? Файлы не будут удалены с устройства.")
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text("Удалить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

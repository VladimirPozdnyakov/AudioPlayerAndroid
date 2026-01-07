package com.foxelectronic.audioplayer.ui.settings.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.foxelectronic.audioplayer.R
import com.foxelectronic.audioplayer.ui.components.SettingsListItem
import com.foxelectronic.audioplayer.ui.settings.utils.FolderUtils

/**
 * Секция управления папками
 */
@Composable
fun FoldersSection(
    folders: List<String>,
    onAddFolder: () -> Unit,
    onRemoveFolder: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Список папок
        folders.forEach { folder ->
            val displayName = remember(folder) { FolderUtils.getFolderDisplayName(folder) }
            val isDefault = FolderUtils.isDefaultFolder(folder)

            if (!isDefault) {
                // Обычная папка с кнопкой удаления
                val deleteFolderDesc = stringResource(R.string.delete_folder_desc, displayName)
                SettingsListItem(
                    title = displayName,
                    subtitle = "",
                    trailing = {
                        IconButton(
                            onClick = { onRemoveFolder(folder) }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = deleteFolderDesc
                            )
                        }
                    },
                    onClick = {}
                )
            } else {
                // Дефолтная папка без кнопки удаления
                SettingsListItem(
                    title = "$displayName ${stringResource(R.string.label_default)}",
                    subtitle = stringResource(R.string.default_music_folder),
                    onClick = {}
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Кнопка добавления папки
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            FilledTonalButton(
                onClick = onAddFolder,
                modifier = Modifier.height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.add_folder))
            }
        }
    }
}

package com.foxelectronic.audioplayer.ui.playlist.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlaylistRemove
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.foxelectronic.audioplayer.R
import com.foxelectronic.audioplayer.ui.components.ModernDialog
import com.foxelectronic.audioplayer.ui.theme.AudioPlayerThemeExtended

/**
 * Современный диалог создания нового плейлиста
 */
@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var playlistName by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    ModernDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.dialog_new_playlist),
        confirmText = stringResource(R.string.btn_create),
        dismissText = stringResource(R.string.btn_cancel),
        onConfirm = {
            if (playlistName.isNotBlank()) {
                onCreate(playlistName.trim())
                onDismiss()
            }
        },
        onDismiss = onDismiss,
        confirmEnabled = playlistName.isNotBlank()
    ) {
        OutlinedTextField(
            value = playlistName,
            onValueChange = { playlistName = it },
            label = { Text(stringResource(R.string.playlist_name_hint)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
    }
}

/**
 * Современный диалог переименования плейлиста
 */
@Composable
fun RenamePlaylistDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    var playlistName by remember { mutableStateOf(currentName) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    ModernDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.dialog_rename_playlist),
        confirmText = stringResource(R.string.btn_save),
        dismissText = stringResource(R.string.btn_cancel),
        onConfirm = {
            if (playlistName.isNotBlank()) {
                onRename(playlistName.trim())
                onDismiss()
            }
        },
        onDismiss = onDismiss,
        confirmEnabled = playlistName.isNotBlank()
    ) {
        OutlinedTextField(
            value = playlistName,
            onValueChange = { playlistName = it },
            label = { Text(stringResource(R.string.playlist_name_hint)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
    }
}

/**
 * Диалог подтверждения удаления плейлиста
 */
@Composable
fun DeletePlaylistDialog(
    playlistName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val extendedColors = AudioPlayerThemeExtended.colors

    ModernDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.dialog_delete_playlist),
        confirmText = stringResource(R.string.btn_delete),
        dismissText = stringResource(R.string.btn_cancel),
        onConfirm = {
            onConfirm()
            onDismiss()
        },
        onDismiss = onDismiss,
        isDestructive = true
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Иконка предупреждения
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlaylistRemove,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            // Название плейлиста
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(extendedColors.cardBackgroundElevated)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = playlistName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(12.dp))

            // Текст подтверждения
            Text(
                text = stringResource(R.string.delete_playlist_confirm, playlistName),
                style = MaterialTheme.typography.bodyMedium,
                color = extendedColors.subtleText,
                textAlign = TextAlign.Center
            )
        }
    }
}

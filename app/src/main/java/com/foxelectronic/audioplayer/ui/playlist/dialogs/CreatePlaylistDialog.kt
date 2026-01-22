package com.foxelectronic.audioplayer.ui.playlist.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import com.foxelectronic.audioplayer.R
import com.foxelectronic.audioplayer.ui.components.ModernDialog

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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_delete_playlist)) },
        text = {
            Text(stringResource(R.string.delete_playlist_confirm, playlistName))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.btn_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}

package com.foxelectronic.audioplayer.ui.playlist.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.foxelectronic.audioplayer.R
import com.foxelectronic.audioplayer.data.model.PlaylistWithTrackCount

/**
 * Диалог добавления трека в плейлист
 * @param playlists список всех плейлистов
 * @param trackId ID трека, который добавляем
 * @param playlistsContainingTrack множество ID плейлистов, уже содержащих этот трек
 * @param onDismiss колбэк закрытия диалога
 * @param onPlaylistSelected колбэк выбора плейлиста (добавление)
 * @param onPlaylistRemoved колбэк удаления трека из плейлиста
 * @param onCreateNewPlaylist колбэк создания нового плейлиста
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun AddToPlaylistDialog(
    playlists: List<PlaylistWithTrackCount>,
    trackId: Long,
    playlistsContainingTrack: Set<Long>,
    onDismiss: () -> Unit,
    onPlaylistSelected: (PlaylistWithTrackCount) -> Unit,
    onPlaylistRemoved: (PlaylistWithTrackCount) -> Unit,
    onCreateNewPlaylist: () -> Unit
) {
    val trackInPlaylistDesc = stringResource(R.string.track_in_playlist)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_add_to_playlist)) },
        text = {
            Column {
                // Кнопка создания нового плейлиста
                ListItem(
                    headlineContent = { Text(stringResource(R.string.create_new_playlist)) },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.clickable {
                        onCreateNewPlaylist()
                    }
                )

                HorizontalDivider()

                if (playlists.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.empty_no_playlists),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(playlists) { playlist ->
                            val isTrackInPlaylist = playlistsContainingTrack.contains(playlist.playlistId)

                            ListItem(
                                headlineContent = {
                                    Text(
                                        text = playlist.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = if (isTrackInPlaylist)
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                supportingContent = if (isTrackInPlaylist) {
                                    { Text(stringResource(R.string.click_to_remove), style = MaterialTheme.typography.bodySmall) }
                                } else null,
                                leadingContent = {
                                    @Suppress("DEPRECATION")
                                    Icon(
                                        imageVector = Icons.Default.QueueMusic,
                                        contentDescription = null,
                                        tint = if (isTrackInPlaylist)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                trailingContent = if (isTrackInPlaylist) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = trackInPlaylistDesc,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } else null,
                                modifier = Modifier.clickable {
                                    if (isTrackInPlaylist) {
                                        onPlaylistRemoved(playlist)
                                    } else {
                                        onPlaylistSelected(playlist)
                                    }
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}

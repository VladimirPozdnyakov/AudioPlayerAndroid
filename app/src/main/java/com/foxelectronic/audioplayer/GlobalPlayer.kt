package com.foxelectronic.audioplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.RepeatOne
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures

@Composable
fun GlobalPlayerBar(
    uiState: PlayerUiState,
    viewModel: PlayerViewModel,
    onMiniPlayerClick: () -> Unit = {},
    modifier: Modifier = Modifier.height(72.dp)
) {
    if (uiState.currentIndex >= 0 && uiState.tracks.isNotEmpty()) {
        val currentTrack = uiState.tracks.getOrNull(uiState.currentIndex)
        val progress = if (uiState.durationMs > 0) {
            (uiState.positionMs.toFloat() / uiState.durationMs.toFloat()).coerceIn(0f, 1f)
        } else 0f

        Card(
            modifier = modifier
                .wrapContentWidth()
                .wrapContentHeight()
                .clickable { onMiniPlayerClick() }, // Делает мини-плеер кликабельным для открытия полноэкранного плеера
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
            border = null
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Информация о треке
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        currentTrack?.let { track ->
                            val currentTrackId by rememberUpdatedState(track.id)
                            val lastTrackId = remember { mutableStateOf(track.id) }

                            // Анимация при смене трека
                            val animatedAlpha by animateFloatAsState(
                                targetValue = if (currentTrackId != lastTrackId.value) 1f else 0.7f,
                                animationSpec = tween(
                                    durationMillis = 500,
                                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                                ),
                                label = "alpha"
                            )

                            LaunchedEffect(currentTrackId) {
                                if (currentTrackId != lastTrackId.value) {
                                    lastTrackId.value = currentTrackId
                                }
                            }

                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.graphicsLayer(alpha = animatedAlpha)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = track.artist ?: "Неизвестный исполнитель",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                // Таймер трека
                                Text(
                                    text = "${formatTime(uiState.positionMs)} / ${formatTime(uiState.durationMs)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Элементы управления
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Режим перемешивания
                        IconButton(
                            onClick = { viewModel.toggleShuffleMode() },
                            modifier = Modifier
                                .size(36.dp),
                            enabled = uiState.repeatMode != androidx.media3.common.Player.REPEAT_MODE_ONE
                        ) {
                            Icon(
                                Icons.Outlined.Shuffle,
                                contentDescription = "Shuffle",
                                modifier = Modifier.size(20.dp),
                                tint = if (uiState.isShuffleModeEnabled && uiState.repeatMode != androidx.media3.common.Player.REPEAT_MODE_ONE) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }

                        // Предыдущий
                        val (isPrevPressed, setPrevPressed) = remember { mutableStateOf(false) }
                        val prevScale by animateFloatAsState(
                            targetValue = if (isPrevPressed) 0.8f else 1f,
                            animationSpec = tween(
                                durationMillis = 200,
                                easing = androidx.compose.animation.core.LinearEasing
                            ),
                            label = "prevScale"
                        )

                        IconButton(
                            onClick = { viewModel.previous() },
                            modifier = Modifier
                                .size(36.dp)
                                .scale(prevScale)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            setPrevPressed(true)
                                            tryAwaitRelease()
                                            setPrevPressed(false)
                                        }
                                    )
                                }
                        ) {
                            Icon(
                                Icons.Outlined.SkipPrevious,
                                contentDescription = "Previous",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Воспроизведение/Пауза
                        Box(
                            modifier = Modifier
                                .size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val cornerRadius by animateFloatAsState(
                                targetValue = if (uiState.isPlaying) 12f else 20f, // 20f approximates a circle (40dp/2)
                                animationSpec = tween(
                                    durationMillis = 300,
                                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                                ),
                                label = "cornerRadius"
                            )
                            
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(cornerRadius.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable {
                                        if (uiState.isPlaying) viewModel.pause() else viewModel.resume()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                val scale by animateFloatAsState(
                                    targetValue = if (uiState.isPlaying) 1.2f else 1f,
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = androidx.compose.animation.core.FastOutSlowInEasing
                                    ),
                                    label = "scale"
                                )

                                Icon(
                                    if (uiState.isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                    contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .scale(scale),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Следующий
                        val (isNextPressed, setNextPressed) = remember { mutableStateOf(false) }
                        val nextScale by animateFloatAsState(
                            targetValue = if (isNextPressed) 0.8f else 1f,
                            animationSpec = tween(
                                durationMillis = 200,
                                easing = androidx.compose.animation.core.LinearEasing
                            ),
                            label = "nextScale"
                        )

                        IconButton(
                            onClick = { viewModel.next() },
                            modifier = Modifier
                                .size(36.dp)
                                .scale(nextScale)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            setNextPressed(true)
                                            tryAwaitRelease()
                                            setNextPressed(false)
                                        }
                                    )
                                }
                        ) {
                            Icon(
                                Icons.Outlined.SkipNext,
                                contentDescription = "Next",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Режим повтора
                        IconButton(
                            onClick = { viewModel.toggleRepeatMode() },
                            modifier = Modifier
                                .size(36.dp)
                        ) {
                            Icon(
                                when (uiState.repeatMode) {
                                    androidx.media3.common.Player.REPEAT_MODE_OFF -> Icons.Outlined.Repeat
                                    androidx.media3.common.Player.REPEAT_MODE_ALL -> Icons.Outlined.Repeat
                                    else -> Icons.Outlined.RepeatOne
                                },
                                contentDescription = when (uiState.repeatMode) {
                                    androidx.media3.common.Player.REPEAT_MODE_OFF -> "Repeat off"
                                    androidx.media3.common.Player.REPEAT_MODE_ALL -> "Repeat all"
                                    else -> "Repeat one"
                                },
                                modifier = Modifier.size(20.dp),
                                tint = when (uiState.repeatMode) {
                                    androidx.media3.common.Player.REPEAT_MODE_OFF -> MaterialTheme.colorScheme.onSurfaceVariant
                                    else -> MaterialTheme.colorScheme.primary
                                }
                            )
                        }
                    }
                }

                // Полоса прогресса внизу
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
private fun formatTime(millis: Long): String {
    val seconds = (millis / 1000) % 60
    val minutes = (millis / (1000 * 60)) % 60
    val hours = (millis / (1000 * 60 * 60))

    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}

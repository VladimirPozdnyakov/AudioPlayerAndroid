package com.foxelectronic.audioplayer.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player

@Composable
fun RepeatModeButton(
    repeatMode: Int,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    iconSize: Dp = 20.dp,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    IconButton(
        onClick = onToggle,
        modifier = modifier.size(size)
    ) {
        Icon(
            imageVector = when (repeatMode) {
                Player.REPEAT_MODE_OFF -> Icons.Rounded.Repeat
                Player.REPEAT_MODE_ALL -> Icons.Rounded.Repeat
                else -> Icons.Rounded.RepeatOne
            },
            contentDescription = when (repeatMode) {
                Player.REPEAT_MODE_OFF -> "Повтор выключен"
                Player.REPEAT_MODE_ALL -> "Повтор всех"
                else -> "Повтор одного"
            },
            modifier = Modifier.size(iconSize),
            tint = when (repeatMode) {
                Player.REPEAT_MODE_OFF -> inactiveColor
                else -> activeColor
            }
        )
    }
}

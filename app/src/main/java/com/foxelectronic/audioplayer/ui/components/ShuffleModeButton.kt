package com.foxelectronic.audioplayer.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ShuffleModeButton(
    isEnabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 36.dp,
    iconSize: Dp = 20.dp,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    IconButton(
        onClick = onToggle,
        modifier = modifier.size(size),
        enabled = enabled
    ) {
        Icon(
            imageVector = Icons.Rounded.Shuffle,
            contentDescription = if (isEnabled) "Перемешивание включено" else "Перемешивание выключено",
            modifier = Modifier.size(iconSize),
            tint = if (isEnabled && enabled) activeColor else inactiveColor
        )
    }
}

package com.foxelectronic.audioplayer.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.foxelectronic.audioplayer.ui.theme.AudioPlayerThemeExtended

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
    val extendedColors = AudioPlayerThemeExtended.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(100),
        label = "repeatButtonScale"
    )

    val isEnabled = repeatMode != Player.REPEAT_MODE_OFF

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isEnabled) extendedColors.accentSoft
                else Color.Transparent
            )
            .border(
                if (isEnabled) 1.5.dp else 1.dp,
                if (isEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                else extendedColors.cardBorder,
                RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = activeColor),
                onClick = onToggle
            ),
        contentAlignment = Alignment.Center
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
            tint = if (isEnabled) activeColor else extendedColors.iconTint
        )
    }
}

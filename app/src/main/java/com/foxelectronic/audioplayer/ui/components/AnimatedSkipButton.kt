package com.foxelectronic.audioplayer.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class SkipDirection { Previous, Next }

@Composable
fun AnimatedSkipButton(
    direction: SkipDirection,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    iconSize: Dp = 20.dp,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val (isPressed, setPressed) = remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = tween(
            durationMillis = 200,
            easing = LinearEasing
        ),
        label = "skipButtonScale"
    )

    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(size)
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        setPressed(true)
                        tryAwaitRelease()
                        setPressed(false)
                    }
                )
            }
    ) {
        Icon(
            imageVector = when (direction) {
                SkipDirection.Previous -> Icons.Rounded.SkipPrevious
                SkipDirection.Next -> Icons.Rounded.SkipNext
            },
            contentDescription = when (direction) {
                SkipDirection.Previous -> "Предыдущий"
                SkipDirection.Next -> "Следующий"
            },
            modifier = Modifier.size(iconSize),
            tint = tint
        )
    }
}

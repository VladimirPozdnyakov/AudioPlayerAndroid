package com.foxelectronic.audioplayer.ui.settings.colorpicker

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.foxelectronic.audioplayer.R
import com.foxelectronic.audioplayer.ui.components.ModernDialog
import com.foxelectronic.audioplayer.ui.settings.utils.ColorUtils
import com.foxelectronic.audioplayer.ui.theme.AudioPlayerThemeExtended

// Предустановленные цвета
private val presetColors = listOf(
    "#6750A4", // Material You Purple (default)
    "#D32F2F", // Red
    "#E91E63", // Pink
    "#9C27B0", // Purple
    "#673AB7", // Deep Purple
    "#3F51B5", // Indigo
    "#2196F3", // Blue
    "#03A9F4", // Light Blue
    "#00BCD4", // Cyan
    "#009688", // Teal
    "#4CAF50", // Green
    "#8BC34A", // Light Green
    "#FF9800", // Orange
    "#FF5722", // Deep Orange
    "#795548", // Brown
    "#607D8B"  // Blue Grey
)

/**
 * Современный диалог выбора акцентного цвета
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPickerDialog(
    currentAccentHex: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val initialColor = ColorUtils.parseColorSafe(currentAccentHex)
    val pickerState = rememberColorPickerState(initialColor)
    val extendedColors = AudioPlayerThemeExtended.colors

    val colorPickerDesc = stringResource(R.string.color_picker_desc)
    val hueSliderDesc = stringResource(R.string.hue_slider_desc)

    ModernDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.dialog_select_color),
        confirmText = stringResource(R.string.btn_done),
        dismissText = stringResource(R.string.btn_cancel),
        onConfirm = {
            val hex = ColorUtils.colorToHex(pickerState.currentColor)
            onConfirm(hex)
        },
        onDismiss = onDismiss
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Предустановленные цвета
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(extendedColors.cardBackgroundElevated)
                    .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.preset_colors),
                    style = MaterialTheme.typography.labelMedium,
                    color = extendedColors.subtleText,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    presetColors.forEach { colorHex ->
                        val color = ColorUtils.parseColorSafe(colorHex)
                        val isSelected = ColorUtils.colorToHex(pickerState.currentColor)
                            .equals(colorHex, ignoreCase = true)

                        PresetColorItem(
                            color = color,
                            isSelected = isSelected,
                            onClick = {
                                val hsv = ColorUtils.colorToHsv(color)
                                pickerState.updateHue(hsv[0])
                                pickerState.updateSaturationValue(hsv[1], hsv[2])
                            }
                        )
                    }
                }
            }

            // Палитра выбора цвета
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(extendedColors.cardBackgroundElevated)
                    .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.custom_color),
                    style = MaterialTheme.typography.labelMedium,
                    color = extendedColors.subtleText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SaturationValuePanel(
                        hue = pickerState.hue,
                        saturation = pickerState.saturation,
                        value = pickerState.value,
                        onChange = pickerState::updateSaturationValue,
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(12.dp))
                            .semantics { contentDescription = colorPickerDesc }
                    )
                    HueBar(
                        hue = pickerState.hue,
                        onChange = pickerState::updateHue,
                        modifier = Modifier
                            .height(180.dp)
                            .width(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(12.dp))
                            .semantics { contentDescription = hueSliderDesc }
                    )
                }
            }

            // Превью выбранного цвета и HEX код
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(extendedColors.cardBackgroundElevated)
                    .border(1.dp, extendedColors.cardBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Превью цвета
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(pickerState.currentColor)
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            RoundedCornerShape(10.dp)
                        )
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.selected_color),
                        style = MaterialTheme.typography.labelMedium,
                        color = extendedColors.subtleText
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = ColorUtils.colorToHex(pickerState.currentColor),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Кнопка сброса
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.9f else 1f,
                    animationSpec = tween(100),
                    label = "resetScale"
                )

                Box(
                    modifier = Modifier
                        .scale(scale)
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(extendedColors.accentSoft)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = rememberRipple(color = MaterialTheme.colorScheme.primary),
                            onClick = { pickerState.reset() }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = stringResource(R.string.reset_to_default),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetColorItem(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val extendedColors = AudioPlayerThemeExtended.colors

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(100),
        label = "presetScale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                } else {
                    Modifier.border(1.dp, extendedColors.cardBorder, CircleShape)
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(color = Color.White),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = if (ColorUtils.isColorDark(color)) Color.White else Color.Black,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

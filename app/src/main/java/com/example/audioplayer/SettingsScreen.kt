package com.example.audioplayer

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Palette
// Reuse Palette icon to avoid missing icon on some devices
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import android.os.Build
import android.content.pm.PackageManager
import android.provider.DocumentsContract
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.max
import kotlin.math.min

private enum class SettingsSection { MENU, INTERFACE, FOLDERS, ABOUT }

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsScreen(
    state: SettingsUiState,
    onThemeChange: (AppTheme) -> Unit,
    onAccentChange: (String) -> Unit,
    onAddFolder: (String) -> Unit,
    onRemoveFolder: (String) -> Unit
) {
    val context = LocalContext.current
    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (_: Throwable) { }
            onAddFolder(it.toString())
        }
    }

    val versionName by remember {
        mutableStateOf(
            try {
                val pm = context.packageManager
                if (Build.VERSION.SDK_INT >= 33) {
                    pm.getPackageInfo(
                        context.packageName,
                        PackageManager.PackageInfoFlags.of(0)
                    ).versionName
                } else {
                    @Suppress("DEPRECATION")
                    pm.getPackageInfo(context.packageName, 0).versionName
                }
            } catch (_: Throwable) { "-" }
        )
    }

    var section by remember { mutableStateOf(SettingsSection.MENU) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAccentDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            when (section) {
                SettingsSection.MENU -> {
                    TopAppBar(title = { Text("Настройки") })
                }
                SettingsSection.INTERFACE -> {
                    TopAppBar(
                        title = { Text("Интерфейс") },
                        navigationIcon = {
                            IconButton(onClick = { section = SettingsSection.MENU }) {
                                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Назад")
                            }
                        }
                    )
                }
                SettingsSection.FOLDERS -> {
                    TopAppBar(
                        title = { Text("Папки") },
                        navigationIcon = {
                            IconButton(onClick = { section = SettingsSection.MENU }) {
                                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Назад")
                            }
                        }
                    )
                }
                SettingsSection.ABOUT -> {
                    TopAppBar(
                        title = { Text("О приложении") },
                        navigationIcon = {
                            IconButton(onClick = { section = SettingsSection.MENU }) {
                                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Назад")
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            AnimatedContent(
            targetState = section,
            label = "settings_sections",
            transitionSpec = {
                if (targetState.ordinal > initialState.ordinal || initialState == SettingsSection.MENU) {
                    slideInHorizontally(
                        animationSpec = tween(220),
                        initialOffsetX = { it }
                    ) togetherWith slideOutHorizontally(
                        animationSpec = tween(220),
                        targetOffsetX = { -it / 2 }
                    )
                } else {
                    slideInHorizontally(
                        animationSpec = tween(220),
                        initialOffsetX = { -it }
                    ) togetherWith slideOutHorizontally(
                        animationSpec = tween(220),
                        targetOffsetX = { it / 2 }
                    )
                }
            }
            ) { current ->
                when (current) {
                    SettingsSection.MENU -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                onClick = { section = SettingsSection.INTERFACE },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Icon(
                                    Icons.Outlined.Palette,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Интерфейс")
                            }
                            Button(
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                onClick = { section = SettingsSection.FOLDERS },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Icon(
                                    Icons.Outlined.Folder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Папки")
                            }
                            Button(
                                modifier = Modifier.fillMaxWidth().height(44.dp),
                                onClick = { section = SettingsSection.ABOUT },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Icon(
                                    Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("О приложении")
                            }
                        }
                    }
                    SettingsSection.INTERFACE -> {
                        Spacer(Modifier.height(4.dp))
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                        ) {
                            item {
                                val themeLabel = when (state.theme) {
                                    AppTheme.SYSTEM -> "Системная"
                                    AppTheme.LIGHT -> "Светлая"
                                    AppTheme.DARK -> "Тёмная"
                                }
                                SettingItem(
                                    title = "Тема",
                                    subtitle = themeLabel,
                                    onClick = { showThemeDialog = true }
                                )
                            }
                            item {
                                val accentName = when (state.accentHex.uppercase()) {
                                    "#6750A4" -> "Фиолетовый"
                                    "#1E88E5" -> "Синий"
                                    "#2E7D32" -> "Зелёный"
                                    "#E53935" -> "Красный"
                                    "#FB8C00" -> "Оранжевый"
                                    "#D81B60" -> "Розовый"
                                    else -> state.accentHex.uppercase()
                                }
                                SettingItem(
                                    title = "Акцентный цвет",
                                    subtitle = accentName,
                                    trailing = {
                                        val color = try { Color(android.graphics.Color.parseColor(state.accentHex)) } catch (_: Throwable) { Color.Magenta }
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                                .semantics { contentDescription = "accent_preview" }
                                        )
                                    },
                                    onClick = { showAccentDialog = true }
                                )
                            }
                        }

                        if (showThemeDialog) {
                            var selectedTheme by remember { mutableStateOf(state.theme) }
                            AlertDialog(
                                onDismissRequest = { showThemeDialog = false },
                                title = { Text("Выберите тему") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        ThemeOptionRow(
                                            label = "Системная",
                                            selected = selectedTheme == AppTheme.SYSTEM,
                                            onSelect = { selectedTheme = AppTheme.SYSTEM }
                                        )
                                        ThemeOptionRow(
                                            label = "Светлая",
                                            selected = selectedTheme == AppTheme.LIGHT,
                                            onSelect = { selectedTheme = AppTheme.LIGHT }
                                        )
                                        ThemeOptionRow(
                                            label = "Тёмная",
                                            selected = selectedTheme == AppTheme.DARK,
                                            onSelect = { selectedTheme = AppTheme.DARK }
                                        )
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        onThemeChange(selectedTheme)
                                        showThemeDialog = false
                                    }) { Text("Готово") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showThemeDialog = false }) { Text("Отмена") }
                                }
                            )
                        }

                        if (showAccentDialog) {
                            // Инициализация HSV из текущего цвета
                            val startInt = try { android.graphics.Color.parseColor(state.accentHex) } catch (_: Throwable) { android.graphics.Color.parseColor("#6750A4") }
                            val hsvArr = FloatArray(3).also { android.graphics.Color.colorToHSV(startInt, it) }
                            var hue by remember { mutableStateOf(hsvArr[0]) } // 0..360
                            var sat by remember { mutableStateOf(hsvArr[1]) } // 0..1
                            var value by remember { mutableStateOf(hsvArr[2]) } // 0..1

                            val currentColor = remember(hue, sat, value) { Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, sat, value))) }

                            AlertDialog(
                                onDismissRequest = { showAccentDialog = false },
                                title = { Text("Выберите цвет") },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            SaturationValuePanel(
                                                hue = hue,
                                                saturation = sat,
                                                value = value,
                                                onChange = { s, v -> sat = s; value = v },
                                                modifier = Modifier.size(220.dp)
                                            )
                                            HueBar(
                                                hue = hue,
                                                onChange = { hue = it },
                                                modifier = Modifier.height(220.dp).width(28.dp)
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(startInt)))
                                            Spacer(Modifier.width(12.dp))
                                            Text("→")
                                            Spacer(Modifier.width(12.dp))
                                            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(currentColor))
                                            Spacer(Modifier.width(12.dp))
                                            Text(String.format("#%06X", currentColor.toArgb() and 0xFFFFFF))
                                        }
                                        TextButton(onClick = {
                                            // Сброс на стандартный
                                            val def = android.graphics.Color.parseColor("#B498FF")
                                            val h = FloatArray(3)
                                            android.graphics.Color.colorToHSV(def, h)
                                            hue = h[0]; sat = h[1]; value = h[2]
                                        }) { Text("Сбросить на стандартный") }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        val hex = String.format("#%06X", (android.graphics.Color.HSVToColor(floatArrayOf(hue, sat, value)) and 0xFFFFFF))
                                        onAccentChange(hex)
                                        showAccentDialog = false
                                    }) { Text("OK") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showAccentDialog = false }) { Text("Отмена") }
                                }
                            )
                        }
                    }
                    SettingsSection.FOLDERS -> {
                        Spacer(Modifier.height(4.dp))
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                        ) {
                            items(state.folders) { folder ->
                                val displayName = remember(folder) { folderDisplayNameFromUriString(folder) }
                                val isDefault = folder == "content://com.android.externalstorage.documents/tree/primary%3AMusic"
                                SettingItem(
                                    title = if (isDefault) "$displayName (по умолчанию)" else displayName,
                                    subtitle = if (isDefault) "Папка музыки по умолчанию" else "",
                                    trailing = {
                                        if (!isDefault) {
                                            IconButton(onClick = { onRemoveFolder(folder) }) {
                                                Icon(Icons.Outlined.Delete, contentDescription = "Удалить")
                                            }
                                        }
                                    },
                                    onClick = {}
                                )
                            }
                            item {
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Button(onClick = { folderPicker.launch(null) }) {
                                        Icon(Icons.Outlined.Add, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Добавить папку")
                                    }
                                }
                            }
                        }
                    }
                    SettingsSection.ABOUT -> {
                        Spacer(Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ListItem(
                                leadingContent = { Icon(Icons.Outlined.Info, contentDescription = null) },
                                headlineContent = { Text("AudioPlayer") },
                                supportingContent = { Text("Версия: $versionName") }
                            )
                            ListItem(
                                modifier = Modifier.clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/VladimirPozdnyakov/AudioPlayerAndroid"))
                                    context.startActivity(intent)
                                },
                                leadingContent = { Icon(Icons.Outlined.OpenInNew, contentDescription = null) },
                                headlineContent = { Text("Исходный код") },
                                supportingContent = { Text("github.com/VladimirPozdnyakov/AudioPlayerAndroid") }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingItem(
    title: String,
    subtitle: String,
    trailing: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 10.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            if (trailing != null) trailing()
        }
    }
    HorizontalDivider()
}

@Composable
private fun ThemeOptionRow(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onSelect() }.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Spacer(Modifier.width(12.dp))
        Text(label)
    }
}

@Composable
private fun ColorOptionRow(
    name: String,
    color: Color,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onSelect() }.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Spacer(Modifier.width(12.dp))
        Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(12.dp))
        Text(name)
    }
}

private fun folderDisplayNameFromUriString(folderUri: String): String {
    return try {
        val uri = Uri.parse(folderUri)
        val docId = DocumentsContract.getTreeDocumentId(uri)
        val afterColon = docId.substringAfter(":", docId)
        afterColon.trimEnd('/').substringAfterLast('/')
    } catch (_: Throwable) {
        folderUri
    }
}

@Composable
private fun SaturationValuePanel(
    hue: Float,
    saturation: Float,
    value: Float,
    onChange: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Градиент по s-v при фиксированном hue
    val baseColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f)))
    var panelSize by remember { mutableStateOf(IntSize(0, 0)) }
    Box(modifier = modifier
        .clip(MaterialTheme.shapes.medium)
        .background(
            Brush.horizontalGradient(listOf(Color.White, baseColor))
        )
        .background(
            Brush.verticalGradient(listOf(Color.Transparent, Color.Black))
        )
        .onSizeChanged { panelSize = it }
        .pointerInput(hue) {
            detectDragGestures { change, _ ->
                val size = this.size
                val x = (change.position.x).coerceIn(0f, size.width.toFloat()) / size.width
                val y = (change.position.y).coerceIn(0f, size.height.toFloat()) / size.height
                onChange(x, 1f - y)
            }
        }
        .pointerInput(hue) {
            detectTapGestures { pos ->
                val size = this.size
                val x = (pos.x).coerceIn(0f, size.width.toFloat()) / size.width
                val y = (pos.y).coerceIn(0f, size.height.toFloat()) / size.height
                onChange(x, 1f - y)
            }
        }
    ) {
        // Маркер позиции рисуем на Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = (saturation.coerceIn(0f,1f)) * size.width
            val cy = (1f - value.coerceIn(0f,1f)) * size.height
            val r = 6.dp.toPx()
            drawCircle(color = Color.White, radius = r, center = Offset(cx, cy))
            drawCircle(color = Color.Black, radius = r, center = Offset(cx, cy), style = Stroke(width = 2f))
        }
    }
}

@Composable
private fun HueBar(
    hue: Float,
    onChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Color.Red,
        Color.Yellow,
        Color.Green,
        Color.Cyan,
        Color.Blue,
        Color.Magenta,
        Color.Red
    )
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(Brush.verticalGradient(colors))
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val h = (change.position.y / size.height).coerceIn(0f, 1f) * 360f
                    onChange(h)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { pos ->
                    val h = (pos.y / size.height).coerceIn(0f, 1f) * 360f
                    onChange(h)
                }
            }
    )
}

package com.foxelectronic.audioplayer.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.foxelectronic.audioplayer.AppTheme
import com.foxelectronic.audioplayer.FontType
import com.foxelectronic.audioplayer.SettingsUiState
import com.foxelectronic.audioplayer.ui.settings.colorpicker.ColorPickerDialog
import com.foxelectronic.audioplayer.ui.settings.dialogs.FolderDeletionDialog
import com.foxelectronic.audioplayer.ui.settings.dialogs.FontSelectionDialog
import com.foxelectronic.audioplayer.ui.settings.dialogs.ThemeSelectionDialog
import com.foxelectronic.audioplayer.ui.settings.sections.AboutSection
import com.foxelectronic.audioplayer.ui.settings.sections.FoldersSection
import com.foxelectronic.audioplayer.ui.settings.sections.InterfaceSection
import com.foxelectronic.audioplayer.ui.settings.sections.SettingsMenuSection
import com.foxelectronic.audioplayer.ui.settings.state.SettingsDialogState
import com.foxelectronic.audioplayer.ui.settings.state.SnackbarMessage
import com.foxelectronic.audioplayer.ui.settings.utils.FolderUtils
import com.foxelectronic.audioplayer.ui.settings.utils.VersionUtils
import kotlinx.coroutines.launch

private enum class SettingsSection {
    MENU, INTERFACE, FOLDERS, ABOUT
}

/**
 * Полностью рефакторенный экран настроек
 *
 * Улучшения:
 * - Разбит на модульные компоненты (18 файлов)
 * - Type-safe state management через sealed classes
 * - Новые фичи: подтверждение удаления, snackbar, swipe-to-delete
 * - Material3 best practices
 * - Улучшенная accessibility
 * - Устранено ~150 строк дублирования кода
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onThemeChange: (AppTheme) -> Unit,
    onAccentChange: (String) -> Unit,
    onFontTypeChange: (FontType) -> Unit,
    onAddFolder: (String) -> Unit,
    onRemoveFolder: (String) -> Unit
) {
    val context = LocalContext.current

    // State management - единый sealed class вместо 4 Boolean переменных
    var currentSection by remember { mutableStateOf(SettingsSection.MENU) }
    var dialogState by remember { mutableStateOf<SettingsDialogState>(SettingsDialogState.None) }
    var snackbarMessage by remember { mutableStateOf<SnackbarMessage?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Версия приложения вычисляется один раз
    val versionName = remember { VersionUtils.getVersionName(context) }

    // Folder picker
    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                onAddFolder(it.toString())

                // НОВАЯ ФИЧА: Snackbar при добавлении папки
                val folderName = FolderUtils.getFolderDisplayName(it.toString())
                snackbarMessage = SnackbarMessage.FolderAdded(folderName)
            } catch (e: Throwable) {
                snackbarMessage = SnackbarMessage.Error("Не удалось добавить папку")
            }
        }
    }

    // Обработка Snackbar сообщений
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { message ->
            val text = when (message) {
                is SnackbarMessage.FolderAdded -> "Папка \"${message.folderName}\" добавлена"
                is SnackbarMessage.FolderRemoved -> "Папка \"${message.folderName}\" удалена"
                is SnackbarMessage.Error -> message.message
            }
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = text,
                    duration = SnackbarDuration.Short
                )
                snackbarMessage = null
            }
        }
    }

    // Back navigation
    BackHandler(enabled = currentSection != SettingsSection.MENU) {
        currentSection = SettingsSection.MENU
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (currentSection) {
                            SettingsSection.MENU -> "Настройки"
                            SettingsSection.INTERFACE -> "Интерфейс"
                            SettingsSection.FOLDERS -> "Папки"
                            SettingsSection.ABOUT -> "О приложении"
                        }
                    )
                },
                navigationIcon = {
                    if (currentSection != SettingsSection.MENU) {
                        IconButton(onClick = { currentSection = SettingsSection.MENU }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Назад"
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            AnimatedContent(
                targetState = currentSection,
                label = "settings_sections",
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                }
            ) { section ->
                when (section) {
                    SettingsSection.MENU -> {
                        SettingsMenuSection(
                            onNavigateToInterface = { currentSection = SettingsSection.INTERFACE },
                            onNavigateToFolders = { currentSection = SettingsSection.FOLDERS },
                            onNavigateToAbout = { currentSection = SettingsSection.ABOUT }
                        )
                    }

                    SettingsSection.INTERFACE -> {
                        InterfaceSection(
                            currentTheme = state.theme,
                            currentAccentHex = state.accentHex,
                            currentFont = state.fontType,
                            onThemeClick = {
                                dialogState = SettingsDialogState.ThemeSelection(state.theme)
                            },
                            onAccentClick = {
                                dialogState = SettingsDialogState.AccentSelection(state.accentHex)
                            },
                            onFontClick = {
                                dialogState = SettingsDialogState.FontSelection(state.fontType)
                            }
                        )
                    }

                    SettingsSection.FOLDERS -> {
                        FoldersSection(
                            folders = state.folders,
                            onAddFolder = { folderPicker.launch(null) },
                            onRemoveFolder = { folderUri ->
                                // НОВАЯ ФИЧА: Подтверждение удаления
                                val folderName = FolderUtils.getFolderDisplayName(folderUri)
                                dialogState = SettingsDialogState.FolderDeletion(folderUri, folderName)
                            }
                        )
                    }

                    SettingsSection.ABOUT -> {
                        AboutSection(versionName = versionName)
                    }
                }
            }
        }
    }

    // Диалоги - обработка через when вместо множественных if
    when (val dialog = dialogState) {
        is SettingsDialogState.ThemeSelection -> {
            ThemeSelectionDialog(
                currentTheme = dialog.currentTheme,
                onDismiss = { dialogState = SettingsDialogState.None },
                onConfirm = { theme ->
                    onThemeChange(theme)
                    dialogState = SettingsDialogState.None
                }
            )
        }

        is SettingsDialogState.AccentSelection -> {
            ColorPickerDialog(
                currentAccentHex = dialog.currentAccentHex,
                onDismiss = { dialogState = SettingsDialogState.None },
                onConfirm = { hex ->
                    onAccentChange(hex)
                    dialogState = SettingsDialogState.None
                }
            )
        }

        is SettingsDialogState.FontSelection -> {
            FontSelectionDialog(
                currentFont = dialog.currentFont,
                onDismiss = { dialogState = SettingsDialogState.None },
                onConfirm = { font ->
                    onFontTypeChange(font)
                    dialogState = SettingsDialogState.None
                }
            )
        }

        is SettingsDialogState.FolderDeletion -> {
            FolderDeletionDialog(
                folderName = dialog.folderName,
                onDismiss = { dialogState = SettingsDialogState.None },
                onConfirm = {
                    onRemoveFolder(dialog.folderUri)
                    snackbarMessage = SnackbarMessage.FolderRemoved(dialog.folderName)
                    dialogState = SettingsDialogState.None
                }
            )
        }

        SettingsDialogState.None -> { /* Диалоги скрыты */ }
    }
}

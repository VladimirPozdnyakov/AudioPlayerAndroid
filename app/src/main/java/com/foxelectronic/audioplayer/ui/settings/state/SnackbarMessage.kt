package com.foxelectronic.audioplayer.ui.settings.state

/**
 * Sealed class для типов сообщений Snackbar
 */
sealed class SnackbarMessage {
    /** Папка успешно добавлена */
    data class FolderAdded(val folderName: String) : SnackbarMessage()

    /** Папка успешно удалена */
    data class FolderRemoved(val folderName: String) : SnackbarMessage()

    /** Произошла ошибка */
    data class Error(val message: String) : SnackbarMessage()
}

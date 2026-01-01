package com.foxelectronic.audioplayer.ui.settings.utils

import android.net.Uri
import android.provider.DocumentsContract

/**
 * Утилиты для работы с папками
 */
object FolderUtils {
    const val DEFAULT_MUSIC_FOLDER = "content://com.android.externalstorage.documents/tree/primary%3AMusic"

    /**
     * Получить отображаемое имя папки из URI
     */
    fun getFolderDisplayName(folderUri: String): String {
        return try {
            val uri = Uri.parse(folderUri)
            val docId = DocumentsContract.getTreeDocumentId(uri)
            val afterColon = docId.substringAfter(":", docId)
            afterColon.trimEnd('/').substringAfterLast('/')
        } catch (e: Throwable) {
            folderUri
        }
    }

    /**
     * Проверить является ли папка дефолтной
     */
    fun isDefaultFolder(folderUri: String): Boolean {
        return folderUri == DEFAULT_MUSIC_FOLDER
    }
}

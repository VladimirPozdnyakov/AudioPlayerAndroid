package com.foxelectronic.audioplayer

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

enum class AppTheme { SYSTEM, LIGHT, DARK }

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    companion object {
        private val KEY_THEME = stringPreferencesKey("theme")
        private val KEY_FOLDERS = stringPreferencesKey("folders")
        private val KEY_ACCENT = stringPreferencesKey("accent_hex")
        private val KEY_LAST_PLAYED_TRACK_ID = stringPreferencesKey("last_played_track_id")
        private const val DEFAULT_ACCENT = "#B498FF"
    }

    val themeFlow: Flow<AppTheme> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            when (prefs[KEY_THEME]) {
                AppTheme.LIGHT.name -> AppTheme.LIGHT
                AppTheme.DARK.name -> AppTheme.DARK
                else -> AppTheme.SYSTEM
            }
        }

    val foldersFlow: Flow<List<String>> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            val folders = prefs[KEY_FOLDERS]?.split('|')?.filter { it.isNotBlank() } ?: emptyList()
            // Если папки не настроены, возвращаем папку Music по умолчанию
            if (folders.isEmpty()) {
                listOf("content://com.android.externalstorage.documents/tree/primary%3AMusic")
            } else {
                folders
            }
        }

    val accentFlow: Flow<String> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            prefs[KEY_ACCENT] ?: DEFAULT_ACCENT
        }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME] = theme.name
        }
    }

    suspend fun setFolders(uris: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FOLDERS] = uris.joinToString("|")
        }
    }

    suspend fun setAccent(hex: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACCENT] = hex
        }
    }

    val lastPlayedTrackIdFlow: Flow<String?> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            prefs[KEY_LAST_PLAYED_TRACK_ID]
        }

    suspend fun setLastPlayedTrackId(id: String?) {
        context.dataStore.edit { prefs ->
            if (id != null) {
                prefs[KEY_LAST_PLAYED_TRACK_ID] = id
            } else {
                prefs.remove(KEY_LAST_PLAYED_TRACK_ID)
            }
        }
    }
}

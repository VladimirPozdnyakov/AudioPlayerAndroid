package com.foxelectronic.audioplayer

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.Locale

enum class AppTheme { SYSTEM, LIGHT, DARK }

enum class FontType { SYSTEM, JETBRAINS_MONO }

enum class AppLanguage { ENGLISH, RUSSIAN }

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    companion object {
        private val KEY_THEME = stringPreferencesKey("theme")
        private val KEY_FOLDERS = stringPreferencesKey("folders")
        private val KEY_ACCENT = stringPreferencesKey("accent_hex")
        private val KEY_LAST_PLAYED_TRACK_ID = stringPreferencesKey("last_played_track_id")
        private val KEY_LAST_PLAYED_POSITION = longPreferencesKey("last_played_position")
        private val KEY_FONT_TYPE = stringPreferencesKey("font_type")
        private val KEY_LANGUAGE = stringPreferencesKey("language")
        private val KEY_SELECTED_TAB = intPreferencesKey("selected_tab")
        private const val DEFAULT_ACCENT = "#B498FF"
    }

    // Базовый Flow с обработкой ошибок - используется всеми остальными Flow
    private val preferencesFlow: Flow<Preferences> = context.dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }

    val themeFlow: Flow<AppTheme> = preferencesFlow
        .map { prefs ->
            when (prefs[KEY_THEME]) {
                AppTheme.LIGHT.name -> AppTheme.LIGHT
                AppTheme.DARK.name -> AppTheme.DARK
                else -> AppTheme.SYSTEM
            }
        }

    val foldersFlow: Flow<List<String>> = preferencesFlow
        .map { prefs ->
            val folders = prefs[KEY_FOLDERS]?.split('|')?.filter { it.isNotBlank() } ?: emptyList()
            // Если папки не настроены, возвращаем папку Music по умолчанию
            if (folders.isEmpty()) {
                listOf("content://com.android.externalstorage.documents/tree/primary%3AMusic")
            } else {
                folders
            }
        }

    val accentFlow: Flow<String> = preferencesFlow
        .map { prefs ->
            prefs[KEY_ACCENT] ?: DEFAULT_ACCENT
        }

    val fontTypeFlow: Flow<FontType> = preferencesFlow
        .map { prefs ->
            when (prefs[KEY_FONT_TYPE]) {
                FontType.SYSTEM.name -> FontType.SYSTEM
                else -> FontType.JETBRAINS_MONO  // Default to JetBrains Mono font
            }
        }

    val languageFlow: Flow<AppLanguage> = preferencesFlow
        .map { prefs ->
            when (prefs[KEY_LANGUAGE]) {
                AppLanguage.ENGLISH.name -> AppLanguage.ENGLISH
                AppLanguage.RUSSIAN.name -> AppLanguage.RUSSIAN
                else -> {
                    // По умолчанию определяем язык на основе системной локали
                    val systemLocale = Locale.getDefault().language
                    if (systemLocale == "ru") AppLanguage.RUSSIAN else AppLanguage.ENGLISH
                }
            }
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

    val lastPlayedTrackIdFlow: Flow<String?> = preferencesFlow
        .map { prefs ->
            prefs[KEY_LAST_PLAYED_TRACK_ID]
        }

    val lastPlayedPositionFlow: Flow<Long> = preferencesFlow
        .map { prefs ->
            prefs[KEY_LAST_PLAYED_POSITION] ?: 0L
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

    suspend fun setLastPlayedPosition(position: Long) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_PLAYED_POSITION] = position
        }
    }

    suspend fun setFontType(fontType: FontType) {
        context.dataStore.edit { prefs ->
            prefs[KEY_FONT_TYPE] = fontType.name
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = language.name
        }
    }

    val selectedTabFlow: Flow<Int> = preferencesFlow
        .map { prefs ->
            prefs[KEY_SELECTED_TAB] ?: 0
        }

    suspend fun setSelectedTab(tab: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SELECTED_TAB] = tab
        }
    }
}

package com.foxelectronic.audioplayer.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private val Context.searchHistoryDataStore: DataStore<Preferences> by preferencesDataStore(name = "search_history")

class SearchHistoryRepository(private val context: Context) {

    private val searchHistoryKey = stringPreferencesKey("history_list")
    private val maxHistorySize = 20

    /**
     * Получить историю поиска как Flow для реактивности
     */
    fun getSearchHistory(): Flow<List<SearchHistoryItem>> {
        return context.searchHistoryDataStore.data.map { preferences ->
            val json = Json {
                encodeDefaults = true
                ignoreUnknownKeys = true
            }
            val serializedHistory = preferences[searchHistoryKey] ?: return@map emptyList()

            try {
                val history = json.decodeFromString<List<SearchHistoryItem>>(serializedHistory)
                // Сортировка по убыванию timestamp (новые сверху)
                history.sortedByDescending { it.timestamp }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    /**
     * Добавить поисковый запрос в историю
     * - Удаляет дубликаты (если запрос уже есть, обновляет timestamp)
     * - Ограничивает размер до maxHistorySize элементов
     */
    suspend fun addSearchQuery(query: String) {
        if (query.isBlank() || query.length < 2) return

        context.searchHistoryDataStore.edit { preferences ->
            val json = Json {
                encodeDefaults = true
                ignoreUnknownKeys = true
            }

            // Получить текущую историю
            val currentHistory = try {
                val serialized = preferences[searchHistoryKey] ?: "[]"
                json.decodeFromString<List<SearchHistoryItem>>(serialized).toMutableList()
            } catch (e: Exception) {
                e.printStackTrace()
                mutableListOf()
            }

            // Удалить существующий запрос, если есть (чтобы избежать дубликатов)
            currentHistory.removeAll { it.query.equals(query, ignoreCase = true) }

            // Добавить новый запрос в начало
            val newItem = SearchHistoryItem(query = query, timestamp = System.currentTimeMillis())
            currentHistory.add(0, newItem)

            // Ограничить размер до maxHistorySize
            val limitedHistory = currentHistory.take(maxHistorySize)

            // Сохранить обновленную историю
            val serializedHistory = json.encodeToString(limitedHistory)
            preferences[searchHistoryKey] = serializedHistory
        }
    }

    /**
     * Удалить конкретный запрос из истории
     */
    suspend fun removeSearchQuery(query: String) {
        context.searchHistoryDataStore.edit { preferences ->
            val json = Json {
                encodeDefaults = true
                ignoreUnknownKeys = true
            }

            val currentHistory = try {
                val serialized = preferences[searchHistoryKey] ?: return@edit
                json.decodeFromString<List<SearchHistoryItem>>(serialized).toMutableList()
            } catch (e: Exception) {
                e.printStackTrace()
                return@edit
            }

            // Удалить запрос
            currentHistory.removeAll { it.query == query }

            // Сохранить обновленную историю
            if (currentHistory.isEmpty()) {
                preferences.remove(searchHistoryKey)
            } else {
                val serializedHistory = json.encodeToString(currentHistory)
                preferences[searchHistoryKey] = serializedHistory
            }
        }
    }

    /**
     * Очистить всю историю поиска
     */
    suspend fun clearAllHistory() {
        context.searchHistoryDataStore.edit { preferences ->
            preferences.remove(searchHistoryKey)
        }
    }
}

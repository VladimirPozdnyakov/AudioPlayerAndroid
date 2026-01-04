package com.foxelectronic.audioplayer.repository

import kotlinx.serialization.Serializable

/**
 * Элемент истории поиска
 * @param query Поисковый запрос
 * @param timestamp Время создания запроса в миллисекундах
 */
@Serializable
data class SearchHistoryItem(
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)

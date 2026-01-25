package com.foxelectronic.audioplayer.update

/**
 * Утилита для сравнения версий приложения
 */
object VersionComparator {
    /**
     * Сравнивает две версии приложения
     *
     * @param current Текущая версия
     * @param latest Последняя версия
     * @return -1 если current < latest, 0 если равны, 1 если current > latest
     */
    fun compareVersions(current: String, latest: String): Int {
        // Нормализуем версии: убираем префикс 'v' и приводим к lowercase
        val normalizedCurrent = normalizeVersion(current)
        val normalizedLatest = normalizeVersion(latest)

        // Если версии идентичны после нормализации
        if (normalizedCurrent == normalizedLatest) {
            return 0
        }

        // Разделяем версию на числовую часть и суффикс
        val currentParts = parseVersion(normalizedCurrent)
        val latestParts = parseVersion(normalizedLatest)

        // Сравниваем числовые части
        val numericComparison = compareNumericParts(
            currentParts.numericParts,
            latestParts.numericParts
        )

        // Если числовые части разные, возвращаем результат
        if (numericComparison != 0) {
            return numericComparison
        }

        // Если числовые части равны, сравниваем суффиксы
        return compareSuffixes(currentParts.suffix, latestParts.suffix)
    }

    /**
     * Нормализует строку версии: удаляет префикс 'v', приводит к lowercase
     */
    private fun normalizeVersion(version: String): String {
        return version.trim().lowercase().removePrefix("v")
    }

    /**
     * Разбирает версию на числовую часть и суффикс
     */
    private fun parseVersion(version: String): VersionParts {
        // Ищем первый символ, который не является цифрой или точкой
        val suffixIndex = version.indexOfFirst { !it.isDigit() && it != '.' }

        return if (suffixIndex == -1) {
            // Нет суффикса
            VersionParts(version, "")
        } else {
            // Разделяем на числовую часть и суффикс
            val numericPart = version.substring(0, suffixIndex)
            val suffix = version.substring(suffixIndex)
            VersionParts(numericPart, suffix)
        }
    }

    /**
     * Сравнивает числовые части версий
     */
    private fun compareNumericParts(current: String, latest: String): Int {
        // Разбиваем на компоненты по точке
        val currentComponents = current.split('.').mapNotNull { it.toIntOrNull() }
        val latestComponents = latest.split('.').mapNotNull { it.toIntOrNull() }

        // Сравниваем компоненты по порядку
        val maxLength = maxOf(currentComponents.size, latestComponents.size)
        for (i in 0 until maxLength) {
            val currentValue = currentComponents.getOrNull(i) ?: 0
            val latestValue = latestComponents.getOrNull(i) ?: 0

            when {
                currentValue < latestValue -> return -1
                currentValue > latestValue -> return 1
            }
        }

        return 0
    }

    /**
     * Сравнивает суффиксы версий
     * Приоритет: release (без суффикса) > rc > beta > b > alpha
     */
    private fun compareSuffixes(current: String, latest: String): Int {
        // Если оба без суффикса, они равны
        if (current.isEmpty() && latest.isEmpty()) {
            return 0
        }

        // Версия без суффикса считается выше версии с суффиксом
        if (current.isEmpty()) return 1
        if (latest.isEmpty()) return -1

        // Получаем приоритет суффиксов
        val currentPriority = getSuffixPriority(current)
        val latestPriority = getSuffixPriority(latest)

        return when {
            currentPriority < latestPriority -> -1
            currentPriority > latestPriority -> 1
            else -> 0
        }
    }

    /**
     * Определяет приоритет суффикса
     * Чем выше число, тем выше приоритет
     */
    private fun getSuffixPriority(suffix: String): Int {
        val normalizedSuffix = suffix.trim().lowercase().removePrefix("-")

        return when {
            normalizedSuffix.isEmpty() -> 100 // release
            normalizedSuffix.startsWith("rc") -> 80
            normalizedSuffix.startsWith("beta") -> 60
            normalizedSuffix.startsWith("b") && !normalizedSuffix.startsWith("beta") -> 50
            normalizedSuffix.startsWith("alpha") -> 40
            else -> 30 // неизвестный суффикс имеет самый низкий приоритет
        }
    }

    /**
     * Вспомогательный класс для хранения разобранной версии
     */
    private data class VersionParts(
        val numericParts: String,
        val suffix: String
    )
}

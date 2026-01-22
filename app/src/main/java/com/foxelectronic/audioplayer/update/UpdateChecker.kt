package com.foxelectronic.audioplayer.update

import android.util.Log
import com.foxelectronic.audioplayer.network.GitHubApi
import com.foxelectronic.audioplayer.network.GitHubRelease
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Сервис для проверки наличия обновлений приложения через GitHub Releases API
 */
@Singleton
class UpdateChecker @Inject constructor(
    private val gitHubApi: GitHubApi
) {
    companion object {
        private const val TAG = "UpdateChecker"
    }

    /**
     * Проверяет наличие новой версии приложения
     *
     * @param currentVersion Текущая версия приложения (например, "0.11b")
     * @param owner Владелец репозитория на GitHub
     * @param repo Название репозитория на GitHub
     * @return GitHubRelease если доступна новая версия, null если обновление не требуется или произошла ошибка
     */
    suspend fun checkForUpdate(
        currentVersion: String,
        owner: String,
        repo: String
    ): GitHubRelease? {
        return try {
            Log.d(TAG, "Начинаем проверку обновлений для репозитория: $owner/$repo")
            Log.d(TAG, "Текущая версия приложения: $currentVersion")

            // Получаем информацию о последнем релизе из GitHub API
            Log.d(TAG, "Выполняем запрос к GitHub API...")
            val latestRelease = gitHubApi.getLatestRelease(owner, repo)

            // Извлекаем версию из tag_name
            val latestVersion = latestRelease.tagName

            Log.d(TAG, "Получена последняя версия с GitHub: $latestVersion")

            // Сравниваем версии
            val comparisonResult = VersionComparator.compareVersions(currentVersion, latestVersion)
            Log.d(TAG, "Результат сравнения версий: $comparisonResult (0: равны, -1: текущая меньше, 1: текущая больше)")

            // Если текущая версия меньше последней (comparisonResult == -1), возвращаем релиз
            if (comparisonResult < 0) {
                Log.i(TAG, "Доступна новая версия: $latestVersion")
                latestRelease
            } else {
                Log.d(TAG, "Обновление не требуется")
                null
            }
        } catch (e: Exception) {
            // Ошибки сети или API обрабатываются молча - не показываем пользователю
            Log.e(TAG, "Ошибка при проверке обновлений: ${e.message}", e)
            Log.e(TAG, "Подробная информация об ошибке:", e)
            null
        }
    }
}

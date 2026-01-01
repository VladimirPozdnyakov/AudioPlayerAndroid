package com.foxelectronic.audioplayer.ui.settings.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 * Утилиты для получения информации о версии приложения
 */
object VersionUtils {
    /**
     * Получить версию приложения
     */
    fun getVersionName(context: Context): String {
        return try {
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
        } catch (e: Throwable) {
            "-"
        }
    }
}

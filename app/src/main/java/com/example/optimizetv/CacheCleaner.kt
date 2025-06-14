package com.example.optimizetv.utils

import android.content.Context
import android.util.Log
import java.io.File

object CacheCleaner {

    // Очистка кэша всех приложений
    fun clearAllAppCaches(context: Context): String {
        if (!RootUtils.isRootAvailable()) {
            Log.e("CacheCleaner", "Root-доступ недоступен")
            return "Ошибка: Root-доступ не найден"
        }

        val pm = context.packageManager
        val packages = try {
            pm.getInstalledPackages(0)
        } catch (e: Exception) {
            Log.e("CacheCleaner", "Не удалось получить список пакетов", e)
            return "Ошибка получения списка пакетов"
        }

        val output = StringBuilder("Кэш очищен:\n")

        for (packageInfo in packages) {
            val packageName = packageInfo.packageName
            val cacheDir = File("/data/data/$packageName/cache")

            if (cacheDir.exists() && cacheDir.isDirectory) {
                try {
                    // Выполняем команду через root
                    val result = RootUtils.executeCommand(
                        "rm -rf ${cacheDir.absolutePath}",
                        "mkdir -p ${cacheDir.absolutePath}"
                    )
                    output.append("$packageName\n")
                } catch (e: Exception) {
                    Log.e("CacheCleaner", "Не удалось очистить кэш для $packageName", e)
                }
            }
        }

        return output.toString()
    }

    // Очистка только одного кэша по имени пакета
    fun clearSingleAppCache(context: Context, packageName: String): String {
        if (!RootUtils.isRootAvailable()) {
            return "Root-доступ недоступен"
        }

        val cacheDir = "/data/data/$packageName/cache"

        return RootUtils.executeCommand(
            "rm -rf $cacheDir",
            "mkdir -p $cacheDir"
        )
    }

    // Очистка системного кэша
    fun clearSystemCache(): String {
        return RootUtils.executeCommand(
            "echo 3 > /proc/sys/vm/drop_caches",
            "pm trim-caches 1073741824"
        )
    }
}
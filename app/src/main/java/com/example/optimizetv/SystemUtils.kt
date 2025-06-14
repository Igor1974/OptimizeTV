package com.example.optimizetv.utils

import android.content.Context
import android.app.ActivityManager
import android.os.Build
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.InputStreamReader
import java.util.*

object SystemUtils {

    // 🔹 Получить температуру CPU (в градусах)
    fun getCpuTemperature(): String {
        return try {
            val process = Runtime.getRuntime().exec("su -c cat /sys/class/thermal/thermal_zone0/temp")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            val output = StringBuilder()

            while (reader.readLine().also { line = it } != null) {
                line?.let { output.append(it).append("\n") }
            }

            val temp = output.toString().trim()
            if (temp.isNotEmpty()) {
                val celsius = temp.toFloat() / 1000
                "Температура CPU: ${String.format("%.2f°C", celsius)}"
            } else {
                "Не удалось получить температуру"
            }
        } catch (e: Exception) {
            "Ошибка: ${e.message}"
        }
    }

    // 🔹 Более безопасное получение температуры CPU
    fun getCpuTemperatureSafe(): String {
        return try {
            val file = File("/sys/class/thermal/thermal_zone0/temp")
            if (file.exists()) {
                val temp = file.readText().trim()
                val celsius = temp.toFloat() / 1000
                "Температура CPU: ${String.format("%.2f°C", celsius)}"
            } else {
                "Файл не найден"
            }
        } catch (e: Exception) {
            "Ошибка: ${e.message}"
        }
    }

    // 🔹 Получить информацию о памяти
    fun getMemoryUsage(context: Context): String {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)

        val totalMem = formatBytes(memInfo.totalMem)
        val availMem = formatBytes(memInfo.availMem)
        val lowMemory = if (memInfo.lowMemory) "Да" else "Нет"

        return """
            Всего памяти: $totalMem
            Свободно: $availMem
            Низкая память: $lowMemory
        """.trimIndent()
    }

    // 🔹 Получить использование CPU (через /proc/stat)
    fun getCpuUsage(): String {
        return try {
            val first = readCpuStatLine()
            val second = readCpuStatLine(delay = 500)

            if (first[0] == "cpu" && second[0] == "cpu") {
                val prevIdle = first[4].toLong() + first[5].toLong()
                val prevTotal = first.drop(1).map(String::toLong).sum()
                val currIdle = second[4].toLong() + second[5].toLong()
                val currTotal = second.drop(1).map(String::toLong).sum()

                val deltaIdle = currIdle - prevIdle
                val deltaTotal = currTotal - prevTotal
                val usage = ((deltaTotal - deltaIdle) * 100.0 / deltaTotal).toFloat()

                "Использование CPU: ${String.format("%.2f%%", usage)}"
            } else {
                "Ошибка чтения /proc/stat"
            }
        } catch (e: Exception) {
            "Ошибка: ${e.message}"
        }
    }

    private fun readCpuStatLine(delay: Long = 0): List<String> {
        val reader = BufferedReader(FileReader("/proc/stat"))
        val line = reader.readLine()
        reader.close()
        if (delay > 0) Thread.sleep(delay)
        return line.split(" ").filter { it.isNotBlank() }
    }

    // 🔹 Получить список запущенных процессов
    fun getRunningProcesses(context: Context): String {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val processes = am.runningAppProcesses ?: return "Процессы не доступны"

        val result = StringBuilder("Запущенные процессы:\n")

        for (process in processes.take(20)) {
            result.append("${process.processName} → ${process.importance}\n")
        }

        return result.toString()
    }

    // 🔹 Форматирование байтов в человекочитаемый формат
    fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }

    // 🔹 Получить объём свободной и общей RAM
    fun getRamUsage(context: Context): String {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)

        val total = formatBytes(mi.totalMem)
        val free = formatBytes(mi.availMem)
        val threshold = formatBytes(mi.threshold)

        return """
            Объём RAM: $total
            Доступно: $free
            Порог низкой памяти: $threshold
        """.trimIndent()
    }

    // 🔹 Получить версию Android
    fun getAndroidVersion(): String {
        return "Версия Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    }

    // 🔹 Получить модель устройства
    fun getDeviceModel(): String {
        return "Модель: ${Build.MANUFACTURER} ${Build.MODEL}"
    }

    // 🔹 Получить всю информацию о системе
    fun getFullSystemInfo(context: Context): String {
        val cpuTemp = getCpuTemperatureSafe()
        val ramUsage = getRamUsage(context)
        val cpuUsage = getCpuUsage()
        val androidVer = getAndroidVersion()
        val deviceModel = getDeviceModel()

        return """
            $androidVer
            $deviceModel
            
            $cpuTemp
            $ramUsage
            
            $cpuUsage
        """.trimIndent()
    }

    // 🔹 Получить данные для графика использования CPU (для LineChart)
    @Throws(Exception::class)
    fun getCpuUsageForChart(): Pair<Float, Float> {
        val first = readCpuStatLine()
        val second = readCpuStatLine(delay = 500)

        if (first[0] == "cpu" && second[0] == "cpu") {
            val prevIdle = first[4].toLong() + first[5].toLong()
            val prevTotal = first.drop(1).map(String::toLong).sum()
            val currIdle = second[4].toLong() + second[5].toLong()
            val currTotal = second.drop(1).map(String::toLong).sum()

            val deltaIdle = currIdle - prevIdle
            val deltaTotal = currTotal - prevTotal
            val usage = ((deltaTotal - deltaIdle) * 100.0 / deltaTotal).toFloat()

            return Pair(currTotal.toFloat(), usage)
        }
        throw Exception("Не удалось считать данные из /proc/stat")
    }

    // 🔹 Получить данные о доступной памяти (для графика)
    fun getFreeRamForChart(context: Context): Float {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        return (mi.availMem.toFloat() / mi.totalMem.toFloat()) * 100f
    }

    // 🔹 Получить все данные для диаграммы (например, для LineChart)
    fun getChartData(context: Context): Map<String, Any> {
        val cpuLoad = try {
            val data = getCpuUsageForChart()
            data.first
        } catch (e: Exception) {
            0f
        }

        val memFree = getFreeRamForChart(context)

        return mapOf(
            "cpu_usage" to cpuLoad,
            "mem_free_percent" to memFree
        )
    }
}

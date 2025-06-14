package com.example.optimizetv.utils

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

object RootUtils {

    // Проверка root-доступа
    fun isRootAvailable(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su -v")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            reader.readLine() != null
        } catch (e: Exception) {
            false
        }
    }

    // Выполнение команд через root
    fun executeCommand(vararg commands: String): String {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val stdin = process.outputStream
            val stdout = process.inputStream
            val stderr = process.errorStream

            for (cmd in commands) {
                stdin.write("$cmd\n".toByteArray())
            }
            stdin.write("exit\n".toByteArray())
            stdin.flush()
            stdin.close()

            val output = StringBuilder()
            var line: String?
            val reader = BufferedReader(InputStreamReader(stdout))
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }

            val errorOutput = StringBuilder()
            var errorLine: String?
            val errorReader = BufferedReader(InputStreamReader(stderr))
            while (errorReader.readLine().also { errorLine = it } != null) {
                errorOutput.append(errorLine).append("\n")
            }

            if (errorOutput.isNotEmpty()) {
                output.append("\n[ERROR]\n").append(errorOutput)
            }

            output.toString().trim()
        } catch (e: Exception) {
            "Ошибка выполнения: ${e.message}"
        }
    }

    // Выполнение команды с выводом по строкам
    fun executeCommandWithOutput(command: String, callback: (String) -> Unit) {
        Thread {
            if (!isRootAvailable()) {
                callback.invoke("Root недоступен")
                return@Thread
            }

            try {
                val process = Runtime.getRuntime().exec("su")

                val stdin = process.outputStream
                val stdout = process.inputStream
                val stderr = process.errorStream

                stdin.write("$command\n".toByteArray())
                stdin.write("exit\n".toByteArray())
                stdin.flush()
                stdin.close()

                readStream(stdout, callback)
                readStream(stderr) { line -> callback.invoke("[ERROR] $line") }

            } catch (e: Exception) {
                callback.invoke("Ошибка выполнения: ${e.message}")
            }
        }.start()
    }

    private fun readStream(inputStream: java.io.InputStream, callback: (String) -> Unit) {
        val reader = BufferedReader(InputStreamReader(inputStream))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            line?.let(callback)
        }
    }

    // Перезагрузка системы
    fun rebootSystem() {
        executeCommand("reboot")
    }
}
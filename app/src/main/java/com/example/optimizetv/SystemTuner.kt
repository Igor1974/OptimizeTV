package com.example.optimizetv

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.optimizetv.utils.RootUtils
import java.io.File

object SystemTuner {

    fun disableSELinux(): String {
        if (!RootUtils.isRootAvailable()) return "Root недоступен"
        return RootUtils.executeCommand("setenforce 0")
    }

    // 1. Отключение анимаций
    fun disableAnimations(): String {
        if (!RootUtils.isRootAvailable()) return "Root недоступен"

        val commands = """
            settings put global window_animation_scale 0
            settings put global transition_animation_scale 0
            settings put global animator_duration_scale 0
        """

        return RootUtils.executeCommand(commands)
    }

    // 2. Оптимизация JobScheduler
    fun optimizeJobScheduler(): String {
        if (!RootUtils.isRootAvailable()) return "Root недоступен"

        val commands = """
        settings put global job_scheduler_constants fg_job_count=3,bg_normal_job_count=1,bg_moderate_job_count=1
        settings put global job_scheduler_quota_controller_constants max_job_count_per_rate_limiting_window=5,rate_limiting_window_ms=60000
        settings put global job_scheduler_time_controller_constants min_idle_reschedule_ms=30000,min_ready_non_active_reschedule_ms=120000
    """.trimIndent()

        return RootUtils.executeCommand(commands)
    }

    // 3. Остановка фоновых процессов
    fun stopBackgroundServices(): String {
        if (!RootUtils.isRootAvailable()) return "Root недоступен"

        val commands = """
            am kill-all
            killall -9 system_server
        """

        return RootUtils.executeCommand(commands)
    }

    // 4. Снижение энергопотребления
    fun reduceWakeLocks(): String {
        if (!RootUtils.isRootAvailable()) return "Root недоступен"

        val commands = """
            dumpsys power | grep 'Wake Lock Summary'
            settings put system screen_off_timeout 30000
        """

        return RootUtils.executeCommand(commands)
    }

    // 5. Очистка кэша всех приложений
    fun clearAllAppCaches(context: Context): String {
        if (!RootUtils.isRootAvailable()) return "Root недоступен"

        val pm = context.packageManager
        val packages = try {
            pm.getInstalledPackages(0)
        } catch (e: Exception) {
            Log.e("SystemTuner", "Не удалось получить список пакетов", e)
            return "Ошибка получения списка пакетов"
        }

        val output = StringBuilder("Очищено:\n")

        for (packageInfo in packages) {
            val packageName = packageInfo.packageName
            val cacheDir = "/data/data/$packageName/cache"

            if (File("/data/data/$packageName").exists()) {
                try {
                    val result = RootUtils.executeCommand("rm -rf $cacheDir")
                    output.append("$packageName\n")
                } catch (e: Exception) {
                    Log.e("SystemTuner", "Не удалось очистить кэш для $packageName", e)
                }
            }
        }

        return output.toString()
    }

    // 6. Отключение системы пользователей
    fun disableUserSwitcher(): String {
        if (!RootUtils.isRootAvailable()) return "Root недоступен"
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return "API < O не поддерживается"

        return RootUtils.executeCommand("settings put global user_switcher_enabled 0")
    }

    // 7. Отключение MultiUser
    fun disableMultiUserSupport(): String {
        if (!RootUtils.isRootAvailable()) return "Root недоступен"

        // Альтернатива device_config через sysprop
        return RootUtils.executeCommand(
            "setprop persist.sys.disable_multiuser true",
            "stop user_initiated_0",
            "stop user_unlocked_0"
        )
    }

    // 8. Блокировка управления пользователями
    fun blockUserManagement(): String {
        if (!RootUtils.isRootAvailable()) return "Root недоступен"

        return RootUtils.executeCommand(
            "pm disable-user --user 0 com.android.settings/.user.UserSettings"
        )
    }

    // 14. Включить заморозку фоновых процессов
    fun enableCachedAppsFreezer(): String {
        if (!RootUtils.isRootAvailable()) return "Root недоступен"

        return RootUtils.executeCommand("settings put global cached_apps_freezer enabled")
    }

    // 15. Отключить заморозку
    fun disableCachedAppsFreezer(): String {
        if (!RootUtils.isRootAvailable()) return "Root недоступен"

        return RootUtils.executeCommand("settings put global cached_apps_freezer disabled")
    }

    // 16. Принудительная компиляция всех приложений
    fun forceAppCompilationWithLog(): String {
        if (!RootUtils.isRootAvailable()) return "Root недоступен"

        val commands = """
            pm compile -a -f --check-prof false -m everything
            pm compile -a -f --check-prof false --compile-layouts
            pm bg-dexopt-job
        """.trimIndent()

        return RootUtils.executeCommand(commands)
    }

    // 12. Компиляция всего
    fun compileEverything() =
        if (RootUtils.isRootAvailable())
            RootUtils.executeCommand("pm compile -a -f --check-prof false -m everything")
        else
            "Root недоступен"

    // 13. Компиляция layout'ов
    fun compileLayouts() =
        if (RootUtils.isRootAvailable())
            RootUtils.executeCommand("pm compile -a -f --check-prof false --compile-layouts")
        else
            "Root недоступен"

    // 14. Фоновая оптимизация DEX
    fun runBackgroundDexOpt() =
        if (RootUtils.isRootAvailable())
            RootUtils.executeCommand("pm bg-dexopt-job")
        else
            "Root недоступен"

    // 15. Ускорение UI
    fun enableThreadedRenderer(): String {
        if (!RootUtils.isRootAvailable()) return "Root недоступен"

        val commands = """
            setprop debug.hwui.renderer Threaded
            setprop debug.sf.enable_gl_backpressure 1
        """.trimIndent()

        return RootUtils.executeCommand(commands)
    }

    // 16. Ограничение CPU
    fun limitCpuUsage(): String {
        if (!RootUtils.isRootAvailable()) return "Root недоступен"

        // Проверяем доступность governor
        val governorExists = File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor").exists()
        if (!governorExists) return "CPU Governor не найден"

        val commands = """
            echo "interactive" > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor
            echo "1" > /proc/sys/vm/swappiness
        """.trimIndent()

        return RootUtils.executeCommand(commands)
    }

    // 17. Полная оптимизация системы
    fun fullSystemOptimization(): String {
        if (!RootUtils.isRootAvailable()) return "Root недоступен"

        val commands = """
            # Анимации
            settings put global window_animation_scale 0
            settings put global transition_animation_scale 0
            settings put global animator_duration_scale 0
            
            # JobScheduler
            settings put global job_scheduler_constants fg_job_count=3,bg_normal_job_count=1,bg_moderate_job_count=1
            settings put global job_scheduler_quota_controller_constants max_job_count_per_rate_limiting_window=5,rate_limiting_window_ms=60000
            settings put global job_scheduler_time_controller_constants min_idle_reschedule_ms=30000
        """.trimIndent()

        return RootUtils.executeCommand(commands)
    }

    // 18. Настройка DNS
    fun setDnsToGoogle(): String {
        if (!RootUtils.isRootAvailable()) return "Root недоступен"

        return RootUtils.executeCommand(
            "setprop net.dns1 8.8.8.8",
            "setprop net.dns2 8.8.4.4"
        )
    }

    // 19. Полная сетевая оптимизация
    fun fullNetworkOptimization(): String {
        if (!RootUtils.isRootAvailable()) return "Root недоступен"

        val commands = """
            # DNS
            setprop net.dns1 8.8.8.8
            setprop net.dns2 8.8.4.4
            
            # TCP Optimizations
            echo 3 > /proc/sys/net/ipv4/tcp_fastopen
            echo 1 > /proc/sys/net/ipv4/tcp_tw_reuse
            echo 1 > /proc/sys/net/ipv4/tcp_low_latency
        """.trimIndent()

        return RootUtils.executeCommand(commands)
    }
}
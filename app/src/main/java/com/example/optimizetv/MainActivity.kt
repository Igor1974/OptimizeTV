package com.example.optimizetv

import com.example.optimizetv.utils.*
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.optimizetv.databinding.ActivityMainBinding
import com.github.mikephil.charting.data.Entry
import com.example.optimizetv.utils.RootUtils
import java.util.*

class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    @SuppressLint("SetTextI18n", "MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupClickListeners() {
        binding.btnSystemInfo.setOnClickListener {
            if (RootUtils.isRootAvailable()) {
                val info = SystemUtils.getFullSystemInfo(this)
                binding.tvLog.text = info

                try {
                    val chartData = SystemUtils.getChartData(this)
                    updateSystemChart(chartData)
                } catch (e: Exception) {
                    Toast.makeText(this, "Ошибка графика", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Root недоступен", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnClearAllCache.setOnClickListener {
            if (RootUtils.isRootAvailable()) {
                CacheCleaner.clearAllAppCaches(this)
                Toast.makeText(this, "Весь кэш очищен", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Root не доступен", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnKillBackgroundProcesses.setOnClickListener {
            if (RootUtils.isRootAvailable()) {
                RootUtils.executeCommandWithOutput("am kill-all") { line ->
                    handler.post {
                        binding.tvLog.append("\n> $line")
                    }
                }
                Toast.makeText(this, "Фоновые процессы остановлены", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Root не доступен", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnReboot.setOnClickListener {
            if (RootUtils.isRootAvailable()) {
                if (BuildConfig.DEBUG) Log.d("MainActivity", "Перезагрузка системы")
                RootUtils.rebootSystem()
            } else {
                Toast.makeText(this, "Root недоступен", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnRefreshGraphs.setOnClickListener {
            if (RootUtils.isRootAvailable()) {
                try {
                    val chartData = SystemUtils.getChartData(this)
                    updateSystemChart(chartData)
                } catch (e: Exception) {
                    Toast.makeText(this, "Ошибка графика", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Root недоступен", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        binding.btnCompileApps.setOnClickListener {
            if (RootUtils.isRootAvailable()) {
                binding.progressBar.visibility = View.VISIBLE
                binding.tvLog.text = "Начинаем компиляцию в фоне...\n"

                Thread {
                    val commands = """
                        pm compile -a -f --check-prof false -m everything
                        pm bg-dexopt-job
                    """.trimIndent()

                    val result = RootUtils.executeCommand(commands)
                    handler.post {
                        binding.tvLog.append("\nRESULT:\n$result")
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this@MainActivity, "Компиляция завершена", Toast.LENGTH_SHORT).show()
                    }
                }.start()
            } else {
                Toast.makeText(this, "Root недоступен", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnOptimizeJobscheduler.setOnClickListener {
            if (RootUtils.isRootAvailable()) {
                val result = SystemTuner.optimizeJobScheduler()
                binding.tvLog.append("\nRESULT:\n$result")
                Toast.makeText(this, "Планировщик оптимизирован", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Root недоступен", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDisableUsers.setOnClickListener {
            if (RootUtils.isRootAvailable()) {
                val result1 = SystemTuner.disableUserSwitcher()
                val result2 = SystemTuner.disableMultiUserSupport()
                binding.tvLog.append("\nRESULT:\n$result1\n$result2")
                Toast.makeText(this, "Создание пользователей заблокировано", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Root недоступен", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnToggleFreezer.setOnClickListener {
            if (RootUtils.isRootAvailable()) {
                val currentState = isFreezerEnabled()
                val newState = !currentState

                Thread {
                    toggleFreezer(newState)
                    handler.post {
                        binding.btnToggleFreezer.text = if (newState) "Отключить заморозку" else "Включить заморозку"
                        Toast.makeText(this@MainActivity, if (newState) "Заморозка включена" else "Заморозка отключена", Toast.LENGTH_SHORT).show()
                    }
                }.start()
            } else {
                Toast.makeText(this, "Root недоступен", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnOptimizeNetwork.setOnClickListener {
            if (RootUtils.isRootAvailable()) {
                val result = NetworkTuner.fullNetworkOptimization()
                binding.tvLog.append("\nRESULT:\n$result")
                Toast.makeText(this, "Сеть оптимизирована", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Root недоступен", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Проверка состояния заморозки
    private fun isFreezerEnabled(): Boolean {
        val output = RootUtils.executeCommand(
            "settings get global cached_apps_freezer",
            "setprop net.dns2 8.8.4.4"
        )
        return output.contains("enabled", ignoreCase = true)
    }

    // Включение/отключение заморозки
    private fun toggleFreezer(enable: Boolean): String {
        return if (enable) {
            SystemTuner.enableCachedAppsFreezer()
        } else {
            SystemTuner.disableCachedAppsFreezer()
        }
    }

    // Обновление графиков
    private fun updateSystemChart(data: Map<String, Any>) {
        val cpuUsage = data["cpu_usage"] as? Float ?: 0f
        val memFree = data["mem_free_percent"] as? Float ?: 0f

        val entry1 = Entry(binding.chartCpuTemp.data?.getDataSetByIndex(0)?.entryCount?.toFloat() ?: 0f, cpuUsage)
        val entry2 = Entry(binding.chartCpuFreq.data?.getDataSetByIndex(0)?.entryCount?.toFloat() ?: 0f, memFree)

        binding.chartCpuTemp.data?.addEntry(entry1, 0)
        binding.chartCpuTemp.data?.notifyDataChanged()
        binding.chartCpuTemp.notifyDataSetChanged()
        binding.chartCpuTemp.invalidate()

        binding.chartCpuFreq.data?.addEntry(entry2, 0)
        binding.chartCpuFreq.data?.notifyDataChanged()
        binding.chartCpuFreq.notifyDataSetChanged()
        binding.chartCpuFreq.invalidate()
    }
}

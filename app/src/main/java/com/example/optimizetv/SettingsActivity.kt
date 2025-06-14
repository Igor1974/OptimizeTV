package com.example.optimizetv

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.example.optimizetv.R.id.container

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Замена контейнера на фрагмент настроек
        supportFragmentManager
            .beginTransaction()
            .replace(container, SettingsFragment())
            .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
                when (key) {
                    "pref_disable_animations" -> {
                        val enabled = sharedPreferences.getBoolean(key, false)
                        if (enabled) SystemTuner.disableAnimations()
                    }
                    "pref_optimize_jobscheduler" -> {
                        val enabled = sharedPreferences.getBoolean(key, false)
                        if (enabled) SystemTuner.optimizeJobScheduler()
                    }
                    "pref_reduce_wake_locks" -> {
                        val enabled = sharedPreferences.getBoolean(key, false)
                        if (enabled) SystemTuner.reduceWakeLocks()
                    }
                }
            }
        }
    }
}
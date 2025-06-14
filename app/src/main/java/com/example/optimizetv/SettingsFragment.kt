package com.example.optimizetv

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.preference.PreferenceFragmentCompat
import com.example.optimizetv.SystemTuner.disableAnimations

class SettingsFragment() : PreferenceFragmentCompat(), Parcelable {

    constructor(parcel: Parcel) : this() {
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        // Обработка изменений
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                "pref_disable_animations" -> {
                    val enabled = sharedPreferences.getBoolean(key, false)
                    if (enabled) disableAnimations()
                }

                "pref_optimize_jobscheduler" -> {
                    val enabled = sharedPreferences.getBoolean(key, false)
                    if (enabled) SystemTuner.optimizeJobScheduler()
                }

                "pref_disable_background_services" -> {
                    val enabled = sharedPreferences.getBoolean(key, false)
                    if (enabled) SystemTuner.stopBackgroundServices()
                }

                "pref_reduce_wake_locks" -> {
                    val enabled = sharedPreferences.getBoolean(key, false)
                    if (enabled) SystemTuner.reduceWakeLocks()
                }

                "pref_enable_freezer" -> {
                    val enabled = sharedPreferences.getBoolean(key, false)
                    if (enabled) {
                        SystemTuner.enableCachedAppsFreezer()
                    } else {
                        SystemTuner.disableCachedAppsFreezer()
                    }
                }

                "pref_enable_tcp_fastopen" -> {
                    if (sharedPreferences.getBoolean(key, false)) {
                        NetworkTuner.enableTcpFastOpen()
                    }
                }

                "pref_dns_server" -> {
                    val dns = sharedPreferences.getString(key, "8.8.8.8")
                    dns?.let {
                        NetworkTuner.setCustomDNS(it)
                    }
                }


                            }
        }

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SettingsFragment> {
        override fun createFromParcel(parcel: Parcel): SettingsFragment {
            return SettingsFragment(parcel)
        }

        override fun newArray(size: Int): Array<SettingsFragment?> {
            return arrayOfNulls(size)
        }
    }
}

    

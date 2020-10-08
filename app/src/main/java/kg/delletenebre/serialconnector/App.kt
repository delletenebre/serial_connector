package kg.delletenebre.serialconnector

import android.app.Application
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import androidx.preference.PreferenceManager


class App : Application() {

    val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        IntentFilter().also { intentFilter ->
            intentFilter.addAction(Intent.ACTION_SCREEN_ON)
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF)
            registerReceiver(EventsReceiver(), intentFilter)
        }
    }

    fun getBooleanPreference(key: String, defaultValue: Boolean)
            = prefs.getBoolean(key, defaultValue)

    fun getBooleanPreference(key: String): Boolean {
        val defaultValue = resources.getBoolean(
            Utils.getBooleanIdentifier(this, "pref_default_$key")
        )
        return getBooleanPreference(key, defaultValue)
    }

    fun getIntPreference(key: String, defaultValue: Int)
            = prefs.getInt(key, defaultValue)

    fun getIntPreference(key: String): Int {
        val defaultValue = resources.getInteger(
            Utils.getIntegerIdentifier(this, "pref_default_$key")
        )
        return getIntPreference(key, defaultValue)
    }

    fun getPreference(key: String, defaultValue: String): String {
        try {
            return prefs.getString(key, defaultValue) ?: ""
        } catch (ex: RuntimeException) {
            return prefs.getInt(key, defaultValue.toInt()).toString()
        }

    }

    fun getPreference(key: String): String {
        val defaultValue = resources.getString(
            Utils.getStringIdentifier(this, "pref_default_$key")
        )
        return getPreference(key, defaultValue)
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
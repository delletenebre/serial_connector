package kg.delletenebre.serialconnector.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import kg.delletenebre.serialconnector.CommunicationService
import kg.delletenebre.serialconnector.R
import kg.delletenebre.serialconnector.connections.UsbConnection


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val restartServiceButton = findPreference<Preference>("restart_service")
            restartServiceButton?.setOnPreferenceClickListener {
                context?.stopService(Intent(context, CommunicationService::class.java))
                context?.startService(Intent(context, CommunicationService::class.java))
                true
            }

            val logsButton = findPreference<Preference>("logs")
            logsButton?.setOnPreferenceClickListener {
                context?.startActivity(Intent(context, LogsActivity::class.java))
                true
            }
        }
    }
}
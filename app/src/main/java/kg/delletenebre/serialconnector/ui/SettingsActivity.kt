package kg.delletenebre.serialconnector.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import kg.delletenebre.serialconnector.CommunicationService
import kg.delletenebre.serialconnector.R


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

            findPreference<Preference>("restart_service")?.setOnPreferenceClickListener {
                context?.stopService(Intent(context, CommunicationService::class.java))
                context?.startService(Intent(context, CommunicationService::class.java))
                true
            }

            findPreference<Preference>("logs")?.setOnPreferenceClickListener {
                context?.startActivity(Intent(context, LogsActivity::class.java))
                true
            }

            findPreference<Preference>("help")?.setOnPreferenceClickListener {
                val url = "https://github.com/delletenebre/serial_connector#настройки"
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(browserIntent)
                true
            }


        }
    }
}
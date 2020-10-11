package kg.delletenebre.serialconnector

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class EventsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, CommunicationService::class.java)

        when (intent.action) {

            Intent.ACTION_SCREEN_OFF -> {
                if (App.instance.getBooleanPreference("stop_when_screen_off")) {
                    Utils.stopService(context, serviceIntent)
                }
            }

            Intent.ACTION_SCREEN_ON -> {
                if (App.instance.getBooleanPreference("restart_when_screen_on")) {
                    val delay = App.instance.getIntPreference("restart_when_screen_on_delay")
                    Executors.newSingleThreadScheduledExecutor().schedule({
                        Utils.restartService(context, serviceIntent)
                    }, delay.toLong(), TimeUnit.SECONDS)
                }
            }

            Intent.ACTION_BOOT_COMPLETED -> {
                if (App.instance.getBooleanPreference("start_on_boot_completed")) {
                    val delay = App.instance.getIntPreference("start_on_boot_completed_delay")
                    Executors.newSingleThreadScheduledExecutor().schedule({
                        Utils.startService(context, serviceIntent)
                    }, delay.toLong(), TimeUnit.SECONDS)
                }
            }

            CommunicationService.ACTION_RESTART_SERVICE -> {
                Utils.restartService(context, serviceIntent)
            }

            CommunicationService.ACTION_START_SERVICE -> {
                Utils.startService(context, serviceIntent)
            }

            CommunicationService.ACTION_STOP_SERVICE -> {
                Utils.stopService(context, serviceIntent)
            }
        }
    }
}

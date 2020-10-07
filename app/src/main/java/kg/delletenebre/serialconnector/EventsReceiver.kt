package kg.delletenebre.serialconnector

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class EventsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> {
                if (App.instance.getBooleanPreference("stop_when_screen_off")) {
                    context.stopService(Intent(context, CommunicationService::class.java))
                }
            }

            Intent.ACTION_SCREEN_ON -> {
                if (App.instance.getBooleanPreference("restart_when_screen_on")) {
                    val delay = App.instance.getIntPreference("restart_when_screen_on_delay")
                    Executors.newSingleThreadScheduledExecutor().schedule({
                        context.stopService(Intent(context, CommunicationService::class.java))
                        context.startService(Intent(context, CommunicationService::class.java))
                    }, delay.toLong(), TimeUnit.SECONDS)
                }
            }

            Intent.ACTION_BOOT_COMPLETED -> {
//                app.bootedMillis = SystemClock.uptimeMillis()
                if (App.instance.getBooleanPreference("start_on_boot_completed")) {
                    val delay = App.instance.getIntPreference("start_on_boot_completed_delay")
                    Executors.newSingleThreadScheduledExecutor().schedule({
                        context.startService(Intent(context, CommunicationService::class.java))
                    }, delay.toLong(), TimeUnit.SECONDS)
//                    Handler().postDelayed({
//                        if (!stopWhenScreenOff || App.getInstance().isScreenOn) {
//                            val startIntent = Intent(context, CommunicationService::class.java)
//
//                            context.startService(startIntent)
//                            //                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                            //                        context.startForegroundService(startIntent)
//                            //                    } else {
//                            //                        context.startService(startIntent)
//                            //                    }
//                        }
//                    }, startOnBootDelay.toLong())
                }
            }
        }
    }
}

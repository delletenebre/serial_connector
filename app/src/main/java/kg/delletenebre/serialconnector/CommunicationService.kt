package kg.delletenebre.serialconnector

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log.d
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class CommunicationService : Service() {

    private val notificationManager: NotificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    private val notificationBuilder: NotificationCompat.Builder by lazy {
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getNotificationChannel(notificationManager)
        } else {
            ""
        }

        val intent = Intent(this, SettingsActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,0)

        NotificationCompat.Builder(this, channelId).apply {
            setContentIntent(pendingIntent)
            setOngoing(true)
            setSmallIcon(R.drawable.ic_notification)
            setCategory(NotificationCompat.CATEGORY_SERVICE)
        }
    }

    private val usbCommunication: UsbCommunication by lazy {
        UsbCommunication(this)
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            if (intent.getBooleanExtra(EXTRA_RESTART_SERVICE, false)) {
                d("ok", "EXTRA_RESTART_SERVICE")
            }

            if (intent.getBooleanExtra(EXTRA_UPDATE_USB_CONNECTION, false)) {
                d("ok", "EXTRA_UPDATE_USB_CONNECTION")
                usbCommunication.connect()
                updateNotification()
            }
        }
        d("ok", "started")
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        d("ok", "onCreate")

        usbCommunication.connect()
        updateNotification()
    }

    override fun onDestroy() {
        d("ok", "onDestroy")
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getNotificationChannel(notificationManager: NotificationManager): String {
        val channelId = "kg.delletenebre.serialconnector"
        val channelName = resources.getString(R.string.app_name)
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_MIN)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(channel)
        return channelId
    }

    private fun updateNotification() {
        val usbConnections = usbCommunication.connectionsCount
        notificationBuilder.setContentTitle("USB: $usbConnections")// • BT: 0 • WS: 999")

        // startForeground(NOTIFICATION_ID, notificationBuilder.build())

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, notificationBuilder.build())
        }
        //notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    companion object {
        private const val APP_ID = BuildConfig.APPLICATION_ID
        const val ACTION_CONNECTION_ESTABLISHED = "$APP_ID.ACTION_CONNECTION_ESTABLISHED"
        const val ACTION_DATA_RECEIVED = "$APP_ID.ACTION_DATA_RECEIVED"


        const val EXTRA_RESTART_SERVICE = "restart_service"
        const val EXTRA_UPDATE_USB_CONNECTION = "update_usb_connection"

        private const val NOTIFICATION_ID = 255
    }
}

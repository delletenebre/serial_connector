package kg.delletenebre.serialconnector

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log.d
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.felhr.usbserial.UsbSerialDevice
import kg.delletenebre.serialconnector.ui.SettingsActivity


class CommunicationService : Service() {

    private val binder: IBinder = CommunicationServiceBinder()
    inner class CommunicationServiceBinder : Binder() {
        val service: CommunicationService = this@CommunicationService
    }

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
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        NotificationCompat.Builder(this, channelId).apply {
            setContentIntent(pendingIntent)
            setOngoing(true)
            setSmallIcon(R.drawable.ic_notification)
            setCategory(NotificationCompat.CATEGORY_SERVICE)
        }
    }

    private val usbCommunication: UsbCommunication by lazy {
        UsbCommunication(this, object : UsbEvents {
            override fun onConnect(serialDevice: UsbSerialDevice) {
                Intent().also { intent ->
                    intent.action = ACTION_CONNECTION_ESTABLISHED
                    intent.putExtra("connectionType", "usb")
                    intent.putExtra("portName", serialDevice.portName)
                    sendBroadcast(intent)
                }
                updateNotification()
            }

            override fun onDisconnect(deviceName: String) {
                Intent().also { intent ->
                    intent.action = ACTION_CONNECTION_LOST
                    intent.putExtra("connectionType", "usb")
                    intent.putExtra("portName", deviceName)
                    sendBroadcast(intent)
                }
                updateNotification()
            }

            override fun onData(serialDevice: UsbSerialDevice, data: String) {
                Intent().also { intent ->
                    intent.action = ACTION_DATA_RECEIVED
                    intent.putExtra("connectionType", "usb")
                    intent.putExtra("portName", serialDevice.portName)
                    intent.putExtra("data", data)
                    sendBroadcast(intent)
                }
                d(">>>", "connectionType: usb")
                d(">>>", "portName: ${serialDevice.portName}")
                d(">>>", "data: $data")
            }
        })
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
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
        usbCommunication.disconnectAll()
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getNotificationChannel(notificationManager: NotificationManager): String {
        val channelId = "kg.delletenebre.serialconnector"
        val channelName = resources.getString(R.string.app_name)
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_MIN
        )
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        notificationManager.createNotificationChannel(channel)
        return channelId
    }

    private fun updateNotification() {
        val usbConnections = usbCommunication.connections.count()
        notificationBuilder.setContentTitle("USB: $usbConnections")// • BT: 0 • WS: 999")

        // startForeground(NOTIFICATION_ID, notificationBuilder.build())

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, notificationBuilder.build())
        }
        //notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    companion object {
        private const val APP_ID = BuildConfig.APPLICATION_ID
        const val ACTION_CONNECTION_LOST = "$APP_ID.ACTION_CONNECTION_LOST"
        const val ACTION_CONNECTION_ESTABLISHED = "$APP_ID.ACTION_CONNECTION_ESTABLISHED"
        const val ACTION_DATA_RECEIVED = "$APP_ID.ACTION_DATA_RECEIVED"


        const val EXTRA_RESTART_SERVICE = "restart_service"
        const val EXTRA_UPDATE_USB_CONNECTION = "update_usb_connection"

        private const val NOTIFICATION_ID = 255
    }
}

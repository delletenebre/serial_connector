package kg.delletenebre.serialconnector

import android.app.*
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.felhr.usbserial.UsbSerialDevice
import kg.delletenebre.serialconnector.connections.BluetoothConnection
import kg.delletenebre.serialconnector.connections.BluetoothEvents
import kg.delletenebre.serialconnector.connections.UsbConnection
import kg.delletenebre.serialconnector.connections.UsbEvents
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

    private val usbConnection: UsbConnection by lazy {
        UsbConnection(this, object : UsbEvents {
            override fun onConnect(serialDevice: UsbSerialDevice) {
                Intent().also { intent ->
                    intent.action = ACTION_CONNECTION_ESTABLISHED
                    intent.putExtra("connectionType", "usb")
                    intent.putExtra("name", serialDevice.portName)
                    sendBroadcast(intent)
                }
                updateNotification()
            }

            override fun onDisconnect(deviceName: String) {
                Intent().also { intent ->
                    intent.action = ACTION_CONNECTION_LOST
                    intent.putExtra("connectionType", "usb")
                    intent.putExtra("name", deviceName)
                    sendBroadcast(intent)
                }
                updateNotification()
            }

            override fun onMessageReceived(serialDevice: UsbSerialDevice, data: String) {
                Intent().also { intent ->
                    intent.action = ACTION_DATA_RECEIVED
                    intent.putExtra("connectionType", "usb")
                    intent.putExtra("name", serialDevice.portName)
                    intent.putExtra("data", data)
                    sendBroadcast(intent)
                }
            }
        })
    }

    private val bluetoothConnection: BluetoothConnection by lazy {
        BluetoothConnection(this, object : BluetoothEvents {
            override fun onConnect(mac: String) {
                Intent().also { intent ->
                    intent.action = ACTION_CONNECTION_ESTABLISHED
                    intent.putExtra("connectionType", "bluetooth")
                    intent.putExtra("name", mac)
                    sendBroadcast(intent)
                }
                updateNotification()
            }

            override fun onDisconnect(mac: String) {
                Intent().also { intent ->
                    intent.action = ACTION_CONNECTION_LOST
                    intent.putExtra("connectionType", "bluetooth")
                    intent.putExtra("name", mac)
                    sendBroadcast(intent)
                }
                updateNotification()
            }

            override fun onMessageReceived(mac: String, data: String) {
                Intent().also { intent ->
                    intent.action = ACTION_DATA_RECEIVED
                    intent.putExtra("connectionType", "bluetooth")
                    intent.putExtra("name", mac)
                    intent.putExtra("data", data)
                    sendBroadcast(intent)
                }
            }
        })
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val usbDevice: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
        usbDevice?.let {
            usbConnection.connectTo(it)
        }

        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, notificationBuilder.build())
        updateNotification()
        usbConnection.connect()
        bluetoothConnection.connect()
    }

    override fun onDestroy() {
        usbConnection.destroy()
        bluetoothConnection.destroy()
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getNotificationChannel(notificationManager: NotificationManager): String {
        val channelId = APP_ID
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
        val usbConnections = usbConnection.connections.count()
        val bluetoothConnected = bluetoothConnection.connectedDeviceMac.isNotEmpty()
        val bluetoothState = if (bluetoothConnected) { "+" } else { "-" }
        notificationBuilder.setContentTitle("USB: $usbConnections • BT: $bluetoothState")// • WS: 999")

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    companion object {
        private const val APP_ID = BuildConfig.APPLICATION_ID
        const val ACTION_CONNECTION_ESTABLISHED = "$APP_ID.ACTION_CONNECTION_ESTABLISHED"
        const val ACTION_CONNECTION_LOST = "$APP_ID.ACTION_CONNECTION_LOST"
        const val ACTION_DATA_RECEIVED = "$APP_ID.ACTION_DATA_RECEIVED"

        const val ACTION_START_SERVICE = "$APP_ID.ACTION_START_SERVICE"
        const val ACTION_STOP_SERVICE = "$APP_ID.ACTION_STOP_SERVICE"
        const val ACTION_RESTART_SERVICE = "$APP_ID.ACTION_RESTART_SERVICE"

        private const val NOTIFICATION_ID = 255
    }
}

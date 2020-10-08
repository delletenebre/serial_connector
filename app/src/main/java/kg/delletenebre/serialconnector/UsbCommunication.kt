package kg.delletenebre.serialconnector

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import com.felhr.usbserial.UsbSerialDevice


interface UsbEvents {
    fun onConnect(serialDevice: UsbSerialDevice)
    fun onDisconnect(deviceName: String)
    fun onData(serialDevice: UsbSerialDevice, data: String)
}

class UsbCommunication(private val context: Context, private val usbEvents: UsbEvents) {

    var connections = mutableMapOf<String, UsbSerialDevice>()

    private val broadcastReceiver: BroadcastReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                        val usbDevice: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                        disconnect(usbDevice?.deviceName ?: "")
                    }

                    ACTION_USB_PERMISSION -> {
                        synchronized(this) {
                            val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                                device?.apply {
                                    connectTo(this)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    private val usbManager: UsbManager by lazy {
        context.getSystemService(Context.USB_SERVICE) as UsbManager
    }
    private val permissionIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(ACTION_USB_PERMISSION),
        0
    )
    private var baudRate = App.instance.getPreference("usb_connection_baud_rate").toInt()
    private var dataBits = App.instance.getPreference("usb_connection_data_bits").toInt()
    private var parity = App.instance.getPreference("usb_connection_parity").toInt()
    private var stopBits = App.instance.getPreference("usb_connection_stop_bits").toInt()
    private var flowControl = App.instance.getPreference("usb_connection_flow_control").toInt()


    init {
        IntentFilter().also { intentFilter ->
            intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            context.registerReceiver(broadcastReceiver, intentFilter)
        }
    }

    fun connect() {
        baudRate = App.instance.getPreference("usb_connection_baud_rate").toInt()
        dataBits = App.instance.getPreference("usb_connection_data_bits").toInt()
        parity = App.instance.getPreference("usb_connection_parity").toInt()
        stopBits = App.instance.getPreference("usb_connection_stop_bits").toInt()
        flowControl = App.instance.getPreference("usb_connection_flow_control").toInt()

        usbManager.deviceList.values.forEach { usbDevice ->
            usbManager.requestPermission(usbDevice, permissionIntent)
        }
    }

    fun disconnect(portName: String) {
        if (connections.containsKey(portName)) {
            connections[portName]?.close()
            connections.remove(portName)
            usbEvents.onDisconnect(portName)
        }
    }

    fun disconnectAll() {
        connections.values.forEach {
            disconnect(it.portName)
        }
    }

    private fun connectTo(usbDevice: UsbDevice) {
        try {
            val connection = usbManager.openDevice(usbDevice)
            val serialDevice: UsbSerialDevice
            val deviceName = usbDevice.deviceName

            serialDevice = UsbSerialDevice.createUsbSerialDevice(usbDevice, connection)
            if (serialDevice.open()) {
                serialDevice.setBaudRate(baudRate)
                serialDevice.setDataBits(dataBits)
                serialDevice.setParity(parity)
                serialDevice.setStopBits(stopBits)
                serialDevice.setFlowControl(flowControl)
                serialDevice.portName = deviceName


//                        if (buffer.containsKey(deviceName)) {
//                            return;
//                        }

                serialDevice.read { bytes ->
                    if (bytes.isNotEmpty()) {
                        usbEvents.onData(serialDevice, bytes.toString(Charsets.UTF_8))
//                                if (buffer != null) {
//                                    buffer.write(bytes)
//                                    if (buffer.toByteArray().contains(0x0A)) {
//                                        val data = buffer.toString(Charsets.UTF_8.name())
//                                        val dataParts = data.split("\n".toRegex(), 2)
//                                        val command = dataParts[0].replace("\r", "")
//                                        // TODO send broadcast data received
//                                        buffer.reset()
//                                        buffer.write(dataParts[1].toByteArray(Charsets.UTF_8))
//                                    }
//                                }
                    }
                }

                // Some Arduinos would need some sleep because firmware wait some time to
                // know whether a new sketch is going to be uploaded or not
                Thread.sleep(1000)
                Log.d("USB Connection", "Connected to USB-device: $deviceName")
                // TODO send broadcast device connected
//                        if (App.getInstance().getBooleanPreference("send_connection_state")) {
//                            serialDevice.write((App.ACTION_CONNECTION_ESTABLISHED + "\n").toByteArray())
//                        }

                connections[serialDevice.portName] = serialDevice
                usbEvents.onConnect(serialDevice)
            }
        } catch (e: Exception) {
            Log.d("usb", "exception: ${e.localizedMessage}")
        }
    }

    companion object {
        private const val ACTION_USB_PERMISSION = "${BuildConfig.APPLICATION_ID}.USB_PERMISSION"
    }
}
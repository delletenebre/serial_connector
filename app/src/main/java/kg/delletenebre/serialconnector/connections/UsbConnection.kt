package kg.delletenebre.serialconnector.connections

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import com.felhr.usbserial.UsbSerialDevice
import kg.delletenebre.serialconnector.App
import kg.delletenebre.serialconnector.BuildConfig
import kg.delletenebre.serialconnector.CommunicationService
import kg.delletenebre.serialconnector.SerialEventsListener


class UsbConnection(private val context: Context, private val events: SerialEventsListener) {

    val connections = mutableMapOf<String, UsbSerialDevice>()
    private val buffers = mutableMapOf<String, ConnectionBuffer>()

    private val usbManager: UsbManager by lazy {
        context.getSystemService(Context.USB_SERVICE) as UsbManager
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val usbDevice: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    usbDevice?.let {
                        disconnect(it.deviceName)
                    }
                }

                ACTION_USB_PERMISSION -> {
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        device?.let { usbDevice ->
                            val filterMatch = deviceNameFilter.find(usbDevice.deviceName)
                            filterMatch?.let {
                                connectTo(usbDevice)
                            }
                        }
                    }
                }

                CommunicationService.ACTION_SEND_MESSAGE -> {
                    val message = intent.getStringExtra("message") ?: ""
                    if (message.isNotEmpty()) {
                        write(message)
                    }
                }
            }
        }
    }

    private val permissionIntent = PendingIntent.getBroadcast(
        context,
        0,
        Intent(ACTION_USB_PERMISSION),
        0
    )

    private var baudRate = getIntegerPreference("usb_connection_baud_rate")
    private var dataBits = getIntegerPreference("usb_connection_data_bits")
    private var parity = getIntegerPreference("usb_connection_parity")
    private var stopBits = getIntegerPreference("usb_connection_stop_bits")
    private var flowControl = getIntegerPreference("usb_connection_flow_control")
    private var deviceNameFilter = App.instance.getPreference("usb_connection_filter").toRegex()

    init {
        IntentFilter().also { intentFilter ->
            intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            intentFilter.addAction(ACTION_USB_PERMISSION)
            intentFilter.addAction(CommunicationService.ACTION_SEND_MESSAGE)
            context.registerReceiver(broadcastReceiver, intentFilter)
        }
    }

    fun connect() {
        baudRate = getIntegerPreference("usb_connection_baud_rate")
        dataBits = getIntegerPreference("usb_connection_data_bits")
        parity = getIntegerPreference("usb_connection_parity")
        stopBits = getIntegerPreference("usb_connection_stop_bits")
        flowControl = getIntegerPreference("usb_connection_flow_control")
        deviceNameFilter = App.instance.getPreference("usb_connection_filter").toRegex()

        usbManager.deviceList.values.forEach { usbDevice ->
            if (usbManager.hasPermission(usbDevice)) {
                connectTo(usbDevice)
            } else {
                usbManager.requestPermission(usbDevice, permissionIntent)
            }
        }
    }

    fun write(message: String) {
        connections.values.forEach {
            it.write(message.toByteArray())
        }
    }

    @Synchronized fun disconnect(portName: String) {
        if (connections.containsKey(portName)) {
            connections[portName]?.close()
            connections.remove(portName)
            buffers.remove(portName)
            events.onDisconnect(SERIAL_TYPE, portName)
        }
    }

    fun destroy() {
        connections.values.forEach {
            disconnect(it.portName)
        }
        context.unregisterReceiver(broadcastReceiver)
    }

    @Synchronized fun connectTo(usbDevice: UsbDevice) {
        val deviceName = usbDevice.deviceName
        if (connections.containsKey(deviceName)) {
            disconnect(deviceName)
        }

        try {
            val connection = usbManager.openDevice(usbDevice)
            val serialDevice: UsbSerialDevice


            serialDevice = UsbSerialDevice.createUsbSerialDevice(usbDevice, connection)
            if (serialDevice.open()) {
                serialDevice.setBaudRate(baudRate)
                serialDevice.setDataBits(dataBits)
                serialDevice.setParity(parity)
                serialDevice.setStopBits(stopBits)
                serialDevice.setFlowControl(flowControl)
                serialDevice.portName = deviceName

                /*
                * `Управление потоком` (flow control) - Ранние устройства с UART могли быть настолько медлительными,
                что не успевали обрабатывать поток принимаемых данных. Для решения этой проблемы модули UART иногда
                снабжались отдельными выходами и входами управления потоком. При заполнении входного буфера логика
                принимающего UART выставляла на соответствующем выходе запрещающий уровень, и передающий UART
                приостанавливал передачу.
                `Off` - управление потоком отключено.
                `RTS-CTS` - аппаратный протокол RTS/CTS.
                `DSR-DTR` - аппаратный протокол DSR/DTR.
                `XON-XOFF` - программный протокол XOn/XOff.
                */
                connections[deviceName] = serialDevice
                buffers[deviceName] = ConnectionBuffer()
                serialDevice.read { bytes ->
                    if (bytes.isNotEmpty()) {
                        buffers[deviceName]?.let {
                            if (it.checkBytes(bytes)) {
                                it.commands.forEach { message ->
                                    events.onMessageReceived(SERIAL_TYPE, deviceName, message)
                                }
                                it.commands.clear()
                            }
                        }
                    }
                }

                // Some Arduinos would need some sleep because firmware wait some time to
                // know whether a new sketch is going to be uploaded or not
                // Thread.sleep(1000)
//                        if (App.getInstance().getBooleanPreference("send_connection_state")) {
//                            serialDevice.write((App.ACTION_CONNECTION_ESTABLISHED + "\n").toByteArray())
//                        }
                events.onConnect(SERIAL_TYPE, deviceName)
            }
        } catch (e: Exception) {
            Log.d("usb", "exception: ${e.localizedMessage}")
        }
    }

    private fun getIntegerPreference(key: String) = App.instance.getPreference(key).toInt()

    companion object {
        private const val SERIAL_TYPE = "usb"
        private const val ACTION_USB_PERMISSION = "${BuildConfig.APPLICATION_ID}.USB_PERMISSION"
    }
}
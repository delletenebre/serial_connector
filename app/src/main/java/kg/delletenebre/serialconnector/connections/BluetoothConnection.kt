package kg.delletenebre.serialconnector.connections

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothClassicService
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothConfiguration
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService.OnBluetoothEventCallback
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus
import com.github.douglasjunior.bluetoothlowenergylibrary.BluetoothLeService
import kg.delletenebre.serialconnector.App
import kg.delletenebre.serialconnector.R
import kg.delletenebre.serialconnector.SerialEventsListener
import java.util.*


class BluetoothConnection(private val context: Context, private val events: SerialEventsListener) {
    private val bluetoothManager: BluetoothManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    var connectedDeviceMac = ""
    val buffer = ConnectionBuffer()

    fun connect() {
        val selectedMac = App.instance.getPreference("bluetooth_device", "")
        if (selectedMac.isNotEmpty()) {
            connectTo(selectedMac)
        }
    }

    fun destroy() {
        buffer.clear()
    }

    private fun connectTo(mac: String) {
        val bluetoothAdapter = bluetoothManager.adapter
        val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(mac)
        val useLowEnergyProtocol = App.instance.getBooleanPreference("use_bluetooth_le")
        val serviceUuid = App.instance.getPreference("bluetooth_le_service_uuid")
        val characteristicUuid = App.instance.getPreference("bluetooth_le_characteristic_uuid")
        val appName = context.resources.getString(R.string.app_name)
        val serialUuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

        val config = BluetoothConfiguration().apply {
            context = App.instance
            bufferSize = 1024
            characterDelimiter = '\n'
            deviceName = appName
            callListenersInMainThread = false
            uuid = serialUuid // Used to filter found devices. Set null to find all devices.
            if (useLowEnergyProtocol) {
                bluetoothServiceClass = BluetoothLeService::class.java
                uuidService = UUID.fromString(serviceUuid) // Required
                uuidCharacteristic = UUID.fromString(characteristicUuid) // Required
                // transport = BluetoothDevice.TRANSPORT_LE // Required for dual-mode devices
            } else {
                bluetoothServiceClass = BluetoothClassicService::class.java
            }
        }

        BluetoothService.init(config)
        BluetoothService.getDefaultInstance()?.apply {
            disconnect()
            setOnEventCallback(object : OnBluetoothEventCallback {
                override fun onDataRead(bytes: ByteArray, length: Int) {
                    if (bytes.isNotEmpty()) {
                        if (buffer.checkBytes(bytes)) {
                            buffer.commands.forEach {
                                events.onMessageReceived(SERIAL_TYPE, connectedDeviceMac, it)
                            }
                            buffer.commands.clear()
                        }
                    }
                }

                override fun onStatusChange(status: BluetoothStatus) {
                    when (status) {
                        BluetoothStatus.NONE -> {
                            val disconnectedDeviceMac = connectedDeviceMac
                            connectedDeviceMac = ""
                            events.onDisconnect(SERIAL_TYPE, disconnectedDeviceMac)
                        }

                        BluetoothStatus.CONNECTED -> {
                            connectedDeviceMac = device.address
                            events.onConnect(SERIAL_TYPE, connectedDeviceMac)
                        }

                        else -> {
                            buffer.clear()
                        }
                    }
                }

                override fun onDeviceName(deviceName: String) {}

                override fun onToast(message: String) {
                    Log.d("ahoha", "onToast: $message")
                }

                override fun onDataWrite(buffer: ByteArray) {
                    Log.d("ahoha", "onDataWrite: $buffer")
                }
            })
            connect(device)
        }
    }

    companion object {
        private const val SERIAL_TYPE = "bluetooth"
    }
//
//    private fun onError(error: Throwable) {
//        Log.e("ahoha", "error: " + error.localizedMessage)
//        val disconnectedDeviceMac = connectedDeviceMac
//        connectedDeviceMac = ""
//        events.onDisconnect(disconnectedDeviceMac)
//
//        //connect()
//    }
}
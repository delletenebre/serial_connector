package kg.delletenebre.serialconnector

import android.content.Context
import android.hardware.usb.UsbManager
import android.util.Log
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import java.io.ByteArrayOutputStream
import java.lang.Exception


class UsbCommunication(private val context: Context) {
    val connectionsCount: Int
        get() = buffer.size

    private val buffer = HashMap<String, ByteArrayOutputStream>()

    fun connect() {
        // Find all available drivers from attached devices.
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        for (device in usbManager.deviceList.values) {
            if (usbManager.hasPermission(device)) {
                val connection = usbManager.openDevice(device)
                val serialDevice: UsbSerialDevice

                try {
                    serialDevice = UsbSerialDevice.createUsbSerialDevice(device, connection)
                    if (serialDevice.open()) {
                        serialDevice.setBaudRate(115200)
                        serialDevice.setDataBits(UsbSerialInterface.DATA_BITS_8)
                        serialDevice.setParity(UsbSerialInterface.PARITY_ODD)
                        serialDevice.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)

                        val deviceName = device.deviceName
//                        if (buffer.containsKey(deviceName)) {
//                            return;
//                        }
                        buffer[deviceName] = ByteArrayOutputStream()
                        serialDevice.read { bytes ->
                            if (bytes.isNotEmpty()) {
                                val buffer = buffer[deviceName]
                                if (buffer != null) {
                                    buffer.write(bytes)
                                    if (buffer.toByteArray().contains(0x0A)) {
                                        val data = buffer.toString(Charsets.UTF_8.name())
                                        val dataParts = data.split("\n".toRegex(), 2)
                                        val command = dataParts[0].replace("\r", "")
                                        // TODO send broadcast data received
                                        buffer.reset()
                                        buffer.write(dataParts[1].toByteArray(Charsets.UTF_8))
                                    }
                                }
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
//                        mLocalBroadcastManager.sendBroadcast(
//                            Intent(App.LOCAL_ACTION_CONNECTION_ESTABLISHED)
//                                .putExtra("type", "usb")
//                                .putExtra("name", deviceName))
                    }
                } catch (e: Exception) {
                    Log.d("usb", "exception: ${e.localizedMessage}")
                }
            }
        }
    }
}
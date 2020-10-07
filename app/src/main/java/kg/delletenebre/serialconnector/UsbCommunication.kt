package kg.delletenebre.serialconnector

import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import java.io.ByteArrayOutputStream
import java.lang.Exception


class UsbCommunication(private val context: Context) {
    var connectionsCount = 0

    fun connect() {
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        for (usbDevice in usbManager.deviceList.values) {
            if (usbManager.hasPermission(usbDevice)) {
                try {
                    val connection = usbManager.openDevice(usbDevice)
                    val serialDevice: UsbSerialDevice

                    serialDevice = UsbSerialDevice.createUsbSerialDevice(usbDevice, connection)
                    if (serialDevice.open()) {
                        serialDevice.setBaudRate(115200)
                        serialDevice.setDataBits(UsbSerialInterface.DATA_BITS_8)
                        serialDevice.setParity(UsbSerialInterface.PARITY_ODD)
                        serialDevice.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)

                        val deviceName = usbDevice.deviceName
//                        if (buffer.containsKey(deviceName)) {
//                            return;
//                        }
                        serialDevice.read { bytes ->
                            if (bytes.isNotEmpty()) {
                                Intent().also { intent ->
                                    intent.action = CommunicationService.ACTION_DATA_RECEIVED
                                    intent.putExtra("connectionType", "usb")
                                    intent.putExtra("deviceName", deviceName)
                                    intent.putExtra("data", bytes.toString(Charsets.UTF_8))
                                    context.sendBroadcast(intent)
                                }
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
                        connectionsCount++
                        // TODO send broadcast device connected
//                        if (App.getInstance().getBooleanPreference("send_connection_state")) {
//                            serialDevice.write((App.ACTION_CONNECTION_ESTABLISHED + "\n").toByteArray())
//                        }
                        Intent().also { intent ->
                            intent.action = CommunicationService.ACTION_CONNECTION_ESTABLISHED
                            intent.putExtra("connectionType", "usb")
                            intent.putExtra("deviceName", deviceName)
                            context.sendBroadcast(intent)
                        }
                    }
                } catch (e: Exception) {
                    Log.d("usb", "exception: ${e.localizedMessage}")
                }
            }
        }
    }
}
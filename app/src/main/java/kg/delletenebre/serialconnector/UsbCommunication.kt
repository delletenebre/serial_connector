package kg.delletenebre.serialconnector

//import android.content.Context
//import android.hardware.usb.UsbManager
//import android.util.Log
//import com.hoho.android.usbserial.driver.UsbSerialPort
//import com.hoho.android.usbserial.driver.UsbSerialProber
//import com.hoho.android.usbserial.util.SerialInputOutputManager
//import java.lang.Exception
//import java.util.concurrent.Executors
//
//
//class UsbCommunication(val context: Context) : SerialInputOutputManager.Listener {
//    var connectedDevices = 0
//
//    fun connect() {
//        // Find all available drivers from attached devices.
//        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
//        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
//        if (availableDrivers.isEmpty()) {
//            return
//        }
//
//        connectedDevices = 0
//        for (driver in availableDrivers) {
//            // Open a connection
//            val connection = manager.openDevice(driver.device) ?: return
//            val port = driver.ports[0] // Most devices have just one port (port 0)
//            port.open(connection)
//            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
//
////            val usbIoManager = SerialInputOutputManager(port, object : SerialInputOutputManager.Listener {
////                override fun onNewData(data: ByteArray) {
////                    Log.d("usb", "new data")
////                    Log.d("usb", data.toString())
////                }
////
////                override fun onRunError(e: Exception) {
////                    Log.d("usb", "exception: ${e.localizedMessage}")
////                }
////            })
//            val usbIoManager = SerialInputOutputManager(port, this)
//            Executors.newSingleThreadExecutor().submit(usbIoManager)
//
//            port.write("hello".toByteArray(), 1000)
//            Log.d("usb", "usb connected")
//
//            connectedDevices++
//        }
//    }
//
//    override fun onNewData(data: ByteArray) {
//        Log.d("usb", "new data")
//        Log.d("usb", data.toString())
//    }
//
//    override fun onRunError(e: Exception) {
//        Log.d("usb", "exception: ${e.localizedMessage}")
//    }
//}

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import com.hoho.android.usbserial.driver.UsbSerialPort
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.nio.charset.Charset


class UsbCommunication(val context: Context) {
    var connectedDevices = 0

    fun connect() {
        // Find all available drivers from attached devices.
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        connectedDevices = 0
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
                        val vid = device.vendorId.toString()
                        val pid = device.productId.toString()
//                        sBuffers[deviceName] = ByteArrayOutputStream()
                        serialDevice.read { bytes ->
                            val str = bytes.toString(Charset.forName("utf-8"))
                            Log.d("usb", "read: $str")
                            if (bytes.isNotEmpty()) {
//                                val buffer = sBuffers[deviceName]
//                                if (buffer != null) {
//                                    buffer.write(bytes)
//                                    if (buffer.toByteArray().contains(0x0A)) {
//                                        val data = buffer.toString("UTF-8")
//                                        val dataParts = data.split("\n".toRegex(), 2)
//                                        val command = dataParts[0]
//                                            .replace("\r", "")
//                                        mLocalBroadcastManager.sendBroadcast(
//                                            Intent(App.LOCAL_ACTION_COMMAND_RECEIVED)
//                                                .putExtra("from", "usb")
//                                                .putExtra("command", command))
//                                        buffer.reset()
//                                        buffer.write(dataParts[1].toByteArray(Charsets.UTF_8))
//                                    }
//                                }
                            }
                        }

                        // Some Arduinos would need some sleep because firmware wait some time to know whether
                        // a new sketch is going to be uploaded or not
                        //Thread.sleep(2000)
                        connectedDevices++
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
//                if (open(device)) {
//                    val path = device.deviceName
//                    val vid = device.vendorId.toString()
//                    val pid = device.productId.toString()
//                    Log.d("USB Connection", "Connected to USB-device: $path | VID:$vid | PID: $pid")
//                }
            }
        }
    }
}
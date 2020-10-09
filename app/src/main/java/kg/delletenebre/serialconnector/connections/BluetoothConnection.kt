package kg.delletenebre.serialconnector.connections

import com.harrysoft.androidbluetoothserial.BluetoothManager
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kg.delletenebre.serialconnector.App

interface BluetoothEvents {
    fun onConnect(mac: String)
    fun onDisconnect(mac: String)
    fun onMessageReceived(mac: String, data: String)
}

class BluetoothConnection(private val bluetoothEvents: BluetoothEvents) {
    private val bluetoothManager: BluetoothManager = BluetoothManager.getInstance()
    private lateinit var deviceInterface: SimpleBluetoothDeviceInterface

    fun connect() {
        val selectedMac = App.instance.getPreference("bluetooth_device", "")
        if (selectedMac.isNotEmpty()) {
            connectTo(selectedMac)
        }
    }

    fun destroy() {
        bluetoothManager.close()
    }

    private fun connectTo(mac: String): Disposable {
        return bluetoothManager.openSerialDevice(mac)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onConnected, this::onError)
    }

    private fun onConnected(connectedDevice: BluetoothSerialDevice) {
        val mac = connectedDevice.mac
        deviceInterface = connectedDevice.toSimpleDeviceInterface()
        deviceInterface.setListeners(
            { message: String -> bluetoothEvents.onMessageReceived(mac, message) },
            { message: String -> onMessageSent(message) }
        ) { error: Throwable -> onError(error) }
        bluetoothEvents.onConnect(mac)
    }

    private fun onMessageSent(message: String) {

    }

    private fun onError(error: Throwable) {
        // Handle the error
    }
}
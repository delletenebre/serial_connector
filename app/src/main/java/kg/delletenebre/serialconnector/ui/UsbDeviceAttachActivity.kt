package kg.delletenebre.serialconnector.ui

import android.app.Activity
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import kg.delletenebre.serialconnector.CommunicationService
import kg.delletenebre.serialconnector.Utils

class UsbDeviceAttachActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(applicationContext, CommunicationService::class.java)
        val usbDevice: UsbDevice? = getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE)
        usbDevice?.let {
            intent.putExtra(UsbManager.EXTRA_DEVICE, it)
        }
        Utils.startService(this, intent)
        finish()
    }
}
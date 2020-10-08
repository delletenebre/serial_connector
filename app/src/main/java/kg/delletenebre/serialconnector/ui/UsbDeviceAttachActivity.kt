package kg.delletenebre.serialconnector.ui

import android.app.Activity
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import kg.delletenebre.serialconnector.CommunicationService

class UsbDeviceAttachActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(applicationContext, CommunicationService::class.java)
        val usbDevice: UsbDevice? = getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE)
        usbDevice?.apply {
            intent.putExtra(CommunicationService.EXTRA_UPDATE_USB_CONNECTION, true)
        }
        startService(intent)
        finish()
    }
}
package kg.delletenebre.serialconnector.ui

import android.app.Activity
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import kg.delletenebre.serialconnector.CommunicationService

class UsbDeviceAttachActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val usbManager = getSystemService(USB_SERVICE) as UsbManager
        val intent = Intent(applicationContext, CommunicationService::class.java)
        val usbDevice = getIntent().getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
        if (usbDevice != null && usbManager.hasPermission(usbDevice)) {
            intent.putExtra(CommunicationService.EXTRA_UPDATE_USB_CONNECTION, true)
        }
        startService(intent)
        finish()
    }
}
package kg.delletenebre.serialconnector.views

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference
import kg.delletenebre.serialconnector.R


class BluetoothDevicesListPreference(
    context: Context,
    attrs: AttributeSet?
) : ListPreference(context, attrs) {
    init {
        val entriesList: MutableList<CharSequence> = ArrayList()
        val entryValuesList: MutableList<CharSequence> = ArrayList()
        val res = context.resources
        entriesList.add(res.getString(R.string.no_device))
        entryValuesList.add("")
        val bta = BluetoothAdapter.getDefaultAdapter()
        if (bta != null) {
            val pairedDevices = bta.bondedDevices
            for (dev in pairedDevices) {
                entriesList.add("${dev.name} [ ${dev.address} ]")
                entryValuesList.add(dev.address)
            }
        }
        entries = entriesList.toTypedArray()
        entryValues = entryValuesList.toTypedArray()
    }
}
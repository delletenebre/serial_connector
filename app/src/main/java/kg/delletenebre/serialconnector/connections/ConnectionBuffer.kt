package kg.delletenebre.serialconnector.connections

import android.util.Log
import kg.delletenebre.serialconnector.App


class ConnectionBuffer {
    private var buffer = ""
    var command = ""

    fun checkBytes(bytes: ByteArray): Boolean {
        val maxMessageLength = App.instance.getPreference("max_message_length").toInt()
        if (bytes.size < maxMessageLength) {
            val message = bytes.toString(Charsets.UTF_8)
            return checkMessage(message)
        }
        return false
    }

    fun clear() {
        buffer = ""
    }

    private fun checkMessage(message: String): Boolean {
        val finalSymbol = App.instance.getPreference("final_symbol")
        val finalRegex = finalSymbol.toRegex()
        buffer = buffer.plus(message)
        if (finalSymbol.isEmpty()) {
            command = buffer
            clear()
            return command.isNotEmpty()
        } else if (buffer.contains(finalRegex)) {
            val dataParts = buffer.split(finalRegex).toMutableList()
            command = dataParts.removeFirst()
            buffer = dataParts.joinToString(finalSymbol)
            return command.isNotEmpty()
        }
        return false
    }

}
package kg.delletenebre.serialconnector.connections

import kg.delletenebre.serialconnector.App


class ConnectionBuffer {
    private var buffer = ""
    var command = ""

    fun checkBytes(bytes: ByteArray, finalSymbol: String): Boolean {
        val maxMessageLength = App.instance.getPreference("max_message_length").toInt()
        if (bytes.size < maxMessageLength) {
            val message = bytes.toString(Charsets.UTF_8)
            return checkMessage(message, finalSymbol)
        }
        return false
    }

    private fun checkMessage(message: String, finalSymbol: String): Boolean {
        buffer = buffer.plus(message)
        if (buffer.contains(finalSymbol)) {
            val dataParts = buffer.split(finalSymbol).toMutableList()
            command = dataParts.removeFirst()
            buffer = dataParts.joinToString(finalSymbol)
            return true
        }
        return false
    }

}
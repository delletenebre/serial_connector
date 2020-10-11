package kg.delletenebre.serialconnector.connections

import kg.delletenebre.serialconnector.App


class ConnectionBuffer {
    private var buffer = ""
    var commands = mutableListOf<String>()

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
        val finalRegex = App.instance.getPreference("final_symbol").toRegex()
        buffer = buffer.plus(message)
        if (finalSymbol.isEmpty()) {
            commands.clear()
            commands.add(buffer)
            clear()
        } else if (buffer.contains(finalRegex)) {
            val dataParts = buffer.split(finalRegex).toMutableList()
            buffer = dataParts.removeLast()
            commands.clear()
            commands = dataParts
        }
        commands = commands.filter { it.isNotEmpty() }.toMutableList()
        return commands.isNotEmpty()
    }

}
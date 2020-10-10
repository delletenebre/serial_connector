package kg.delletenebre.serialconnector.connections


class ConnectionBuffer {
    private var buffer = ""
    var command = ""

    fun checkBuffer(message: String, finalSymbol: String): Boolean {
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
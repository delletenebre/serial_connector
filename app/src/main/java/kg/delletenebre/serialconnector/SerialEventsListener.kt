package kg.delletenebre.serialconnector

interface SerialEventsListener {
    fun onConnect(type: String, name: String)
    fun onDisconnect(type: String, name: String)
    fun onMessageReceived(type: String, name: String, data: String)
}
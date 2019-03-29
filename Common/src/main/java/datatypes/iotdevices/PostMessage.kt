package datatypes.iotdevices

data class PostMessage(
    val messageChainID: String = "", val deviceID: String = "", val type: String = "", val path: String = "", val params: Map<String, String> = emptyMap(), val addressTo: String = ""
)

data class PostMessageHack(
    val postMessage: PostMessage, val json: String, val addressFrom: String = ""
)
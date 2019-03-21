package datatypes.iotdevices

data class PostMessage(
    val messageChainID: String = "", val devicedID: String = "", val type: String = "", val path: String = "", val params: Map<String, String> = emptyMap()
)

data class PostMessageHack(
    val postMessage: PostMessage, val json: String
)
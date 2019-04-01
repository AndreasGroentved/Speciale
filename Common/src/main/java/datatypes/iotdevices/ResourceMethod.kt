package datatypes.iotdevices

data class ResourceMethod(val methodType: String ="", val parameters: Map<String, String> = mapOf(), val description: String = "")
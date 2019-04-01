package datatypes.iotdevices

data class DeviceResource(val resourceMethods: List<ResourceMethod> = listOf(), val path: String ="", val title: String="")
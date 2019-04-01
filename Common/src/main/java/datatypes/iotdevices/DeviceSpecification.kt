package datatypes.iotdevices

data class DeviceSpecification(val id: String = "", val deviceResources: MutableList<DeviceResource> = mutableListOf())
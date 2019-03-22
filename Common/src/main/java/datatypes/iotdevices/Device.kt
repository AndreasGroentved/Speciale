package datatypes.iotdevices

data class Device(val idIp: IdIp = IdIp(), val specification: DeviceSpecification = DeviceSpecification("", mutableListOf()))
package datatypes.authmanager

import datatypes.iotdevices.DeviceSpecification

data class User(
    val username: String,
    val password: String,
    val devices: MutableList<DeviceSpecification>
)
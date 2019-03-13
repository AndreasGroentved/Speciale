package datatypes.authmanager

import datatypes.iotdevices.DeviceSpecification
import java.math.BigInteger

data class User(
    val username: String,
    val password: String,
    val key: BigInteger,
    val devices: MutableList<DeviceSpecification>
)
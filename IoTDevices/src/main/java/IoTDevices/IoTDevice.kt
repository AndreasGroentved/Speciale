package IoTDevices

import Helpers.PropertiesLoader
import org.eclipse.californium.core.CoapServer


abstract class IoTDevice(val id: String) : CoapServer() {
    var coapPort: Int? = -1

    init {
        loadProperties()
    }

    private fun loadProperties() {
        val properties = PropertiesLoader.loadProperties()
        coapPort = properties.getProperty("coapPort").toInt()
    }

    fun getDeviceSpecification(): DeviceSpecification {
        return DeviceSpecification(id, root.children)
    }
}
package IoTDevices

import org.eclipse.californium.core.CoapServer
import java.util.*


abstract class IoTDevice(val id :String) : CoapServer() {
    var coapPort: Int? = -1

    init {
        loadProperties()
    }

    private fun loadProperties() {
        val properties = Properties()
        try {
            val resourceAsStream = IoTDevice::class.java.getResourceAsStream("properties.config")
            properties.load(resourceAsStream)
            coapPort = properties.getProperty("coapPort").toInt()
            resourceAsStream.close()
        } catch (e: Exception) {
            println("Could not read file config.properties")
            e.printStackTrace()
            throw e
        }
    }

    fun getDeviceSpecification(): DeviceSpecification {
        return DeviceSpecification(id, root.children)
    }
}
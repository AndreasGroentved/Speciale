package IoTDevices

import Helpers.PropertiesLoader
import org.eclipse.californium.core.CoapServer
import org.eclipse.californium.core.server.resources.Resource


abstract class IoTDevice(val id: String = "") : CoapServer() {
    var coapPort: Int = -1
    val deviceSpecification = DeviceSpecification(id, mutableListOf())

    init {
        loadProperties()
    }

    fun add(resource: Resource, resourceMethods: MutableList<ResourceMethod>): CoapServer {
        deviceSpecification.deviceResources.add(DeviceResource(resourceMethods, resource.uri, resource.attributes.title))
        return super.add(resource)
    }

    private fun loadProperties() {
        val properties = PropertiesLoader.loadProperties()
        coapPort = properties.getProperty("coapPort").toInt()
    }
}
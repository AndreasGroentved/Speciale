package IoTDevices

import helpers.PropertiesLoader
import org.eclipse.californium.core.CoapServer
import org.eclipse.californium.core.server.resources.Resource


abstract class IoTDevice(val id: String = "") : CoapServer() {
    var coapPort: Int = -1
    val deviceSpecification = DeviceSpecification(id, mutableListOf())

    init {
        loadProperties()
    }

    fun add(resource: Resource, resourceMethods: List<ResourceMethod>): CoapServer {
        deviceSpecification.deviceResources.add(DeviceResource(resourceMethods, resource.uri, resource.attributes.title))
        return super.add(resource)
    }

    private fun loadProperties() {
        coapPort = PropertiesLoader.instance.getProperty("coapPort").toInt()
    }
}
package DeviceManager

import Helpers.IpDevice
import Tangle.TangleController
import com.google.gson.Gson
import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.coap.MediaTypeRegistry
import java.math.BigDecimal


class DeviceManager {


    val devicesIdToSpecification = mutableMapOf<String, String>()
    private val gson = Gson()
    private val tangle = TangleController()

    fun startDiscovery() {
        Thread {
            ClientDiscovery().startListening {
                val simpleDevice = gson.fromJson<IpDevice>(it, IpDevice::class.java)
                println(simpleDevice)
                devicesIdToSpecification[simpleDevice.id] = simpleDevice.specification
            }
        }.start()
    }


    fun getAllDevices(): String = gson.toJson(devicesIdToSpecification.map { IpDevice(it.key, it.value) })
    fun getDevice(id: String): String = devicesIdToSpecification[id]!! //TODO laves om når device ændres

    fun getAllSavings(from: Long, to: Long) =
        devicesIdToSpecification.keys.map { getSavingsForDevice(from, to, it) }.fold(BigDecimal(0)) { a: BigDecimal, b: BigDecimal -> a + b }.toString()


    fun getSavingsForDevice(from: Long, to: Long, deviceId: String): BigDecimal = tangle.getDevicePriceSavings(from, to, deviceId)


    fun get(deviceId: String, path: String): String {
        val client = CoapClient("${devicesIdToSpecification[deviceId]}:5683/$path")
        val response = client.get()
        return response?.let { response.responseText } ?: "No response received"
    }

    fun post(deviceId: String, path: String, parameter: String): String {
        val client = CoapClient("${devicesIdToSpecification[deviceId]}:5683/$path")
        val response = client.post(parameter, MediaTypeRegistry.APPLICATION_JSON)
        return response?.let { response.responseText } ?: "No response received"
    }

}


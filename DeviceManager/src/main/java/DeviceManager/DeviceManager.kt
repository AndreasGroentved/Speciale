package DeviceManager

import Tangle.TangleController
import com.google.gson.Gson
import helpers.Device
import helpers.IdIp
import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.coap.MediaTypeRegistry
import java.math.BigDecimal


class DeviceManager {


    val devicesIdIpToSpecification = mutableMapOf<IdIp, String>() //TODO pair
    private val gson = Gson()
    private val tangle = TangleController()

    fun startDiscovery() {
        Thread {
            ClientDiscovery().startListening {
                val simpleDevice = gson.fromJson<Device>(it, Device::class.java)
                println(simpleDevice)
                devicesIdIpToSpecification[simpleDevice.idIp] = simpleDevice.specification
            }
        }.start()
    }


    private fun getDeviceSpecificationFromId(id: String) = devicesIdIpToSpecification.filter { it.key.id == id }.map { it.value }.firstOrNull()
        ?: "invalid id"

    fun getAllDevices(): String = gson.toJson(devicesIdIpToSpecification.keys)
    fun getDevice(id: String): String = getDeviceSpecificationFromId(id)

    private fun getDeviceKeyFromId(id: String) = devicesIdIpToSpecification.filter { it.key.id == id }.map { it.key }.firstOrNull()

    fun getAllSavings(from: Long, to: Long) =
        devicesIdIpToSpecification.keys.map { getSavingsForDevice(from, to, it.id) }.fold(BigDecimal(0)) { a, b -> a + b }.toString()

    fun getSavingsForDevice(from: Long, to: Long, deviceId: String): BigDecimal = tangle.getDevicePriceSavings(from, to, deviceId)

    fun get(deviceId: String, path: String): String {
        val mapKey = getDeviceKeyFromId(deviceId) ?: return "invalid device id"
        val client = CoapClient("${mapKey.ip}:5683/$path?yolo='yolo'")
        val response = client.get()
        return response?.let { response.responseText } ?: "No response received"
    }

    fun post(deviceId: String, path: String, parameter: String): String {
        val mapKey = getDeviceKeyFromId(deviceId) ?: return "invalid device id"
        val client = CoapClient("${mapKey.ip}:5683/$path")
        val response = client.post(parameter, MediaTypeRegistry.APPLICATION_JSON)
        return response?.let { response.responseText } ?: "No response received"
    }

}


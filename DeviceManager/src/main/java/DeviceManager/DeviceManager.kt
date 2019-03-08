package DeviceManager

import Helpers.SimpleDevice
import Tangle.TangleController
import com.google.gson.Gson
import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.coap.MediaTypeRegistry
import java.math.BigDecimal


class DeviceManager() {


    val devicesIdToIp = mutableMapOf<String, String>()
    private val gson = Gson()
    private val tangle = TangleController()

    fun startDiscovery() {
        Thread {
            ClientDiscovery().startListening {
                val simpleDevice = gson.fromJson<SimpleDevice>(it, SimpleDevice::class.java)
                devicesIdToIp[simpleDevice.id] = simpleDevice.ip
            }
        }.start()
    }


    fun getAllDevices(): String = gson.toJson(devicesIdToIp.map { SimpleDevice(it.key, it.value) })
    fun getDevice(id: String) = gson.toJson(Device(devicesIdToIp[id], id)) //TODO laves om når device ændres

    fun getAllSavings(from: Long, to: Long) =
        devicesIdToIp.keys.map { getSavingsForDevice(from, to, it) }.fold(BigDecimal(0)) { a: BigDecimal, b: BigDecimal -> a + b }.toString()


    fun getSavingsForDevice(from: Long, to: Long, deviceId: String): BigDecimal = tangle.getDevicePriceSavings(from, to, deviceId)


    fun get(deviceId: String, path: String): String {
        val client = CoapClient("${devicesIdToIp[deviceId]}:5683/$path")
        val response = client.get()
        return response?.let { response.responseText } ?: "No response received"
    }

    fun post(deviceId: String, path: String, parameter: String): String {
        val client = CoapClient("${devicesIdToIp[deviceId]}:5683/$path")
        val response = client.post(parameter, MediaTypeRegistry.APPLICATION_JSON)
        return response?.let { response.responseText } ?: "No response received"
    }

}


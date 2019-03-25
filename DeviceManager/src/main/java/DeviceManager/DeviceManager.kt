package DeviceManager

import Tangle.TangleController
import com.google.gson.Gson
import datatypes.iotdevices.Device
import datatypes.iotdevices.IdIp
import datatypes.iotdevices.PostMessage
import datatypes.iotdevices.TangleDeviceSpecification
import datatypes.tangle.Tag
import helpers.EncryptionHelper
import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.coap.MediaTypeRegistry
import org.slf4j.simple.SimpleLoggerFactory
import java.math.BigDecimal
import java.security.PrivateKey


//TODO DB OG CACHES VEDLIGEHOLDELSE, BÅDE PUT OG REMOVE ETC.
//TODO BRUG HASH READ MESSAGES TING FLERE STEDER
//TODO BURDE VÆRE GJORT, MEN DET SKAL I HVERT FALD GÅES EFTER
//TODO JEG ER RET UENIG MED AT ALTING SKAL RETURNERE STRING HERINDE

class DeviceManager {
    private val devicesIdIpToSpecification = mutableMapOf<IdIp, Device>()
    private val registeredDevices = mutableListOf<IdIp>()
    private val gson = Gson()
    private val logger = SimpleLoggerFactory().getLogger("DeviceManager")

    fun getRegisteredDevices(): Map<IdIp, Device> = devicesIdIpToSpecification.filter { it.key in registeredDevices }

    fun getNotRegisteredDevices(): Map<IdIp, Device> = devicesIdIpToSpecification.filterNot { it.key in registeredDevices }

    fun startDiscovery() {
        Thread {
            ClientDiscovery().startListening {
                val simpleDevice = gson.fromJson(it, Device::class.java)
                println(simpleDevice)
                devicesIdIpToSpecification[simpleDevice.idIp] = simpleDevice
            }
        }.start()
    }


    fun registerDevice(privateKey: PrivateKey, publicKey: String, deviceID: String, seed: String, tangle: TangleController): String {
        val spec = devicesIdIpToSpecification.entries.firstOrNull { it.value.idIp.id == deviceID }.let { e ->
            println("device yolo")
            val deviceSpecification = TangleDeviceSpecification(publicKey, e!!.value.specification)
            val toJson = gson.toJson(deviceSpecification)
            val signedJson = "$toJson||${EncryptionHelper.signBase64(privateKey, toJson)}"
            tangle.attachDeviceToTangle(seed, signedJson)?.let { registeredDevices.add(e.key) }
        }
        return "{\"result\" :" +
                (spec?.let { "\"successful\"}" } ?: "\"unsuccessful\"}")
    }

    fun unregisterDevice(privateKey: PrivateKey, seed: String, deviceId: String, tangle: TangleController): String {
        val entry = devicesIdIpToSpecification.entries.firstOrNull { it.key.id == deviceId }
        entry?.let {
            val toJson = gson.toJson(it.value.specification)
            val signed = EncryptionHelper.signBase64(privateKey, toJson)
            tangle.attachBroadcastToTangle(seed, toJson + "__" + signed, Tag.XDSPEC)
            return "{\"unRegister\" :" + "\"successful\"}"
        }
        return "{\"unRegister\" :" + "\"unsuccessful\"}"

    }

    private fun getDeviceSpecificationFromId(id: String) = devicesIdIpToSpecification.filter { it.key.id == id }.map { it.value }.firstOrNull()


    fun getDevices(parameter: String = "all") = "{\"result\":" + gson.toJson(
        when (parameter.trim()) {
            "registered=true" -> getRegisteredDevices()
            "registered=false" -> getNotRegisteredDevices()
            else -> getAllDevices()
        }.keys
    ) + "}"


    private fun getAllDevices() = devicesIdIpToSpecification
    fun getDevice(id: String): Device? = getDeviceSpecificationFromId(id)

    private fun getDeviceKeyFromId(id: String) = devicesIdIpToSpecification.filter { it.key.id == id }.map { it.key }.firstOrNull()

    fun getAllSavings(from: Long, to: Long, tangle: TangleController) =
        devicesIdIpToSpecification.keys.map { getSavingsForDevice(from, to, it.id, tangle) }.fold(BigDecimal(0)) { a, b -> a + b }.toString()

    fun getSavingsForDevice(from: Long, to: Long, deviceId: String, tangle: TangleController): BigDecimal = tangle.getDevicePriceSavings(from, to, deviceId)

    fun get(postMessage: PostMessage): String { //todo, noget retry logik måske?
        logger.info("attempting to call get with message: $postMessage")
        val mapKey = getDeviceKeyFromId(postMessage.deviceID) ?: return "{\"error\":\"Invalid device id\"}"
        println("${mapKey.ip}:5683/${postMessage.path}?${postMessage.params["queryString"]}")
        val client = CoapClient("${mapKey.ip}:5683/${postMessage.path}?${postMessage.params["queryString"]}")

        return client.get()?.responseText ?: "{\"error\":\"No result received\"}"
    }

    fun post(postMessage: PostMessage): String {
        logger.info("attempting to call post with message: $postMessage")
        val mapKey = getDeviceKeyFromId(postMessage.deviceID) ?: return "{\"error\":\"Invalid device id\"}"
        val client = CoapClient("${mapKey.ip}:5683/${postMessage.path}") //todo resource på port
        println(gson.toJson(postMessage.params))
        println("yo")
        val a = client.post(gson.toJson(postMessage), MediaTypeRegistry.APPLICATION_JSON)?.responseText
            ?: "{\"error\":\"No result received\"}"
        return a
    }

    fun post(id: String, path: String, params: String): String {
        val mapKey = getDeviceKeyFromId(id) ?: return "{\"error\":\"Invalid device id\"}"
        val client = CoapClient("${mapKey.ip}:5683/${path}") //todo resource på port
        return client.post(gson.toJson(params), MediaTypeRegistry.APPLICATION_JSON)?.responseText //todo to json på JSON?
            ?: "{\"error\":\"No result received\"}"
    }

}
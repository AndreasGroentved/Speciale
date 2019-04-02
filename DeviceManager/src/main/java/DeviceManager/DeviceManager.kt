package DeviceManager

import Tangle.TangleController
import com.google.gson.Gson
import datatypes.ClientResponse
import datatypes.ErrorResponse
import datatypes.Response
import datatypes.iotdevices.Device
import datatypes.iotdevices.IdIp
import datatypes.iotdevices.PostMessage
import datatypes.iotdevices.TangleDeviceSpecification
import datatypes.tangle.Tag
import helpers.EncryptionHelper
import helpers.LogI
import helpers.PropertiesLoader
import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.coap.MediaTypeRegistry
import org.slf4j.simple.SimpleLoggerFactory
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
    private val port = PropertiesLoader.instance.getProperty("COAPPort")

    fun getRegisteredDevices(): Map<IdIp, Device> = devicesIdIpToSpecification.filter { it.key in registeredDevices }

    fun getNotRegisteredDevices(): Map<IdIp, Device> = devicesIdIpToSpecification.filterNot { it.key in registeredDevices }

    fun startDiscovery() {
        Thread {
            ClientDiscovery().startListening {
                val simpleDevice = gson.fromJson(it, Device::class.java)
                LogI("Found device $simpleDevice")
                devicesIdIpToSpecification[simpleDevice.idIp] = simpleDevice
            }
        }.start()
    }

    fun registerDevice(privateKey: PrivateKey, publicKey: String, deviceID: String, seed: String, tangle: TangleController): Response {
        val spec = devicesIdIpToSpecification.entries.firstOrNull { it.value.idIp.id == deviceID }?.let { e ->
            val deviceSpecification = TangleDeviceSpecification(publicKey, e.value.specification)
            val toJson = gson.toJson(deviceSpecification)
            val signedJson = toJson + "__" + EncryptionHelper.signBase64(privateKey, toJson)
            tangle.attachDeviceToTangle(seed, signedJson)?.let {
                registeredDevices.add(e.key)
            }
        }
        return spec?.let { ClientResponse("succesful") } ?: ErrorResponse("unsuccesful")
    }

    fun unregisterDevice(privateKey: PrivateKey, publicKey: String, seed: String, deviceID: String, tangle: TangleController): Response {
        val spec = devicesIdIpToSpecification.entries.firstOrNull { it.value.idIp.id == deviceID }?.let { e ->
            val deviceSpecification = TangleDeviceSpecification(publicKey, e.value.specification)
            val toJson = gson.toJson(deviceSpecification)
            val signedJson = toJson + "__" + EncryptionHelper.signBase64(privateKey, toJson)
            tangle.attachBroadcastToTangle(seed, signedJson, Tag.XDSPEC)?.let {
                registeredDevices.remove(e.key)
            }
        }
        return spec?.let { ClientResponse("succesful") } ?: ErrorResponse("unsuccesful")
    }

    private fun getDeviceSpecificationFromId(id: String) = devicesIdIpToSpecification.filter { it.key.id == id }.map { it.value }.firstOrNull()

    fun getDevices(parameter: String = "all") = ClientResponse(
        when (parameter.trim()) {
            "registered=true" -> getRegisteredDevices()
            "registered=false" -> getNotRegisteredDevices()
            else -> getAllDevices()
        }.keys
    )

    private fun getAllDevices() = devicesIdIpToSpecification
    fun getDevice(id: String): Device? = getDeviceSpecificationFromId(id)

    private fun getDeviceKeyFromId(id: String) = devicesIdIpToSpecification.filter { it.key.id == id }.map { it.key }.firstOrNull()

    fun get(postMessage: PostMessage): Response { //todo, noget retry logik måske?
        logger.info("attempting to call get with message: $postMessage")
        val mapKey = getDeviceKeyFromId(postMessage.deviceID) ?: return ErrorResponse("Invalid device id")
        val client = CoapClient("${mapKey.ip}:$port/${postMessage.path}?${postMessage.params["queryString"]}")

        return client.get()?.let { gson.fromJson(it.responseText, ClientResponse::class.java) }
            ?: ErrorResponse("No result received")
    }

    fun post(postMessage: PostMessage): Response {
        logger.info("attempting to call post with message: $postMessage")
        val mapKey = getDeviceKeyFromId(postMessage.deviceID) ?: return ErrorResponse("Invalid device id")
        val client = CoapClient("${mapKey.ip}:$port/${postMessage.path}") //todo resource på port
        val a = client.post(gson.toJson(postMessage), MediaTypeRegistry.APPLICATION_JSON)
        return a?.let { gson.fromJson(it.responseText, ClientResponse::class.java) } ?: ErrorResponse("No result received")
    }

    fun post(id: String, path: String, params: String): Response {
        val mapKey = getDeviceKeyFromId(id) ?: return ErrorResponse("Invalid device id")
        val client = CoapClient("${mapKey.ip}:$port/$path")
        return client.post(params, MediaTypeRegistry.APPLICATION_JSON)?.let { gson.fromJson(it.responseText, ClientResponse::class.java) }
            ?: ErrorResponse("No result received")
    }

}
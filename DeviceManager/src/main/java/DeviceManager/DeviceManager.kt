package DeviceManager

import Tangle.TangleController
import com.google.gson.Gson
import datatypes.iotdevices.*
import helpers.EncryptionHelper
import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.coap.MediaTypeRegistry
import org.slf4j.simple.SimpleLoggerFactory
import java.math.BigDecimal
import java.security.PrivateKey
import java.util.*


//TODO DB OG CACHES VEDLIGEHOLDELSE, BÅDE PUT OG REMOVE ETC.
//TODO BRUG HASH READ MESSAGES TING FLERE STEDER

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
            val deviceSpecification = TangleDeviceSpecification(publicKey, e!!.value.specification)
            val toJson = gson.toJson(deviceSpecification)
            val signedJson = "$toJson||${EncryptionHelper.signBase64(privateKey, toJson)}"
            tangle.attachDeviceToTangle(seed, signedJson)?.let { registeredDevices.add(e.key) }
        }
        return "{\"register\" :" +
                (spec?.let { "\"successful\"}" } ?: "\"unsuccessful\"}")
    }

    fun unregisterDevice(deviceId: String): String {
        val idIp = devicesIdIpToSpecification.keys.firstOrNull { it.id == deviceId }
        val successful = idIp?.let { registeredDevices.remove(idIp) } ?: false
        return "{\"register\" :" +
                (if (successful) "\"successful\"}" else "\"unsuccessful\"}")
    }

    private fun getDeviceSpecificationFromId(id: String) = devicesIdIpToSpecification.filter { it.key.id == id }.map { it.value }.firstOrNull()


    fun getDevices(parameter: String = "all") = gson.toJson(
        when (parameter.trim()) {
            "registered=true" -> getRegisteredDevices()
            "registered=false" -> getNotRegisteredDevices()
            else -> getAllDevices()
        }.keys
    )


    private fun getAllDevices() = devicesIdIpToSpecification
    fun getDevice(id: String): Device? = getDeviceSpecificationFromId(id)

    private fun getDeviceKeyFromId(id: String) = devicesIdIpToSpecification.filter { it.key.id == id }.map { it.key }.firstOrNull()

    fun getAllSavings(from: Long, to: Long, tangle: TangleController) =
        devicesIdIpToSpecification.keys.map { getSavingsForDevice(from, to, it.id, tangle) }.fold(BigDecimal(0)) { a, b -> a + b }.toString()

    fun getSavingsForDevice(from: Long, to: Long, deviceId: String, tangle: TangleController): BigDecimal = tangle.getDevicePriceSavings(from, to, deviceId)

    fun get(postMessage: PostMessage): String {
        val mapKey = getDeviceKeyFromId(postMessage.deviceID) ?: return "{\"error\":\"Invalid device id\"}"
        val client = CoapClient("${mapKey.ip}:5683/${postMessage.path}?${postMessage.params["queryString"]}")
        return client.get()?.responseText ?: "{\"error\":\"No response received\"}"
    }

    fun post(postMessage: PostMessage): String {
        val mapKey = getDeviceKeyFromId(postMessage.deviceID) ?: return "{\"error\":\"Invalid device id\"}"
        val client = CoapClient("${mapKey.ip}:5683/${postMessage.path}") //todo resource på port
        return client.post(gson.toJson(postMessage.params), MediaTypeRegistry.APPLICATION_JSON)?.responseText
            ?: "{\"error\":\"No response received\"}"
    }

    fun post(id: String, path: String, params: String): String {
        val mapKey = getDeviceKeyFromId(id) ?: return "{\"error\":\"Invalid device id\"}"
        val client = CoapClient("${mapKey.ip}:5683/${path}") //todo resource på port
        return client.post(gson.toJson(params), MediaTypeRegistry.APPLICATION_JSON)?.responseText
            ?: "{\"error\":\"No response received\"}"
    }

    fun getActivePendingProcurations(accepted: List<Procuration>, seed: String, tangle: TangleController): List<Procuration> {
        val messages = tangle.getMessagesUnchecked(seed, "PRO")
        val procurations = messages.mapNotNull { m ->
            try {
                gson.fromJson(m, Procuration::class.java)
            } catch (e: Exception) {
                null
            }
        }
        return procurations.filter { p -> accepted.firstOrNull { a -> p.messageChainID == a.messageChainID } == null }.filter { p -> p.dateTo >= Date() }
    }

    fun getExpiredProcurations(seed: String, tangle: TangleController): List<Procuration> {
        val messages = tangle.getMessagesUnchecked(seed, "PROACK")
        return messages.mapNotNull { m ->
            try {
                gson.fromJson(m, Procuration::class.java)
            } catch (e: Exception) {
                null
            }
        }.filter { p -> p.dateTo <= Date() }
    }

    fun respondToProcuration(procuration: Procuration, accepted: Boolean, seed: String, tangle: TangleController) {
        val procurationAck = ProcurationAck(procuration.messageChainID, accepted)
        val privateKey = EncryptionHelper.loadPrivateECKeyFromProperties("houseHoldPrivateKey")
        val json = gson.toJson(procurationAck)
        val signBase64 = EncryptionHelper.signBase64(privateKey, json)
        tangle.attachTransactionToTangle(seed, "$json||$signBase64", "PROACK")
    }

}
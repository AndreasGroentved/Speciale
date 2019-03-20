package DeviceManager

import Tangle.TangleController
import com.google.gson.Gson
import datatypes.iotdevices.Device
import datatypes.iotdevices.IdIp
import datatypes.iotdevices.Procuration
import datatypes.iotdevices.ProcurationAck
import helpers.EncryptionHelper
import org.eclipse.californium.core.CoapClient
import org.eclipse.californium.core.coap.MediaTypeRegistry
import org.slf4j.simple.SimpleLoggerFactory
import java.math.BigDecimal
import java.util.*


class DeviceManager {
    private val devicesIdIpToSpecification = mutableMapOf<IdIp, String>()
    private val registeredDevices = mutableListOf<IdIp>()
    private val gson = Gson()
    private val tangle = TangleController()
    private val logger = SimpleLoggerFactory().getLogger("DeviceManager")
    private val seed = "TESTQ9999999999999999999999999999999999999999999999999999999999999999999999999999"

    fun getRegisteredDevices(): Map<IdIp, String> = devicesIdIpToSpecification.filter { it.key in registeredDevices }

    fun getNotRegisteredDevices(): Map<IdIp, String> =
        devicesIdIpToSpecification.filterNot { it.key in registeredDevices }

    fun startDiscovery() {
        Thread {
            ClientDiscovery().startListening {
                val simpleDevice = gson.fromJson<Device>(it, Device::class.java)
                println(simpleDevice)
                devicesIdIpToSpecification[simpleDevice.idIp] = simpleDevice.specification
            }
        }.start()
    }


    fun registerDevice(publicKey: String, deviceID: String): String {
        val idIp = devicesIdIpToSpecification.keys.firstOrNull { it.id == deviceID }
        idIp?.let {
            tangle.attachDeviceToTangle(seed, getDeviceSpecificationFromId(idIp.id))?.let { registeredDevices.add(idIp) }
        }

        return "{\"register\" :" +
                (idIp?.let { "\"successful\"}" } ?: "\"unsuccessful\"}")
    }

    fun unregisterDevice(deviceId: String): String {
        val idIp = devicesIdIpToSpecification.keys.firstOrNull { it.id == deviceId }
        val successful = idIp?.let { registeredDevices.remove(idIp) } ?: false
        return "{\"register\" :" +
                (if (successful) "\"successful\"}" else "\"unsuccessful\"}")
    }

    private fun getDeviceSpecificationFromId(id: String) = devicesIdIpToSpecification.filter { it.key.id == id }.map { it.value }.firstOrNull()
        ?: "{\"error\": \"invalid id\"}"


    fun getDevices(parameter: String = "all") = gson.toJson(
        when (parameter.trim()) {
            "registered=true" -> getRegisteredDevices()
            "registered=false" -> getNotRegisteredDevices()
            else -> getAllDevices()
        }.keys
    )


    private fun getAllDevices() = devicesIdIpToSpecification
    fun getDevice(id: String): String = getDeviceSpecificationFromId(id)

    private fun getDeviceKeyFromId(id: String) = devicesIdIpToSpecification.filter { it.key.id == id }.map { it.key }.firstOrNull()

    fun getAllSavings(from: Long, to: Long) =
        devicesIdIpToSpecification.keys.map { getSavingsForDevice(from, to, it.id) }.fold(BigDecimal(0)) { a, b -> a + b }.toString()

    fun getSavingsForDevice(from: Long, to: Long, deviceId: String): BigDecimal = tangle.getDevicePriceSavings(from, to, deviceId)

    fun get(deviceId: String, path: String, queryString: String): String {
        val mapKey = getDeviceKeyFromId(deviceId) ?: return "{\"error\":\"Invalid device id\"}"
        val client = CoapClient("${mapKey.ip}:5683/$path$queryString")
        return client.get()?.responseText ?: "{\"error\":\"No response received\"}"
    }

    fun post(deviceId: String, path: String, parameter: String): String {
        val mapKey = getDeviceKeyFromId(deviceId) ?: return "{\"error\":\"Invalid device id\"}"
        val client = CoapClient("${mapKey.ip}:5683/$path") //todo resource på port
        return client.post(parameter, MediaTypeRegistry.APPLICATION_JSON)?.responseText
            ?: "{\"error\":\"No response received\"}"
    }

    fun getActivePendingProcurations(): List<Procuration> {
        val messages = tangle.getMessages(seed, "PRO")
        val procurations = messages.mapNotNull { m ->
            try {
                gson.fromJson(m, Procuration::class.java)
            } catch (e: Exception) {
                null
            }
        }
        val accepts = messages.mapNotNull { m ->
            try {
                gson.fromJson(m, Procuration::class.java)
            } catch (e: Exception) {
                null
            }
        }
        return procurations.filter { p -> accepts.firstOrNull { a -> p.messageChainID == a.messageChainID } == null }.filter { p -> p.dateTo >= Date() }
    }

    fun getActiveAcceptedProcurations(): List<Procuration> {
        val messages = tangle.getMessages(seed, "PROACK")
        return messages.mapNotNull { m ->
            try {
                gson.fromJson(m, Procuration::class.java)
            } catch (e: Exception) {
                null
            }
        }.filter { p -> p.dateTo >= Date() }
    }

    fun getExpiredProcurations(): List<Procuration> {
        val messages = tangle.getMessages(seed, "PROACK")
        return messages.mapNotNull { m ->
            try {
                gson.fromJson(m, Procuration::class.java)
            } catch (e: Exception) {
                null
            }
        }.filter { p -> p.dateTo <= Date() }
    }

    fun RespondToProcuration(procuration: Procuration, accepted: Boolean) {
        val procurationAck = ProcurationAck(procuration.messageChainID, accepted)
        val privateKey = EncryptionHelper.loadPrivateECKeyFromProperties("houseHoldPrivateKey")
        val json = gson.toJson(procurationAck)
        val signBase64 = EncryptionHelper.signBase64(privateKey, json)
        tangle.attachTransactionToTangle(seed, "$json||$signBase64", "PROACK")
    }

}
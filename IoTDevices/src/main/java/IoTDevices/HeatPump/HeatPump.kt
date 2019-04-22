package IoTDevices.HeatPump

import IoTDevices.Discovery
import IoTDevices.IoTDevice
import datatypes.ClientResponse
import datatypes.ErrorResponse
import datatypes.iotdevices.PostMessage
import datatypes.iotdevices.ResourceMethod
import helpers.LogE
import helpers.LogI
import org.eclipse.californium.core.CoapResource
import org.eclipse.californium.core.network.CoapEndpoint
import org.eclipse.californium.core.network.EndpointManager
import org.eclipse.californium.core.network.config.NetworkConfig
import org.eclipse.californium.core.server.resources.CoapExchange
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.util.*

val COAP_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT)

fun main() {
    val pump = HeatPump("Swole12", 5683)
    val pump2 = HeatPump("sssaass", 2684)
    pump.start()
    pump2.start()
    Discovery(pump).startDiscovery()
    Discovery(pump2).startDiscovery()
}

class HeatPump(id: String = UUID.randomUUID().toString(), val port: Int = 5683) : IoTDevice(id, port) {
    init {
        addEndpoints()
        add(
            HeatPumpResource("temperature"), listOf(
                ResourceMethod("GET", mapOf("temperature" to "Integer"), "Gets the target temperature of the heat pump"),
                ResourceMethod("POST", mapOf("temperature" to "Integer"), "Adjusts target temperature of the heat pump by diff")
            )
        )
    }

    private fun addEndpoints() {
        EndpointManager.getEndpointManager().networkInterfaces.filter { it is Inet4Address || it.isLoopbackAddress }
            .forEach {
                val bindToAddress = InetSocketAddress(it, port)
                addEndpoint(CoapEndpoint(bindToAddress))
            }
    }

    inner class HeatPumpResource(name: String) : CoapResource(name) {
        private var temperature: Int = 20

        private fun adjustTemperature(diff: Int) {
            temperature += diff
        }

        init {
            attributes.title = "Heat pump resource"
        }

        override fun handleGET(exchange: CoapExchange?) {
            LogI("get temperature $temperature")
            exchange?.respond(gson.toJson(ClientResponse(temperature)))
        }

        override fun handlePOST(exchange: CoapExchange) {
            LogI("posting temperature")
            try {
                exchange.apply {
                    val fromJson = this@HeatPump.gson.fromJson(requestText, PostMessage::class.java)
                    val temp = fromJson.params["temperature"]
                    LogI("new temperature $temp, old $temperature")
                    temperature = Math.round(temp!!.toDouble()).toInt()
                    exchange.respond(gson.toJson(ClientResponse(temperature)))
                }
            } catch (e: Exception) {
                LogE(e.message)
                when (e) {
                    is NumberFormatException -> exchange.respond(gson.toJson(ErrorResponse("Parameter is not a real number")))
                    is IndexOutOfBoundsException -> exchange.respond(gson.toJson(ErrorResponse("Missing parameter diff")))
                }
                exchange.respond(gson.toJson(ErrorResponse("Parameter is not a real number")))
            }
        }
    }
}

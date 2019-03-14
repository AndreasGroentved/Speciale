package IoTDevices.HeatPump

import IoTDevices.IoTDevice
import datatypes.iotdevices.PostMessage
import datatypes.iotdevices.ResourceMethod
import org.eclipse.californium.core.CoapResource
import org.eclipse.californium.core.network.CoapEndpoint
import org.eclipse.californium.core.network.EndpointManager
import org.eclipse.californium.core.network.config.NetworkConfig
import org.eclipse.californium.core.server.resources.CoapExchange
import java.net.Inet4Address
import java.net.InetSocketAddress

val COAP_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT)

fun main() {
    val pump = HeatPump()
    pump.start()
}

class HeatPump : IoTDevice("hest") {
    init {
        addEndpoints()
        add(
            HeatPumpResource(), listOf(
                ResourceMethod("GET", mapOf("temperature" to "Integer"), "Gets the target temperature of the heat pump"),
                ResourceMethod("POST", mapOf("temperature" to "Integer"), "Adjusts target temperature of the heat pump by diff")
            )
        )
    }

    private fun addEndpoints() {
        EndpointManager.getEndpointManager().networkInterfaces.filter { it is Inet4Address || it.isLoopbackAddress }
            .forEach {
                val bindToAddress = InetSocketAddress(it, COAP_PORT)
                addEndpoint(CoapEndpoint(bindToAddress))
            }
    }

    inner class HeatPumpResource : CoapResource("temperature") {
        private var temperature: Int = 20

        private fun adjustTemperature(diff: Int) {
            temperature += diff
        }

        init {
            attributes.title = "Heat pump resource"
        }

        override fun handleGET(exchange: CoapExchange?) {
            exchange?.respond("{\"temperature\": $temperature}")
        }

        override fun handlePOST(exchange: CoapExchange?) {
            try {
                exchange?.let {
                    println(exchange.requestText)
                    val fromJson = this@HeatPump.gson.fromJson(exchange.requestText, PostMessage::class.java)
                    println(fromJson)
                    val temp = fromJson.params["temperature"]
                    println(temp)
                    temperature = temp?.toInt() ?: temperature
                    exchange.respond("{\"temperature\": $temperature}")
                }
            } catch (e: Exception) {
                when (e) {
                    is NumberFormatException -> exchange?.respond("Parameter is not a real number")
                    is IndexOutOfBoundsException -> exchange?.respond("Missing parameter diff")
                }
                exchange?.respond("Parameter is not a real number")
                e.printStackTrace()
            }
        }
    }
}

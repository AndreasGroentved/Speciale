package IoTDevices.HeatPump

import IoTDevices.IoTDevice
import IoTDevices.PostMessage
import IoTDevices.ResourceMethod
import com.google.gson.Gson
import org.eclipse.californium.core.CoapResource
import org.eclipse.californium.core.network.CoapEndpoint
import org.eclipse.californium.core.network.EndpointManager
import org.eclipse.californium.core.network.config.NetworkConfig
import org.eclipse.californium.core.server.resources.CoapExchange
import java.lang.NullPointerException
import java.net.Inet4Address
import java.net.InetSocketAddress

val COAP_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT)

fun main() {
    var pump = HeatPump()
    pump.start()
}

class HeatPump : IoTDevice("hest") {
    init {
        addEndpoints()
        add(
            HeatPumpResource(), mutableListOf(
                ResourceMethod("GET", mutableMapOf(), "Gets the target temperature of the heat pump"),
                ResourceMethod("POST", mutableMapOf("diff" to "Integer"), "Adjusts target temperature of the heat pump by diff")
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

    class HeatPumpResource : CoapResource("temperature") {
        private var temperature: Int = 20

        private fun adjustTemperature(diff: Int) {
            temperature += diff
        }

        init {
            attributes.title = "Heat pump resource"
        }

        override fun handleGET(exchange: CoapExchange?) {
            exchange?.respond(temperature.toString())
        }

        override fun handlePOST(exchange: CoapExchange?) {
            try {
                exchange?.let {
                    val fromJson = Gson().fromJson(exchange.requestText, PostMessage::class.java)
                    adjustTemperature(fromJson.params[0].toInt())
                    exchange.respond("temperature is now $temperature")
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

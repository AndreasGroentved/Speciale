package IoTDevices.HeatPump

import com.google.gson.Gson
import org.eclipse.californium.core.CoapResource
import org.eclipse.californium.core.CoapServer
import org.eclipse.californium.core.network.CoapEndpoint
import org.eclipse.californium.core.network.EndpointManager
import org.eclipse.californium.core.network.config.NetworkConfig
import org.eclipse.californium.core.server.resources.CoapExchange
import java.lang.Exception
import java.lang.IndexOutOfBoundsException
import java.lang.NumberFormatException
import java.net.Inet4Address
import java.net.InetSocketAddress

val COAP_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT)

fun main(args: Array<String>) {
    var pump = HeatPump()
    pump.start()
}

class HeatPump : CoapServer() {
    init {
        addEndpoints()
        add(HeatPumpResource())
    }

    private fun addEndpoints() {
        for (addr in EndpointManager.getEndpointManager().networkInterfaces) {
            // only binds to IPv4 addresses and localhost
            if (addr is Inet4Address || addr.isLoopbackAddress) {
                val bindToAddress = InetSocketAddress(addr, COAP_PORT)
                addEndpoint(CoapEndpoint(bindToAddress))
            }
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
                    adjustTemperature(fromJson.params.get(0).toInt())
                    exchange.respond("temperature is now $temperature")
                }
            } catch (e : Exception) {
                when(e) {is NumberFormatException -> exchange?.respond("Parameter is not a real number")
                is IndexOutOfBoundsException -> exchange?.respond("Missing parameter diff")}
                exchange?.respond("Parameter is not a real number")
                e.printStackTrace()
            }
        }
    }
}
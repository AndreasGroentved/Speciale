package IoTDevices

import com.google.gson.Gson
import helpers.PostMessage
import helpers.PropertiesLoader
import helpers.TimeUsage
import org.eclipse.californium.core.CoapResource
import org.eclipse.californium.core.CoapServer
import org.eclipse.californium.core.coap.MediaTypeRegistry
import org.eclipse.californium.core.server.resources.CoapExchange
import org.eclipse.californium.core.server.resources.Resource
import java.util.*


abstract class IoTDevice(val id: String = "") : CoapServer() {
    var coapPort: Int = -1
    val deviceSpecification = DeviceSpecification(id, mutableListOf())
    private val timeMap = mutableMapOf<Long, Long>()//TODO gem i db
    private var lastCalculateTime = -1L
    protected val gson = Gson()


    init {
        loadProperties()
        lastCalculateTime = System.currentTimeMillis()
        add(
            OnOffResource(), listOf(
                ResourceMethod("GET", mapOf("status" to "Boolean"), "Gets the current status"),
                ResourceMethod("POST", mapOf("status" to "Boolean"), "Turn device on/off")
            )
        )
        add(
            TimeResource(), listOf(ResourceMethod("GET", mapOf("time" to "List<String, String>"), "Gets time in map of hour to on time"))
        )
    }

    private var isOn = true

    protected fun turnOff() {
        updateTimeMap()
        isOn = false
    }

    protected fun turnOn() {
        updateTimeMap()
        isOn = true
    }

    private fun getStartOfHour(time: Long) = Calendar.getInstance().apply {
        timeInMillis = time
        set(Calendar.MILLISECOND, 0);set(Calendar.SECOND, 0);set(Calendar.MINUTE, 0)
    }.timeInMillis


    private fun updateTimeMap() {
        if (!isOn) {
            lastCalculateTime = System.currentTimeMillis()
            return
        }

        val endOfCurrentHour = getStartOfHour(System.currentTimeMillis() + lengthOfHour)
        while (lastCalculateTime < endOfCurrentHour) {
            getStartOfHour(lastCalculateTime)
            val elapsedTime = System.currentTimeMillis() - lastCalculateTime
            val hourUsage = when {
                !isOn -> 0
                (elapsedTime > lengthOfHour) -> lengthOfHour
                else -> elapsedTime
            }
            updateHour(lastCalculateTime, hourUsage)
            lastCalculateTime += lengthOfHour
        }
    }

    private fun updateHour(hour: Long, elapsedTime: Long) {
        val alreadyOnForHour = getUsageForHour(hour)
        timeMap[hour] = alreadyOnForHour + elapsedTime
    }

    private fun getUsageForHour(time: Long) = timeMap.getOrDefault(time, 0L)

    private val lengthOfHour = 60 * 60 * 1000L


    fun getOnTimeFromTo(from: Long, to: Long) = timeMap.filter { it.key in from..to }


    fun add(resource: Resource, resourceMethods: List<ResourceMethod>): CoapServer {
        deviceSpecification.deviceResources.add(DeviceResource(resourceMethods, resource.uri, resource.attributes.title))

        return super.add(resource)
    }

    private fun loadProperties() {
        coapPort = PropertiesLoader.instance.getProperty("coapPort").toInt()
    }

    inner class TimeResource : CoapResource("time") {
        init {
            attributes.title = "Time"
        }

        override fun handleGET(exchange: CoapExchange?) {
            updateTimeMap()
            exchange?.let {
                val map = exchange.requestOptions.uriQuery.map {
                    it.split("=").let {
                        mapOf(
                            "from" to (it.getOrNull(0)?.toLong() ?: 0L), "to" to (it.getOrNull(0)?.toLong()
                                ?: System.currentTimeMillis())
                        )
                    }
                }.first() //TODO det er fedt -> lav om
                val timeFromTo = getOnTimeFromTo(map["from"]!!.toLong(), map["to"]!!.toLong()).map { TimeUsage(it.key, it.value) }
                val hourOnList = gson.toJson(timeFromTo)
                exchange.respond(hourOnList)
            }
        }
    }


    inner class OnOffResource : CoapResource("onOff") {
        init {
            attributes.title = "On/off"
        }

        override fun handleGET(exchange: CoapExchange?) {
            exchange?.respond(isOn.toString())
        }

        override fun handlePOST(exchange: CoapExchange?) {
            exchange?.let {
                if (exchange.isValidJson(exchange.requestText)) {
                    exchange.respond("invalid json"); return@handlePOST
                }

                val postMessage = gson.fromJson(exchange.requestText, PostMessage::class.java).params
                val turnOn = postMessage["status"]?.toBoolean() ?: return@let
                if (turnOn) turnOn() else turnOff()
            }
        }
    }
}

fun CoapExchange.isValidJson(string: String) = this.requestOptions.isContentFormat(MediaTypeRegistry.APPLICATION_JSON)
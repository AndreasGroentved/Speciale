package IoTDevices

import com.google.gson.Gson
import helpers.PropertiesLoader
import helpers.TimeUsage
import org.eclipse.californium.core.CoapResource
import org.eclipse.californium.core.CoapServer
import org.eclipse.californium.core.server.resources.CoapExchange
import org.eclipse.californium.core.server.resources.Resource
import java.util.*


abstract class IoTDevice(val id: String = "") : CoapServer() {
    var coapPort: Int = -1
    val deviceSpecification = DeviceSpecification(id, mutableListOf())
    protected val timeMap = mutableMapOf<Long, Long>()//TODO gem i db
    private var lastCalculateTime = -1L
    protected val gson = Gson()

    init {
        loadProperties()
        lastCalculateTime = System.currentTimeMillis()
        add(
            OnOffResource(), listOf(
                ResourceMethod("GET", mapOf(), "Gets the current status"),
                ResourceMethod("POST", mapOf("status" to "Boolean"), "Turn device on/off")
            )
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


    fun add(resource: Resource, resourceMethods: List<ResourceMethod>): CoapServer {
        deviceSpecification.deviceResources.add(DeviceResource(resourceMethods, resource.uri, resource.attributes.title))

        return super.add(resource)
    }

    private fun loadProperties() {
        coapPort = PropertiesLoader.instance.getProperty("coapPort").toInt()

    }

    inner class TimeResource : CoapResource("Time") {
        init {
            attributes.title = "Time"
        }

        override fun handleGET(exchange: CoapExchange?) {
            updateTimeMap()

            val hourOnList = gson.toJson(timeMap.map { TimeUsage(it.key, it.value) })
            exchange?.let {
                println(exchange.requestOptions.locationQuery)
                val turnOn = Gson().fromJson(exchange.requestText, PostMessage::class.java).params.firstOrNull()?.toBoolean()
                    ?: let { exchange.respond("invalid input"); return }
                if (turnOn) turnOn() else turnOff()
                exchange.respond("device is now $isOn")
            }
        }

        override fun handlePOST(exchange: CoapExchange?) {
            exchange?.let {
                val turnOn = gson.fromJson(exchange.requestText, PostMessage::class.java).params.firstOrNull()?.toBoolean()
                    ?: let { exchange.respond("invalid input"); return }
                if (turnOn) turnOn() else turnOff()
                exchange.respond("device is now $isOn")
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
                val turnOn = gson.fromJson(exchange.requestText, PostMessage::class.java).params.firstOrNull()?.toBoolean()
                    ?: let { exchange.respond("invalid input"); return }
                if (turnOn) turnOn() else turnOff()
                exchange.respond("device is now $isOn")
            }
        }
    }
}
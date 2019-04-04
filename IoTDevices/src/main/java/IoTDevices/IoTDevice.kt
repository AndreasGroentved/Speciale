package IoTDevices

import com.google.gson.Gson
import datatypes.TimePair
import datatypes.iotdevices.*
import helpers.LogI
import helpers.PropertiesLoader
import org.dizitart.no2.Nitrite
import org.dizitart.no2.objects.ObjectRepository
import org.eclipse.californium.core.CoapResource
import org.eclipse.californium.core.CoapServer
import org.eclipse.californium.core.coap.MediaTypeRegistry
import org.eclipse.californium.core.server.resources.CoapExchange
import org.eclipse.californium.core.server.resources.Resource
import java.util.*


abstract class IoTDevice(val id: String = "") : CoapServer() {
    var coapPort: Int = -1
    val deviceSpecification = DeviceSpecification(id, mutableListOf())
    private val timeMap = mutableMapOf<Long, Long>()
    private var lastCalculateTime = -1L
    protected val gson = Gson()
    private var timeRep: ObjectRepository<TimePair>
    private val db: Nitrite

    init {
        db = Nitrite.builder()
            .filePath("time.db")
            .openOrCreate()
        timeRep = db.getRepository(TimePair::class.java)
        timeRep.find().forEach { timeMap[it.hour] = it.amount }

        loadProperties()
        lastCalculateTime = System.currentTimeMillis()
        add(
            OnOffResource("onOff"), listOf(
                ResourceMethod("GET", mapOf("status" to "Boolean"), "Gets the current status"),
                ResourceMethod("POST", mapOf("status" to "Boolean"), "Turn device on/off")
            )
        )

        super.add   (
            TimeResource()
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

        var lastCalcHour = getStartOfHour(lastCalculateTime)
        var endOfCurrentHour = getStartOfHour(lastCalcHour + lengthOfHour)
        val now = System.currentTimeMillis()
        while (lastCalcHour < now) {
            val elapsedTime = if (now > endOfCurrentHour) endOfCurrentHour - lastCalculateTime
            else now - lastCalculateTime

            val hourUsage = when {
                !isOn -> 0
                else -> elapsedTime
            }
            updateHour(lastCalcHour, hourUsage)
            endOfCurrentHour += lengthOfHour
            lastCalcHour += lengthOfHour
            lastCalculateTime = lastCalcHour
        }
        lastCalculateTime = now
        updateDb()
    }

    private fun updateDb() {
        timeRep.drop()
        timeRep = db.getRepository(TimePair::class.java)
        timeMap.forEach {
            timeRep.insert(TimePair(it.key, it.value))
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
            attributes.title = "time"
        }

        override fun handleGET(exchange: CoapExchange?) {
            LogI("yo")
            updateTimeMap()
            exchange?.apply {
                val from = requestOptions.uriQuery.getOrNull(0)?.let { it.split("=").getOrNull(1)?.toLong() }
                    ?: 0L
                val to = requestOptions.uriQuery.getOrNull(1)?.let { it.split("=").getOrNull(1)?.toLong() }
                    ?: 0L
                println("from $from , to $to")
                println(timeMap)
                val timeFromTo = getOnTimeFromTo(from, to).map { TimeUsage(it.key, it.value) }
                println(timeFromTo)
                val hourOnList = "{\"result\":${gson.toJson(timeFromTo)}}"
                respond(hourOnList)
            }
        }
    }


    inner class OnOffResource(title:String) : CoapResource(title) {
        init {
            attributes.title = title
        }

        override fun handleGET(exchange: CoapExchange?) {
            exchange?.respond("{\"result\":$isOn}")
        }

        override fun handlePOST(exchange: CoapExchange?) {
            exchange?.apply {
                if (!isValidJson(requestText)) {
                    respond("{\"error\":\"invalid json\"}"); return@handlePOST
                }
                val postMessage = gson.fromJson(requestText, PostMessage::class.java).params
                val turnOn = postMessage["status"]?.toBoolean() ?: return@apply
                if (turnOn) turnOn() else turnOff()
                respond("{\"result\":{\"status\":$turnOn }}")
            }
        }
    }
}

fun CoapExchange.isValidJson(string: String) = this.requestOptions.isContentFormat(MediaTypeRegistry.APPLICATION_JSON)